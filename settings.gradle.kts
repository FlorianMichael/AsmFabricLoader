pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
	}

    plugins {
        id("fabric-loom") version "1.10-SNAPSHOT"
        id("de.florianmichael.baseproject.BaseProject") version "1.0.0"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "AsmFabricLoader"

include("asmfabricloader-test-mod")

