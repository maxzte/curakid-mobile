plugins {
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("com.android.library")
}

android {
    namespace = "com.misterio.core.data"
    compileSdk = 37

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 26
        buildConfigField("String", "VERSION_NAME", "\"1.0.0\"")
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.core.network)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)

    // Coroutines for repository logic
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.androidx.work.runtime.ktx)
}
