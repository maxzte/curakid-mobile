import java.util.Properties

plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.misterio.feature.signup"
    compileSdk = 37

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        minSdk = 26

        val localProperties = Properties()
        val localPropertiesFile = project.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { input ->
                localProperties.load(input)
            }
        }
        val googleWebClientId = localProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: ""

        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.ui)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.androidx.ui.tooling.preview)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    debugImplementation(libs.androidx.ui.tooling)
    ksp(libs.hilt.compiler)
}
