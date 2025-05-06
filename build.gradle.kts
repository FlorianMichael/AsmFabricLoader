import de.florianmichael.baseproject.*

plugins {
    id("fabric-loom")
    id("de.florianmichael.baseproject.BaseProject")
}

allprojects {

    setupProject()
    setupFabric()
    setupPublishing(listOf(DeveloperInfo("FlorianMichael", "EnZaXD", "florian.michael07@gmail.com")))

}

val jij = configureJij()

repositories {
    mavenCentral()
    maven("https://maven.lenni0451.net/everything")
}

dependencies {
    jij("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    jij("net.lenni0451:Reflect:1.5.0")
    jij("net.lenni0451.classtransform:core:1.14.1") {
        exclude(group = "org.ow2.asm", module = "asm")
        exclude(group = "org.ow2.asm", module = "asm-commons")
        exclude(group = "org.ow2.asm", module = "asm-tree")
    }

    processJijDependencies()
}

tasks {
    jar {
        dependsOn(configurations["mappings"])
        val mappingsJar = configurations["mappings"].resolvedConfiguration.resolvedArtifacts.firstOrNull { it.name.contains("mappings") }?.file

        if (mappingsJar != null && mappingsJar.exists()) {
            val mappingsFile = zipTree(mappingsJar).matching {
                include("mappings/mappings.tiny")
            }.singleFile

            from(mappingsFile) {
                rename { "afl_mappings.tiny" }
            }
        }
    }
}
