plugins {
    id("com.google.devtools.ksp")
    id("kolibrium.kotlin-conventions")
}

dependencies {
    implementation("com.google.auto.service:auto-service-annotations:_")
    implementation("com.google.devtools.ksp:symbol-processing-api:_")
    implementation("com.squareup:kotlinpoet:_")
    implementation("com.squareup:kotlinpoet-ksp:_")
    api("dev.zacsweers.autoservice:auto-service-ksp:_")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:_")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:_")
}
