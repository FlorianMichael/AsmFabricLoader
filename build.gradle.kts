plugins {
    `java-library`
    `maven-publish`
    signing
    idea
    id("fabric-loom") version "1.10-SNAPSHOT"
}

allprojects {
    apply(plugin = "fabric-loom")

    group = property("maven_group") as String
    version = property("maven_version") as String
    description = property("maven_description") as String

    dependencies {
        minecraft("com.mojang:minecraft:${property("minecraft_version")}")
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${property("parchment_version")}@zip")
        })
        modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    }

    tasks {
        processResources {
            val projectVersion = project.version
            val projectDescription = project.description

            filesMatching("fabric.mod.json") {
                expand("version" to projectVersion, "description" to projectDescription)
            }
        }
    }

    java {
        withSourcesJar()
        withJavadocJar()

        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }

        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

base {
    archivesName.set(property("maven_name") as String)
}

val jij: Configuration by configurations.creating

repositories {
    mavenCentral()
    maven("https://maven.lenni0451.net/everything")
    maven("https://maven.parchmentmc.org")
}

dependencies {
    jij("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    jij("net.lenni0451:Reflect:1.5.0")
    jij("net.lenni0451.classtransform:core:1.14.1") {
        exclude(group = "org.ow2.asm", module = "asm")
        exclude(group = "org.ow2.asm", module = "asm-commons")
        exclude(group = "org.ow2.asm", module = "asm-tree")
    }

    // Fabric's jar in jar system doesn't support transitive dependencies, so we have to manually add them
    configurations["jij"].dependencies.forEach { dependency ->
        val compileDep = dependencies.create(dependency.toString()) as ExternalModuleDependency
        compileDep.isTransitive = false
        dependencies.add("compileOnlyApi", compileDep)
        dependencies.add("implementation", compileDep)
        dependencies.add("include", compileDep)
    }
}

tasks {
    jar {
        val projectName = project.name

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
        // Rename the project's license file to LICENSE_<project_name> to avoid conflicts
        from("LICENSE") {
            rename { "LICENSE_${projectName}" }
        }
    }

    withType<PublishToMavenRepository>().configureEach {
        dependsOn(withType<Sign>())
    }
}

idea {
    module {
        excludeDirs.add(file("run"))
    }
}

publishing {
    repositories {
        maven {
            name = "reposilite"
            url = uri("https://maven.lenni0451.net/" + if (version.toString().endsWith("SNAPSHOT")) "snapshots" else "releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
        maven {
            name = "ossrh"
            val releasesUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl)
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set(artifactId)
                description.set(project.description)
                url.set("https://github.com/FlorianMichael/AsmFabricLoader")
                licenses {
                    license {
                        name.set("Apache-2.0 license")
                        url.set("https://github.com/FlorianMichael/AsmFabricLoader/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("FlorianMichael")
                        name.set("EnZaXD")
                        email.set("florian.michael07@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/FlorianMichael/AsmFabricLoader.git")
                    developerConnection.set("scm:git:ssh://github.com/FlorianMichael/AsmFabricLoader.git")
                    url.set("https://github.com/FlorianMichael/AsmFabricLoader")
                }
            }
        }
    }
}

signing {
    isRequired = false
    sign(publishing.publications["maven"])
}
