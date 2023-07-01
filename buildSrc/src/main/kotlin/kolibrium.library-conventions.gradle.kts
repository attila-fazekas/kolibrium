plugins {
    `java-library`
    `maven-publish`
    id("kolibrium.base-conventions")
    id("com.github.johnrengelman.shadow")
}

kotlin {
    explicitApi()
}

publishing {
    publications {
        create<MavenPublication>("kolibrium") {
            artifactId = "$name-$artifactId"
            from(components["kotlin"])
        }
    }
}