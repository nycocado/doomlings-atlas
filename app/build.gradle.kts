import com.android.build.api.artifact.SingleArtifact
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.nycocado.atlas"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "dev.nycocado.atlas"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.0.0"
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    lint {
        warningsAsErrors = true
        // New library releases must not break the build; Dependabot handles updates.
        disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion")
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// The app works offline (ADR 0002): fail if the merged manifest asks for a permission
// that is not in permissions-allowlist.txt, e.g. one added by a library.
abstract class CheckPermissions : DefaultTask() {
    @get:InputFile
    abstract val manifest: RegularFileProperty

    @get:InputFile
    abstract val allowlist: RegularFileProperty

    @TaskAction
    fun check() {
        val requested =
            Regex("""<uses-permission(?:-sdk-23)?\s[^>]*android:name="([^"]+)"""")
                .findAll(manifest.get().asFile.readText())
                .map { it.groupValues[1] }
                .toSet()
        val allowed =
            allowlist
                .get()
                .asFile
                .readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .toSet()
        val unexpected = requested - allowed
        check(unexpected.isEmpty()) {
            "Permissions not in app/permissions-allowlist.txt: ${unexpected.joinToString()}"
        }
    }
}

// Sets shipped in the APK. The rest of assets/doomlings stays in the repo until its data is checked.
val enabledSets = listOf("classic")

// Copies into the APK assets only the enabled sets: a filtered cards.json and the images it points to.
abstract class PackageCards : DefaultTask() {
    @get:Input
    abstract val sets: ListProperty<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val source: DirectoryProperty

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @TaskAction
    fun pack() {
        val from = source.get().asFile
        val to = output.get().asFile.resolve("cards")
        to.deleteRecursively()

        @Suppress("UNCHECKED_CAST")
        val all = JsonSlurper().parse(from.resolve("cards.json")) as List<Map<String, Any?>>
        val cards = all.filter { it["set"] in sets.get() }
        val empty = sets.get() - cards.map { it["set"] }.toSet()
        check(empty.isEmpty()) { "Enabled sets with no cards: ${empty.joinToString()}" }

        to.mkdirs()
        to.resolve("cards.json").writeText(JsonOutput.toJson(cards))
        cards.map { it["image"] as String }.forEach { from.resolve(it).copyTo(to.resolve(it)) }
    }
}

androidComponents.onVariants { variant ->
    val cards =
        tasks.register<PackageCards>("package${variant.name.replaceFirstChar { it.uppercase() }}Cards") {
            sets.set(enabledSets)
            source.set(rootProject.layout.projectDirectory.dir("assets/doomlings"))
        }
    variant.sources.assets?.addGeneratedSourceDirectory(cards, PackageCards::output)

    val task =
        tasks.register<CheckPermissions>("check${variant.name.replaceFirstChar { it.uppercase() }}Permissions") {
            manifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            allowlist.set(layout.projectDirectory.file("permissions-allowlist.txt"))
        }
    tasks.named("check") { dependsOn(task) }
}
