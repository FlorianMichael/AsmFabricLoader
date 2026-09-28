plugins {
    id("base.java")
    id("base.fabric")
}

dependencies {
    implementation(libs.classtransform.core)
    implementation(projects.asmfabricloader)
}
