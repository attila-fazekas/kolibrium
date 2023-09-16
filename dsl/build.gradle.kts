plugins {
    id("kolibrium.library-conventions")
    id("kolibrium.test-conventions")
}

version = "0.1.0-SNAPSHOT"

dependencies {
    api("org.seleniumhq.selenium:selenium-java:_")
    implementation("commons-validator:commons-validator:_")
}
