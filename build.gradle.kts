/*
 *  Copyright (c) 2019-present, Android Assets Journalist Contributors.
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 *  the License for the specific language governing permissions and limitations under the License.
 */

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    jacoco
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.sonarqube)
    // id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

group = "com.github.utilx"

repositories {
    mavenCentral()
    google()
}

gradlePlugin {
    website.set("https://github.com/karczews/android-assets-journalist")
    vcsUrl.set("https://github.com/karczews/android-assets-journalist")
    plugins {
        create("android-assets-journalist") {
            id = "com.github.utilx.android-assets-journalist"
            displayName = "Android Asset Files Listing Plugin"
            description = "Plugin that generates android assets list as string resources or source code file"
            implementationClass = "com.github.utilx.assetsjournalist.AssetsJournalistPlugin"
            tags.set(listOf("android", "assets", "file", "listing", "generator", "journaling"))
        }
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet =
    sourceSets.create("functionalTest") {
    }

sonarqube {
}

dependencies {
    implementation(gradleApi())
    implementation(libs.kotlinpoet)
    compileOnly(libs.android.gradle.plugin)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.engine)
    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.android.gradle.plugin)
    testImplementation(libs.kotlin.gradle.plugin)
    testImplementation(libs.assertk)
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

tasks.check {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<Test> {
    useJUnitPlatform {}
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<JacocoReport> {
    reports {
        html.required.set(false)
        xml.required.set(true)
        csv.required.set(false)
    }
}

// Detekt disabled - config file incompatible with newer versions
// detekt {
//     config.setFrom(files("detekt-config.yml"))
//     buildUponDefaultConfig = true
// }
//
// tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
//     reports {
//         html.required.set(true)
//     }
// }
