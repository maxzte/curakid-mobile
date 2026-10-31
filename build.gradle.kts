import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    jacoco
}

subprojects {
    apply(plugin = "jacoco")

    configure<JacocoPluginExtension> {
        toolVersion = "0.8.14"
    }

    plugins.withId("com.android.application") {
        configure<ApplicationExtension> {
            buildTypes {
                getByName("debug") {
                    enableUnitTestCoverage = true
                }
            }
        }
    }

    plugins.withId("com.android.library") {
        configure<LibraryExtension> {
            buildTypes {
                getByName("debug") {
                    enableUnitTestCoverage = true
                }
            }
        }
    }

    tasks.withType<Test> {
        configure<JacocoTaskExtension> {
            isEnabled = true
            isIncludeNoLocationClasses = false
            excludes = listOf(
                "jdk.internal.*",
                "jdk.proxy*",
                "jdk.proxy2.*",
                "sun.util.*",
                "*.Proxy*",
                "*.Proxy"
            )
        }
    }
}

tasks.register<JacocoReport>("jacocoRootReport") {
    description = "Generates consolidated Jacoco code coverage report for debug build"
    val leafProjects = subprojects.filter { it.subprojects.isEmpty() }
    dependsOn(leafProjects.map { "${it.path}:test" })

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*", "**/*Hilt*.*", "**/Hilt_*.*",
        "**/*_Factory.*", "**/*_MembersInjector.*", "**/*_HiltModules*.*",
        "**/*Module_*Factory.*", "**/*_Provide*Factory.*"
    )

    val debugClasses = leafProjects.map { subproject ->
        val buildDir = subproject.layout.buildDirectory

        val androidClasses = fileTree(buildDir.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
            exclude(fileFilter)
        }
        val androidJavaClasses = fileTree(buildDir.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")) {
            exclude(fileFilter)
        }
        val jvmKotlinClasses = fileTree(buildDir.dir("classes/kotlin/main")) {
            exclude(fileFilter)
        }
        val jvmJavaClasses = fileTree(buildDir.dir("classes/java/main")) {
            exclude(fileFilter)
        }

        androidClasses + androidJavaClasses + jvmKotlinClasses + jvmJavaClasses
    }

    val mainSrc = leafProjects.flatMap { subproject ->
        listOf(
            "${subproject.projectDir}/src/main/java",
            "${subproject.projectDir}/src/main/kotlin"
        )
    }

    val executionDataFiles = leafProjects.map { subproject ->
        fileTree(subproject.layout.buildDirectory) {
            include(
                "**/testDebugUnitTest.exec",
                "**/jacoco/testDebugUnitTest.exec",
                "**/jacoco/test.exec",
                "**/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "**/outputs/unit_test_code_coverage/debug/testDebugUnitTest.exec",
                "**/test.exec"
            )
        }
    }

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugClasses))
    executionData.setFrom(files(executionDataFiles))
}
