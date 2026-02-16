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
        const val version = "5.10.2"

        const val juniperApi = "org.junit.jupiter:junit-jupiter-api:$version"
        const val juniperParams = "org.junit.jupiter:junit-jupiter-params:$version"
        const val juniperEngine = "org.junit.jupiter:junit-jupiter-engine:$version"
        const val vintageEngine = "org.junit.vintage:junit-vintage-engine:$version"

        object PlatformLauncher {
            const val version = "1.10.2"
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

// =============================================================================
// SECURITY FIXES - Transitive Dependency Updates
// =============================================================================
// The following resolution strategy forces updates to transitive dependencies
// that have known security vulnerabilities. These dependencies are pulled in
// by Android Gradle Plugin 8.8.0 and would otherwise remain vulnerable.
//
// Last updated: 2026-02-16
// Total vulnerabilities addressed: 23 (7 High, 15 Medium, 1 Low)
//
// To check current dependency versions:
//   ./gradlew dependencies --configuration testRuntimeClasspath
//
// To verify security fixes are applied:
//   ./gradlew dependencies --configuration testRuntimeClasspath | grep <package>
// =============================================================================

configurations.all {
    resolutionStrategy {
        // -------------------------------------------------------------------------
        // HIGH SEVERITY FIXES
        // -------------------------------------------------------------------------

        // CVE-2024-29371 / GHSA-3677-xxcr-wjqv
        // Package: org.bitbucket.b_c:jose4j
        // Issue: DoS via compressed JWE content (zip bomb attack)
        // CVSS Score: 7.5
        // Affected: < 0.9.6
        force("org.bitbucket.b_c:jose4j:0.9.6")

        // GHSA-735f-pc8j-v9w8
        // Package: com.google.protobuf:protobuf-java
        // Issue: Potential Denial of Service
        // Affected: < 3.25.5
        force("com.google.protobuf:protobuf-java:3.25.5")

        // GHSA-78wr-2p64-hpwj
        // Package: commons-io:commons-io
        // Issue: XmlStreamReader DoS on untrusted input
        // Affected: < 2.14.0
        force("commons-io:commons-io:2.14.0")

        // GHSA-2363-cqg2-863c
        // Package: org.jdom:jdom2
        // Issue: XML External Entity (XXE) Injection
        // Affected: < 2.0.6.1
        force("org.jdom:jdom2:2.0.6.1")

        // -------------------------------------------------------------------------
        // NETTY - Multiple CVEs (9 vulnerabilities)
        // ------------------------------------------------------------------------
        // High Severity:
        //   - GHSA-4g8c-wm8x-jfhw: SslHandler validation issue
        //   - GHSA-prj3-ccx8-p6x4: HTTP/2 DDoS vulnerability (MadeYouReset)
        //   - GHSA-xpw8-rcwv-8f8p: HTTP/2 Rapid Reset Attack
        // Medium Severity:
        //   - GHSA-389x-839f-4rhx, GHSA-xq3w-v528-46rv: DoS on Windows
        //   - GHSA-3p8m-j85q-pgmj: Zip bomb style attack
        //   - GHSA-6mjq-h674-j845: SniHandler 16MB allocation issue
        //   - GHSA-5jpm-x58v-624v: HttpPostRequestDecoder OOM
        //   - GHSA-84h7-rjj3-6jx4: CRLF Injection vulnerability
        // Low Severity:
        //   - GHSA-fghv-69vj-qj49: Request smuggling via chunk extensions
        // Affected: < 4.1.115.Final
        force("io.netty:netty-common:4.1.115.Final")
        force("io.netty:netty-buffer:4.1.115.Final")
        force("io.netty:netty-codec:4.1.115.Final")
        force("io.netty:netty-codec-http:4.1.115.Final")
        force("io.netty:netty-codec-http2:4.1.115.Final")
        force("io.netty:netty-handler:4.1.115.Final")
        force("io.netty:netty-handler-proxy:4.1.115.Final")
        force("io.netty:netty-resolver:4.1.115.Final")
        force("io.netty:netty-transport:4.1.115.Final")
        force("io.netty:netty-transport-native-unix-common:4.1.115.Final")

        // -------------------------------------------------------------------------
        // BOUNCYCASTLE - Multiple CVEs (6 vulnerabilities)
        // -------------------------------------------------------------------------
        // Medium Severity:
        //   - GHSA-4cx2-fc23-5wg6: Excessive allocation in bcpkix-jdk18on
        //   - GHSA-4h8f-2wvx-gg5w: DNS poisoning vulnerability
        //   - GHSA-67mf-3cr5-8w23: Excessive allocation in bcprov-jdk18on
        //   - GHSA-8xfc-gm6g-vgpv: Certificate parsing high CPU usage
        //   - GHSA-m44j-cfrm-g8qc: Infinite loop with crafted signature
        //   - GHSA-v435-xc8x-wvr9: RSA timing side-channel (Marvin Attack)
        // Affected: < 1.80
        force("org.bouncycastle:bcprov-jdk18on:1.80")
        force("org.bouncycastle:bcpkix-jdk18on:1.80")
        force("org.bouncycastle:bcutil-jdk18on:1.80")

        // -------------------------------------------------------------------------
        // MEDIUM SEVERITY FIXES
        // -------------------------------------------------------------------------

        // Apache Commons Compress - 2 CVEs
        //   - GHSA-4265-ccf5-phj5: OutOfMemoryError unpacking broken Pack200 file
        //   - GHSA-4g9r-vxhx-9pgx: Infinite loop for corrupted DUMP file
        // Affected: < 1.26.0
        force("org.apache.commons:commons-compress:1.26.0")

        // GHSA-j288-q9x7-2f5v
        // Package: org.apache.commons:commons-lang3
        // Issue: Uncontrolled Recursion when processing long inputs
        // Affected: < 3.14.0
        force("org.apache.commons:commons-lang3:3.14.0")
    }
}

// =============================================================================
// END SECURITY FIXES
// =============================================================================

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
