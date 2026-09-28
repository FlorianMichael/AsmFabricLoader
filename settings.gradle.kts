pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("base.settings")
    id("base.fabric_settings")
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.lenni0451.net/everything")
    }
}

rootProject.name = "asmfabricloader"

include("asmfabricloader-test-mod")
