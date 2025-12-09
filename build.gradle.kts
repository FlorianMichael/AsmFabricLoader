import de.florianmichael.baseproject.*

plugins {
    id("fabric-loom")
    id("de.florianmichael.baseproject.BaseProject")
}

allprojects {

    setupProject()
    setupFabric()

}

setupPublishing()

val jij = configureJij()

repositories {
    mavenCentral()
    maven("https://maven.lenni0451.net/everything")
}

dependencies {
    jij("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    jij("net.lenni0451:Reflect:1.6.0")
    jij("net.lenni0451.classtransform:core:1.14.1") {
        exclude(group = "org.ow2.asm", module = "asm")
        exclude(group = "org.ow2.asm", module = "asm-commons")
        exclude(group = "org.ow2.asm", module = "asm-tree")
    }

    includeTransitiveJijDependencies()
}
