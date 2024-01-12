plugins {
    id("kolibrium.test-conventions")
    id("kolibrium.library-conventions")
}

dependencies {
    api("org.seleniumhq.selenium:selenium-java:_")
    testImplementation("com.titusfortner:selenium-logger:_")
}
