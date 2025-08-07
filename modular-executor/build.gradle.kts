plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":modular-annotations"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
}
