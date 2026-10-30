plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "dev.nycocado.atlas.shared"
        compileSdk =
            libs.versions.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kt.string.similarity)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

tasks.withType<Test>().configureEach {
    val cards = rootDir.resolve("assets/doomlings")
    inputs.dir(cards).withPathSensitivity(PathSensitivity.RELATIVE)
    systemProperty("cards.dir", cards.path)
    systemProperty("enabled.sets", providers.gradleProperty("atlas.enabledSets").get())
    systemProperty("enabled.types", providers.gradleProperty("atlas.enabledTypes").get())
}
