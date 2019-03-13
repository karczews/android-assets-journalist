import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.10.1"
}

group = "com.github.utilx"
version = "0.9.8"

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
    website = "http://github.com/karczews/android-assets-journalistr"
    vcsUrl = "http://github.com/karczews/android-assets-journalist"
    tags = listOf("android", "assets", "file", "listing", "generator", "journaling")
}

dependencies {

    implementation(gradleApi())
    implementation("com.squareup:javapoet:1.11.1")
    implementation("com.squareup:kotlinpoet:1.1.0")
    compileOnly("com.android.tools.build:gradle:3.3.1")
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
