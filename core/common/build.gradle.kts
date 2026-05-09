plugins {
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("com.android.library")
}

android {
    namespace = "com.misterio.core.common"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
