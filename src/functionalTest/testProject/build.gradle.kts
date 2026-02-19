// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    // Plugins are applied via settings.gradle.kts or the test framework
}

repositories {
    google()
    mavenCentral()
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
