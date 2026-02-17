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

package com.github.utilx.assetsjournalist

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AssetsJournalistPluginTest {
    @TempDir
    lateinit var tempDir: File

    private val classpath = System.getProperty("java.class.path")
    private val testClasspath = classpath.split(File.pathSeparator.toRegex()).map { File(it) }

    @Test
    @DisplayName("Should register and execute asset generation tasks")
    fun `Should register tasks`() {
        val resourceDirectory = Paths.get("src", "functionalTest", "testProject")

        // Setup the test build
        val projectDir = tempDir

        resourceDirectory.toFile().copyRecursively(projectDir, true)

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withPluginClasspath(runner.pluginClasspath + testClasspath)
        runner.withArguments("assembleFooDebug")
        runner.withProjectDir(projectDir)
        // Clear conflicting Android SDK environment variables to avoid conflicts
        val androidHome = System.getenv("ANDROID_HOME")
        val envMap = System.getenv().toMutableMap()
        envMap["ANDROID_SDK_ROOT"] = ""
        if (androidHome != null) {
            envMap["ANDROID_HOME"] = androidHome
        }
        runner.withEnvironment(envMap)
        val result = runner.build()

        // Verify the result
        assertNotNull(result, "Build result should not be null")
        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":app:assembleFooDebug")?.outcome,
            "assembleFooDebug task should succeed",
        )
    }

    @Test
    @DisplayName("Should generate Kotlin asset files")
    fun `Should generate Kotlin asset files`() {
        val resourceDirectory = Paths.get("src", "functionalTest", "testProject")

        // Setup the test build
        val projectDir = tempDir
        resourceDirectory.toFile().copyRecursively(projectDir, true)

        // Create a test asset file
        val appAssetsDir = File(projectDir, "app/src/main/assets")
        appAssetsDir.mkdirs()
        File(appAssetsDir, "test_asset.txt").writeText("test content")

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withPluginClasspath(runner.pluginClasspath + testClasspath)
        runner.withArguments("generateFooDebugKotlinAssetFiles", "--stacktrace")
        runner.withProjectDir(projectDir)
        // Clear conflicting Android SDK environment variables to avoid conflicts
        val androidHome = System.getenv("ANDROID_HOME")
        val envMap = System.getenv().toMutableMap()
        envMap["ANDROID_SDK_ROOT"] = ""
        if (androidHome != null) {
            envMap["ANDROID_HOME"] = androidHome
        }
        runner.withEnvironment(envMap)
        val result = runner.build()

        // Verify the result
        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":app:generateFooDebugKotlinAssetFiles")?.outcome,
            "Kotlin asset generation task should succeed",
        )

        // Verify generated file exists
        val generatedDir = File(projectDir, "app/build/generated/assetsjournalist/fooDebug/kotlin")
        val generatedFile = File(generatedDir, "com/github/utilx/AssetFilesKotlin.kt")
        assertTrue(generatedFile.exists(), "Generated Kotlin file should exist at ${generatedFile.absolutePath}")

        // Verify generated file content
        val content = generatedFile.readText()
        assertTrue(content.contains("object AssetFilesKotlin"), "Generated file should contain object declaration")
        assertTrue(content.contains("const val"), "Generated file should contain const declarations")
    }

    @Test
    @DisplayName("Should generate Java asset files")
    fun `Should generate Java asset files`() {
        val resourceDirectory = Paths.get("src", "functionalTest", "testProject")

        // Setup the test build
        val projectDir = tempDir
        resourceDirectory.toFile().copyRecursively(projectDir, true)

        // Create a test asset file
        val appAssetsDir = File(projectDir, "app/src/main/assets")
        appAssetsDir.mkdirs()
        File(appAssetsDir, "test_java_asset.txt").writeText("test content")

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withPluginClasspath(runner.pluginClasspath + testClasspath)
        runner.withArguments("generateFooDebugJavaAssetFiles", "--stacktrace")
        runner.withProjectDir(projectDir)
        // Clear conflicting Android SDK environment variables to avoid conflicts
        val androidHome = System.getenv("ANDROID_HOME")
        val envMap = System.getenv().toMutableMap()
        envMap["ANDROID_SDK_ROOT"] = ""
        if (androidHome != null) {
            envMap["ANDROID_HOME"] = androidHome
        }
        runner.withEnvironment(envMap)
        val result = runner.build()

        // Verify the result
        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":app:generateFooDebugJavaAssetFiles")?.outcome,
            "Java asset generation task should succeed",
        )

        // Verify generated file exists
        val generatedDir = File(projectDir, "app/build/generated/assetsjournalist/fooDebug/java")
        val generatedFile = File(generatedDir, "com/github/utilx/AssetFilesJava.java")
        assertTrue(generatedFile.exists(), "Generated Java file should exist at ${generatedFile.absolutePath}")

        // Verify generated file content
        val content = generatedFile.readText()
        assertTrue(content.contains("class AssetFilesJava"), "Generated file should contain class declaration")
        assertTrue(content.contains("public static final String"), "Generated file should contain constant declarations")
    }

    @Test
    @DisplayName("Should generate XML asset files")
    fun `Should generate XML asset files`() {
        val resourceDirectory = Paths.get("src", "functionalTest", "testProject")

        // Setup the test build
        val projectDir = tempDir
        resourceDirectory.toFile().copyRecursively(projectDir, true)

        // Create a test asset file
        val appAssetsDir = File(projectDir, "app/src/main/assets")
        appAssetsDir.mkdirs()
        File(appAssetsDir, "test_xml_asset.txt").writeText("test content")

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withPluginClasspath(runner.pluginClasspath + testClasspath)
        runner.withArguments("generateFooDebugXmlAssetFiles", "--stacktrace")
        runner.withProjectDir(projectDir)
        // Clear conflicting Android SDK environment variables to avoid conflicts
        val androidHome = System.getenv("ANDROID_HOME")
        val envMap = System.getenv().toMutableMap()
        envMap["ANDROID_SDK_ROOT"] = ""
        if (androidHome != null) {
            envMap["ANDROID_HOME"] = androidHome
        }
        runner.withEnvironment(envMap)
        val result = runner.build()

        // Verify the result
        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":app:generateFooDebugXmlAssetFiles")?.outcome,
            "XML asset generation task should succeed",
        )

        // Verify generated file exists
        val generatedDir = File(projectDir, "app/build/generated/assetsjournalist/fooDebug/res/values")
        val generatedFile = File(generatedDir, "asset-strings.xml")
        assertTrue(generatedFile.exists(), "Generated XML file should exist at ${generatedFile.absolutePath}")

        // Verify generated file content
        val content = generatedFile.readText()
        assertTrue(content.contains("<resources>"), "Generated file should contain resources tag")
        assertTrue(content.contains("<string name="), "Generated file should contain string declarations")
    }

    @Test
    @DisplayName("Should handle multiple product flavors")
    fun `Should handle multiple product flavors`() {
        val resourceDirectory = Paths.get("src", "functionalTest", "testProject")

        // Setup the test build
        val projectDir = tempDir
        resourceDirectory.toFile().copyRecursively(projectDir, true)

        // Create test assets for different flavors
        val fooAssetsDir = File(projectDir, "app/src/foo/assets")
        fooAssetsDir.mkdirs()
        File(fooAssetsDir, "foo_asset.txt").writeText("foo content")

        val barAssetsDir = File(projectDir, "app/src/bar/assets")
        barAssetsDir.mkdirs()
        File(barAssetsDir, "bar_asset.txt").writeText("bar content")

        // Run build for both flavors
        val runnerFoo = GradleRunner.create()
        runnerFoo.forwardOutput()
        runnerFoo.withPluginClasspath()
        runnerFoo.withPluginClasspath(runnerFoo.pluginClasspath + testClasspath)
        runnerFoo.withArguments("generateFooDebugKotlinAssetFiles", "--stacktrace")
        runnerFoo.withProjectDir(projectDir)
        // Clear conflicting Android SDK environment variables to avoid conflicts
        val androidHome = System.getenv("ANDROID_HOME")
        val envMap = System.getenv().toMutableMap()
        envMap["ANDROID_SDK_ROOT"] = ""
        if (androidHome != null) {
            envMap["ANDROID_HOME"] = androidHome
        }
        runnerFoo.withEnvironment(envMap)
        val resultFoo = runnerFoo.build()

        val runnerBar = GradleRunner.create()
        runnerBar.forwardOutput()
        runnerBar.withPluginClasspath()
        runnerBar.withPluginClasspath(runnerBar.pluginClasspath + testClasspath)
        runnerBar.withArguments("generateBarDebugKotlinAssetFiles", "--stacktrace")
        runnerBar.withProjectDir(projectDir)
        runnerBar.withEnvironment(envMap)
        val resultBar = runnerBar.build()

        // Verify both builds succeeded
        assertEquals(
            TaskOutcome.SUCCESS,
            resultFoo.task(":app:generateFooDebugKotlinAssetFiles")?.outcome,
            "Foo flavor asset generation should succeed",
        )
        assertEquals(
            TaskOutcome.SUCCESS,
            resultBar.task(":app:generateBarDebugKotlinAssetFiles")?.outcome,
            "Bar flavor asset generation should succeed",
        )

        // Verify both generated files exist
        val fooGeneratedFile = File(projectDir, "app/build/generated/assetsjournalist/fooDebug/kotlin/com/github/utilx/AssetFilesKotlin.kt")
        val barGeneratedFile = File(projectDir, "app/build/generated/assetsjournalist/barDebug/kotlin/com/github/utilx/AssetFilesKotlin.kt")

        assertTrue(fooGeneratedFile.exists(), "Foo flavor generated file should exist")
        assertTrue(barGeneratedFile.exists(), "Bar flavor generated file should exist")
    }

    @Test
    @DisplayName("Should apply regex replacements to asset paths")
    fun `Should apply regex replacements to asset paths`() {
        val resourceDirectory = Paths.get("src", "functionalTest", "testProject")

        // Setup the test build
        val projectDir = tempDir
        resourceDirectory.toFile().copyRecursively(projectDir, true)

        // Create a test asset file with pattern to be replaced
        val appAssetsDir = File(projectDir, "app/src/main/assets")
        appAssetsDir.mkdirs()
        File(appAssetsDir, "aztest.txt").writeText("test content")

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withPluginClasspath(runner.pluginClasspath + testClasspath)
        runner.withArguments("generateFooDebugKotlinAssetFiles", "--stacktrace")
        runner.withProjectDir(projectDir)
        // Clear conflicting Android SDK environment variables to avoid conflicts
        val androidHome = System.getenv("ANDROID_HOME")
        val envMap = System.getenv().toMutableMap()
        envMap["ANDROID_SDK_ROOT"] = ""
        if (androidHome != null) {
            envMap["ANDROID_HOME"] = androidHome
        }
        runner.withEnvironment(envMap)
        val result = runner.build()

        // Verify the result
        assertEquals(TaskOutcome.SUCCESS, result.task(":app:generateFooDebugKotlinAssetFiles")?.outcome)

        // Verify generated file contains replaced path
        val generatedFile = File(projectDir, "app/build/generated/assetsjournalist/fooDebug/kotlin/com/github/utilx/AssetFilesKotlin.kt")
        val content = generatedFile.readText()

        // According to config: [match: '^az', replaceWith: 'replacekt']
        assertTrue(
            content.contains("replacekttest.txt"),
            "Generated file should contain replaced path (^az -> replacekt)",
        )
    }
}
