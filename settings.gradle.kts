pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Curakid"
include(":app")
include(":core:common")
include(":core:database")
include(":core:data")
include(":core:domain")
include(":core:network")
include(":core:ui")

include(":feature:sign-up")
include(":feature:home")
include(":feature:profile")
include(":feature:calendar")
include(":feature:development")
include(":feature:chat")
