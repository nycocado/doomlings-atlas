plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.screenshot) apply false
    alias(libs.plugins.aboutlibraries) apply false
    alias(libs.plugins.spotless)
}

spotless {
    val ktlintVersion = libs.versions.ktlint.get()
    kotlin {
        target("app/src/**/*.kt", "shared/src/**/*.kt")
        // Not run by default with ktlint 1.8.
        ktlint(ktlintVersion).editorConfigOverride(mapOf("ktlint_standard_no-unused-imports" to "enabled"))
    }
    kotlinGradle {
        target("*.gradle.kts", "app/*.gradle.kts", "shared/*.gradle.kts")
        ktlint(ktlintVersion)
    }
}
