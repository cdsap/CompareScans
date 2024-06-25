import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.20"
    id("io.github.cdsap.fatbinary") version "1.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

fatBinary {
    mainClass = "io.github.cdsap.comparescans.cli.Main"
    name = "comparescans"
}

dependencies {
    implementation(project(":library"))
    implementation("io.github.cdsap:geapi-data:0.2.7")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.3")
    implementation("com.jakewharton.picnic:picnic:0.6.0")
    implementation("com.github.ajalt.clikt:clikt:3.5.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("MainKt")
}
