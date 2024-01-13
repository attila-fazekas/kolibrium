plugins {
    id("kolibrium.library-conventions")
    id("kolibrium.test-conventions")
}

dependencies {
    api("org.seleniumhq.selenium:selenium-java:_")
    testImplementation("com.titusfortner:selenium-logger:_")
}
