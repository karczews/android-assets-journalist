import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Dependencies {
    const val javaPoet = "com.squareup:javapoet:1.12.1"
    const val kotlinPoet = "com.squareup:kotlinpoet:1.5.0"
    const val mockk = "io.mockk:mockk:1.9.3"
    const val assertk = "com.willowtreeapps.assertk:assertk-jvm:0.21"

    object Kotlin {
        const val version = "2.0.21"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
        const val test = "org.jetbrains.kotlin:kotlin-test"
        const val junit = "org.jetbrains.kotlin:kotlin-test-junit"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"
        const val plugin = "kotlin"
    }

    object JUnit5 {
        const val version = "5.3.2"

        const val juniperApi = "org.junit.jupiter:junit-jupiter-api:$version"
        const val juniperParams = "org.junit.jupiter:junit-jupiter-params:$version"
        const val juniperEngine = "org.junit.jupiter:junit-jupiter-engine:$version"
        const val vintageEngine = "org.junit.vintage:junit-vintage-engine:$version"

        object PlatformLauncher {
            const val version = "1.1.0"
            const val lib = "org.junit.platform:junit-platform-launcher:$version"
        }
    }

    object Android {
        const val gradleBuildTools = "com.android.tools.build:gradle:8.8.0"
    }
}

plugins {
    kotlin("jvm") version "2.0.21"
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    jacoco
    id("com.gradle.plugin-publish") version "1.3.1"
    id("org.sonarqube") version "6.0.1.5171"
    // id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

group = "com.github.utilx"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

gradlePlugin {
    website.set("http://github.com/karczews/android-assets-journalist")
    vcsUrl.set("http://github.com/karczews/android-assets-journalist")
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
    implementation(Dependencies.javaPoet)
    implementation(Dependencies.kotlinPoet)
    compileOnly(Dependencies.Android.gradleBuildTools)
    implementation(kotlin("stdlib"))

    testImplementation(Dependencies.Kotlin.test)
    testImplementation(Dependencies.JUnit5.juniperApi)
    testImplementation(Dependencies.JUnit5.juniperEngine)
    testImplementation(Dependencies.JUnit5.PlatformLauncher.lib)
    testImplementation(Dependencies.mockk)
    testImplementation(Dependencies.Android.gradleBuildTools)
    testImplementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    testImplementation(Dependencies.assertk)
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
