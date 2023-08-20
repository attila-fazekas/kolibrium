plugins {
    id("kolibrium.base-conventions")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("com.google.auto.service:auto-service-annotations:_")
    implementation("com.google.devtools.ksp:symbol-processing-api:_")
    implementation("com.squareup:kotlinpoet:_")
    implementation("com.squareup:kotlinpoet-ksp:_")
    implementation("commons-validator:commons-validator:_")
    ksp("dev.zacsweers.autoservice:auto-service-ksp:_")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:_")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:_")
}