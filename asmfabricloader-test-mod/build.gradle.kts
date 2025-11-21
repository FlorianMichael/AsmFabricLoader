dependencies {
    implementation("net.lenni0451.classtransform:core:1.14.1")
    implementation(project(":"))
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
