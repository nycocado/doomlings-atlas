plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    android {
        namespace = "dev.nycocado.atlas.shared"
        compileSdk = 37
        minSdk = 26
    }
}
