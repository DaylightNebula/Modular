plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("kapt") version "2.2.0"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.minestom:minestom:2025.07.30-1.21.8")
    kapt(project(":modular-processor"))
    implementation(project(":modular-annotations"))
    implementation(project(":modular-executor"))
    implementation("org.reflections:reflections:0.10.2")
}
