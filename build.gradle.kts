plugins {
    id("maven-publish")
}

subprojects {
    group = "com.github.DaylightNebula"
    version = "0.1"

    apply(plugin = "java")

    // publish non tests
    if (!project.name.contains("Test")) {
        apply(plugin = "maven-publish")

        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    groupId = "com.github.DaylightNebula"
                    artifactId = project.name
                    version = project.version.toString()

                    from(components["java"])
                }
            }

            repositories {
                mavenLocal()
            }
        }
    }
}
