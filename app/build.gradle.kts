import com.android.build.api.artifact.SingleArtifact
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.w3c.dom.Element
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory

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
        val document =
            DocumentBuilderFactory
                .newInstance()
                .apply { isNamespaceAware = true }
                .newDocumentBuilder()
                .parse(manifest.get().asFile)
        val requested =
            listOf("uses-permission", "uses-permission-sdk-23")
                .flatMap { tag ->
                    val nodes = document.getElementsByTagName(tag)
                    (0 until nodes.length).map {
                        (nodes.item(it) as Element).getAttributeNS("http://schemas.android.com/apk/res/android", "name")
                    }
                }.toSet()
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

    @get:Inject
    abstract val files: FileSystemOperations

    @TaskAction
    fun pack() {
        @Suppress("UNCHECKED_CAST")
        val all = JsonSlurper().parse(source.file("cards.json").get().asFile) as List<Map<String, Any?>>
        val cards = all.filter { it["set"] in sets.get() }
        val empty = sets.get() - cards.map { it["set"] }.toSet()
        check(empty.isEmpty()) { "Enabled sets with no cards: ${empty.joinToString()}" }

        val json = temporaryDir.resolve("cards.json").apply { writeText(JsonOutput.toJson(cards)) }
        // Sync also deletes what a previous run left, e.g. images of a set no longer enabled.
        files.sync {
            from(json)
            from(source) { include(cards.map { it["image"] as String }) }
            into(output.dir("cards"))
        }
    }
}

androidComponents.onVariants { variant ->
    val cards =
        tasks.register<PackageCards>("package${variant.name.replaceFirstChar { it.uppercase() }}Cards") {
            description = "Copies the enabled sets' cards.json and images into the APK assets."
            sets.set(enabledSets)
            source.set(rootProject.layout.projectDirectory.dir("assets/doomlings"))
        }
    variant.sources.assets?.addGeneratedSourceDirectory(cards, PackageCards::output)

    val task =
        tasks.register<CheckPermissions>("check${variant.name.replaceFirstChar { it.uppercase() }}Permissions") {
            description = "Fails if the merged manifest has a permission missing from permissions-allowlist.txt."
            manifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            allowlist.set(layout.projectDirectory.file("permissions-allowlist.txt"))
        }
    tasks.named("check") { dependsOn(task) }
}
