plugins {
    id("java")
    kotlin("jvm") version "2.2.0"
    kotlin("kapt") version "2.2.0"
}

repositories {
    mavenCentral()
}

dependencies {
    kapt(project(":modular-processor"))
    annotationProcessor(project(":modular-processor"))
    implementation(project(":modular-annotations"))
    implementation(project(":modular-executor"))
}
