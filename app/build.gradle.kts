import com.android.build.api.artifact.SingleArtifact
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.w3c.dom.Element
import java.io.File
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.screenshot)
    alias(libs.plugins.aboutlibraries)
}

android {
    namespace = "dev.nycocado.atlas"
    compileSdk {
        version =
            release(
                libs.versions.compileSdk
                    .get()
                    .toInt(),
            )
    }

    defaultConfig {
        applicationId = "dev.nycocado.atlas"
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.compileSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "0.0.0"
    }

    // The release key never enters the repo: the maintainer's ~/.gradle/gradle.properties (or the release workflow)
    // passes it as Gradle properties. Without them, the release APK is built unsigned.
    val releaseKey = providers.gradleProperty("atlas.release.storeFile").orNull
    signingConfigs {
        if (releaseKey != null) {
            create("release") {
                storeFile = file(releaseKey)
                storePassword = providers.gradleProperty("atlas.release.storePassword").get()
                keyAlias = providers.gradleProperty("atlas.release.keyAlias").get()
                // PKCS12 keystores use one password for the store and the key.
                keyPassword = storePassword
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
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
    androidResources {
        generateLocaleConfig = true
    }
    experimentalProperties["android.experimental.enableScreenshotTest"] = true
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
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.adaptive.navigation3)
    implementation(libs.aboutlibraries.core)
    implementation(libs.coil.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.androidx.compose.ui.tooling)
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

// Keeps each translation to one word per concept (CONTRIBUTING.md#translations): fail if a word that
// docs/glossary/<language>.json rules out shows up in that language's interface strings.
abstract class CheckGlossary : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val glossaries: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val resources: DirectoryProperty

    @TaskAction
    fun check() {
        val problems =
            glossaries.get().asFile.listFiles { it: File -> it.extension == "json" }.orEmpty().flatMap { file ->
                val language = file.nameWithoutExtension
                val strings = resources.get().file("values-$language/strings.xml").asFile
                if (!strings.isFile) return@flatMap emptyList()
                val text =
                    DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder()
                        .parse(strings)
                        .documentElement.textContent

                @Suppress("UNCHECKED_CAST")
                val glossary = JsonSlurper().parse(file) as Map<String, List<Any>>
                (glossary.getValue("interface") + glossary.getValue("cards")).flatMap { entry ->
                    @Suppress("UNCHECKED_CAST")
                    val term = entry as Map<String, Any>

                    @Suppress("UNCHECKED_CAST")
                    val avoid = term.getValue("avoid") as List<String>
                    avoid
                        .filter { Regex("(?iu)(?<!\\p{L})${Regex.escape(it)}(?!\\p{L})").containsMatchIn(text) }
                        .map { "$language: \"$it\" found, the glossary uses \"${term["use"]}\" for \"${term["en"]}\"" }
                }
            }
        check(problems.isEmpty()) { problems.joinToString("\n") }
    }
}

val checkGlossary =
    tasks.register<CheckGlossary>("checkGlossary") {
        description = "Fails if a translation uses a word its glossary in docs/glossary rules out."
        glossaries.set(rootProject.layout.projectDirectory.dir("docs/glossary"))
        resources.set(layout.projectDirectory.dir("src/main/res"))
    }
tasks.named("check") { dependsOn(checkGlossary) }

// Sets shipped in the APK. The rest of assets/doomlings stays in the repo until its data is checked.
val enabledSets = listOf("classic")

// Card types the app uses. Gene Pools and the Birth of Life wait for scoring (ROADMAP.md, phase 3).
val enabledTypes = listOf("trait", "age", "catastrophe")

abstract class PackageCards : DefaultTask() {
    @get:Input
    abstract val sets: ListProperty<String>

    @get:Input
    abstract val types: ListProperty<String>

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
        val cards = all.filter { it["set"] in sets.get() && it["type"] in types.get() }
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
            description = "Copies the enabled sets' and types' cards.json and images into the APK assets."
            sets.set(enabledSets)
            types.set(enabledTypes)
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
