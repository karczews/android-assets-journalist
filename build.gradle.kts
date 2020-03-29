import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Dependencies {
    const val javaPoet = "com.squareup:javapoet:1.12.1"
    const val kotlinPoet = "com.squareup:kotlinpoet:1.5.0"
    const val mockk = "io.mockk:mockk:1.9.3"
    const val assertk = "com.willowtreeapps.assertk:assertk-jvm:0.21"

    object Kotlin {
        const val version = "1.3.51"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val test = "org.jetbrains.kotlin:kotlin-test:$version"
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
        const val gradleBuildTools = "com.android.tools.build:gradle:3.5.3"
    }
}

plugins {
    kotlin("jvm") version "1.3.41"
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("jacoco")
    id("com.gradle.plugin-publish") version "0.10.1"
    id("org.sonarqube") version "2.7.1"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC14"
}

group = "com.github.utilx"
version = "0.11.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    google()
}

gradlePlugin {
    plugins {
        create("android-assets-journalist") {
            id = "com.github.utilx.android-assets-journalist"
            displayName ="Android Asset Files Listing Plugin"
            description = "Plugin that generates android assets list as string resources or source code file"
            implementationClass = "com.github.utilx.assetsjournalist.AssetsJournalistPlugin"
        }
    }
}

pluginBundle {
    website = "http://github.com/karczews/android-assets-journalist"
    vcsUrl = "http://github.com/karczews/android-assets-journalist"
    tags = listOf("android", "assets", "file", "listing", "generator", "journaling")
}

sonarqube {
    
}

dependencies {

    implementation(gradleApi())
    implementation(Dependencies.javaPoet)
    implementation(Dependencies.kotlinPoet)
    compileOnly(Dependencies.Android.gradleBuildTools)
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(Dependencies.Kotlin.test)
    testImplementation(Dependencies.JUnit5.juniperApi)
    testImplementation(Dependencies.JUnit5.juniperEngine)
    testImplementation(Dependencies.JUnit5.PlatformLauncher.lib)
    testImplementation(Dependencies.mockk)
    testImplementation(Dependencies.Android.gradleBuildTools)
    testImplementation(Dependencies.assertk)

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform {}
    testLogging {
        events = setOf ( TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED )
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<JacocoReport> {
    reports {
        html.isEnabled = false
        xml.isEnabled = true
        csv.isEnabled = false
    }
}


detekt {
    config = files("detekt-config.yml")
    reports {
        html {
            enabled = true
        }
    }
}
