import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object deps {
    val arrow_version = "0.8.2"
}

plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.10.0"
    kotlin("jvm") version "1.3.11"
}

group = "com.github.utilx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    google()
}

gradlePlugin {
    plugins {
        create("asset-listing-generator") {
            id = "com.github.utilx.asset-file-generator"
            displayName ="Android Assets File Generator Plugin"
            description = "Plugin that generates android assets list to string resources or source code"
            implementationClass = "com.github.utilx.AssetFileGeneratorPlugin"
        }
    }
}

pluginBundle {
    website = "http://github.com/utilx/android-asset-file-generator"
    vcsUrl = "http://github.com/utilx/android-asset-file-generator"
    tags = listOf("android", "assets", "listing", "generator")
}

/*publishing {
    publications {
        create<MavenPublication> ("pluginPublication") {
            groupId = "com.github.utilx"
            artifactId = "assetlistgen"
            version = "1.0"

            from(components["java"])
        }
    }
}*/

dependencies {
    implementation(gradleApi())
    compile("com.android.tools.build:gradle:3.3.1")
    compile(kotlin("stdlib-jdk8"))
    compile("io.arrow-kt:arrow-core:${deps.arrow_version}")
    compile("io.arrow-kt:arrow-data:${deps.arrow_version}")

    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}