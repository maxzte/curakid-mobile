plugins {
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    id("com.android.library")
    id("jacoco")
}

android {
    namespace = "com.misterio.curakid.core.common"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.jvmArgs("-XX:+EnableDynamicAgentLoading")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*Module*.*",
        "**/*Qualifier*.*"
    )
    val debugTree = fileTree("${project.layout.buildDirectory.get()}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.layout.buildDirectory.get()) {
        include("jacoco/testDebugUnitTest.exec")
    })
}
