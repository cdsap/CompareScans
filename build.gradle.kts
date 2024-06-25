plugins {
    kotlin("jvm") version "1.9.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
}
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
