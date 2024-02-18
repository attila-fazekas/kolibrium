plugins {
    id("kolibrium.detekt-conventions")
    id("kolibrium.kotlin-conventions")
    id("kolibrium.ktlint-conventions")
}

dependencies {
    testImplementation("com.lemonappdev:konsist:_")
    testImplementation(Testing.junit.jupiter.api)
    runtimeOnly(Testing.junit.jupiter.engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.ktlintMainSourceSetCheck {
    dependsOn(tasks.ktlintMainSourceSetFormat)
}

tasks.ktlintTestSourceSetCheck {
    dependsOn(tasks.ktlintTestSourceSetFormat)
}

tasks.ktlintKotlinScriptCheck {
    dependsOn(tasks.ktlintKotlinScriptFormat)
}
