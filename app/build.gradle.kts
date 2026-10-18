import com.android.build.api.artifact.SingleArtifact

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

androidComponents.onVariants { variant ->
    val task =
        tasks.register<CheckPermissions>("check${variant.name.replaceFirstChar { it.uppercase() }}Permissions") {
            manifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            allowlist.set(layout.projectDirectory.file("permissions-allowlist.txt"))
        }
    tasks.named("check") { dependsOn(task) }
}
