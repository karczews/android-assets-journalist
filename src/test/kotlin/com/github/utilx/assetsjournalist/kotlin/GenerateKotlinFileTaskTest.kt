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

package com.github.utilx.assetsjournalist.kotlin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GenerateKotlinFileTaskTest {
    private val project = ProjectBuilder.builder().build()

    @TempDir
    lateinit var tempDir: File

    @Nested
    @DisplayName("Configuration Tests")
    inner class Configuration {
        @Test
        @DisplayName("Should configure task using KotlinFileConfig")
        fun shouldConfigureTaskUsingConfig() {
            // given
            val task = project.tasks.create("testTask", GenerateKotlinFileTask::class.java)
            val config =
                KotlinFileConfig().apply {
                    enabled = true
                    className = "TestAssetFiles"
                    packageName = "com.test.assets"
                    constNamePrefix = "TEST_PREFIX_"
                    constValuePrefix = "test_value_"
                    replaceInAssetsPath =
                        listOf(
                            mapOf("match" to "^test", "replaceWith" to "prod"),
                        )
                }

            // when
            task.configureUsing(config)

            // then
            assertEquals("TestAssetFiles", task.className.get())
            assertEquals("com.test.assets", task.packageName.get())
            assertEquals("TEST_PREFIX_", task.constNamePrefix.get())
            assertEquals("test_value_", task.constValuePrefix.get())
            assertEquals(1, task.constValueReplacementExpressions.get().size)
        }

        @Test
        @DisplayName("Should have default values")
        fun shouldHaveDefaultValues() {
            // given
            val task = project.tasks.create("testTask", GenerateKotlinFileTask::class.java)

            // when/then
            assertEquals("AssetFileKt", task.className.get())
            assertEquals("", task.packageName.get())
            assertEquals("", task.constNamePrefix.get())
            assertEquals("", task.constValuePrefix.get())
        }

        @Test
        @DisplayName("Should configure empty replacement expressions")
        fun shouldConfigureEmptyReplacementExpressions() {
            // given
            val task = project.tasks.create("testTask", GenerateKotlinFileTask::class.java)
            val config =
                KotlinFileConfig().apply {
                    replaceInAssetsPath = emptyList()
                }

            // when
            task.configureUsing(config)

            // then
            assertTrue(task.constValueReplacementExpressions.get().isEmpty())
        }
    }

    @Nested
    @DisplayName("File Generation Tests")
    inner class FileGeneration {
        @BeforeEach
        fun setUp() {
            // Create test asset files
            File(tempDir, "test.txt").createNewFile()
            File(tempDir, "subdir").mkdir()
            File(tempDir, "subdir/test2.txt").createNewFile()
        }

        @Test
        @DisplayName("Should generate Kotlin file with assets")
        fun shouldGenerateKotlinFileWithAssets() {
            // given
            val outputDir = File(tempDir, "output")
            outputDir.mkdirs()

            val task = project.tasks.create("testTask", GenerateKotlinFileTask::class.java)
            task.outputSrcDir.set(outputDir)
            task.className.set("TestAssets")
            task.packageName.set("com.test")
            task.constNamePrefix.set("ASSET_")
            task.constValuePrefix.set("")
            task.constValueReplacementExpressions.set(emptyList())
            task.assetFiles.from(tempDir)

            // when
            task.generateKotlinFile()

            // then
            val generatedFile = File(outputDir, "com/test/TestAssets.kt")
            assertTrue(generatedFile.exists(), "Generated file should exist")

            val content = generatedFile.readText()
            assertTrue(content.contains("package com.test"))
            assertTrue(content.contains("object TestAssets"))
            assertTrue(content.contains("const val"))
        }

        @Test
        @DisplayName("Should generate file with value prefix")
        fun shouldGenerateFileWithValuePrefix() {
            // given
            val outputDir = File(tempDir, "output")
            outputDir.mkdirs()

            val task = project.tasks.create("testTask", GenerateKotlinFileTask::class.java)
            task.outputSrcDir.set(outputDir)
            task.className.set("TestAssets")
            task.packageName.set("com.test")
            task.constValuePrefix.set("asset://")
            task.constValueReplacementExpressions.set(emptyList())
            task.assetFiles.from(tempDir)

            // when
            task.generateKotlinFile()

            // then
            val generatedFile = File(outputDir, "com/test/TestAssets.kt")
            val content = generatedFile.readText()
            assertTrue(content.contains("asset://"), "Content should contain value prefix")
        }

        @Test
        @DisplayName("Should apply value replacements")
        fun shouldApplyValueReplacements() {
            // given
            val assetDir = File(tempDir, "assets")
            assetDir.mkdirs()
            File(assetDir, "test_file.txt").createNewFile()

            val outputDir = File(tempDir, "output")
            outputDir.mkdirs()

            val task = project.tasks.create("testTask", GenerateKotlinFileTask::class.java)
            task.outputSrcDir.set(outputDir)
            task.className.set("TestAssets")
            task.packageName.set("com.test")
            task.constValueReplacementExpressions.set(
                listOf(mapOf("match" to "test", "replaceWith" to "prod")),
            )
            task.assetFiles.from(assetDir)

            // when
            task.generateKotlinFile()

            // then
            val generatedFile = File(outputDir, "com/test/TestAssets.kt")
            val content = generatedFile.readText()
            assertTrue(content.contains("prod_file.txt"), "Content should contain replaced value")
            assertFalse(content.contains("test_file.txt"), "Content should not contain original value")
        }

        @Test
        @DisplayName("Should handle empty asset directory")
        fun shouldHandleEmptyAssetDirectory() {
            // given
            val emptyDir = File(tempDir, "empty")
            emptyDir.mkdirs()

            val outputDir = File(tempDir, "output")
            outputDir.mkdirs()

            val task = project.tasks.create("testTask", GenerateKotlinFileTask::class.java)
            task.outputSrcDir.set(outputDir)
            task.className.set("EmptyAssets")
            task.packageName.set("com.test")
            task.constValueReplacementExpressions.set(emptyList())
            task.assetFiles.from(emptyDir)

            // when
            task.generateKotlinFile()

            // then
            val generatedFile = File(outputDir, "com/test/EmptyAssets.kt")
            assertTrue(generatedFile.exists(), "Generated file should exist even with no assets")

            val content = generatedFile.readText()
            assertTrue(content.contains("object EmptyAssets"))
            assertTrue(content.contains("package com.test"))
        }

        @Test
        @DisplayName("Should handle assets with special characters in names")
        fun shouldHandleSpecialCharactersInAssetNames() {
            // given
            val assetDir = File(tempDir, "assets")
            assetDir.mkdirs()
            File(assetDir, "test-file.txt").createNewFile()
            File(assetDir, "test file.txt").createNewFile()
            File(assetDir, "test.file.txt").createNewFile()

            val outputDir = File(tempDir, "output")
            outputDir.mkdirs()

            val task = project.tasks.create("testTask", GenerateKotlinFileTask::class.java)
            task.outputSrcDir.set(outputDir)
            task.className.set("SpecialAssets")
            task.packageName.set("com.test")
            task.constValueReplacementExpressions.set(emptyList())
            task.assetFiles.from(assetDir)

            // when
            task.generateKotlinFile()

            // then
            val generatedFile = File(outputDir, "com/test/SpecialAssets.kt")
            assertTrue(generatedFile.exists())

            val content = generatedFile.readText()
            // Special characters should be replaced with underscores in constant names
            assertTrue(content.contains("const val"))
            assertTrue(content.contains("test-file.txt"))
            assertTrue(content.contains("test file.txt"))
            assertTrue(content.contains("test.file.txt"))
        }

        @Test
        @DisplayName("Should handle nested directory structure")
        fun shouldHandleNestedDirectoryStructure() {
            // given
            val assetDir = File(tempDir, "assets")
            val subdir1 = File(assetDir, "level1")
            val subdir2 = File(subdir1, "level2")
            subdir2.mkdirs()

            File(assetDir, "root.txt").createNewFile()
            File(subdir1, "file1.txt").createNewFile()
            File(subdir2, "file2.txt").createNewFile()

            val outputDir = File(tempDir, "output")
            outputDir.mkdirs()

            val task = project.tasks.create("testTask", GenerateKotlinFileTask::class.java)
            task.outputSrcDir.set(outputDir)
            task.className.set("NestedAssets")
            task.packageName.set("com.test")
            task.constValueReplacementExpressions.set(emptyList())
            task.assetFiles.from(assetDir)

            // when
            task.generateKotlinFile()

            // then
            val generatedFile = File(outputDir, "com/test/NestedAssets.kt")
            val content = generatedFile.readText()

            assertTrue(content.contains("root.txt"))
            assertTrue(content.contains("level1/file1.txt") || content.contains("level1\\file1.txt"))
            assertTrue(content.contains("level1/level2/file2.txt") || content.contains("level1\\level2\\file2.txt"))
        }

        @Test
        @DisplayName("Should generate file with empty package name")
        fun shouldGenerateFileWithEmptyPackageName() {
            // given
            val assetDir = File(tempDir, "assets")
            assetDir.mkdirs()
            File(assetDir, "test.txt").createNewFile()

            val outputDir = File(tempDir, "output")
            outputDir.mkdirs()

            val task = project.tasks.create("testTask", GenerateKotlinFileTask::class.java)
            task.outputSrcDir.set(outputDir)
            task.className.set("RootAssets")
            task.packageName.set("")
            task.constValueReplacementExpressions.set(emptyList())
            task.assetFiles.from(assetDir)

            // when
            task.generateKotlinFile()

            // then
            val generatedFile = File(outputDir, "RootAssets.kt")
            assertTrue(generatedFile.exists(), "Generated file should exist at root level")

            val content = generatedFile.readText()
            assertFalse(content.contains("package "), "Content should not contain package declaration")
            assertTrue(content.contains("object RootAssets"))
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    inner class EdgeCases {
        @Test
        @DisplayName("Should handle multiple replacement expressions")
        fun shouldHandleMultipleReplacementExpressions() {
            // given
            val assetDir = File(tempDir, "assets")
            assetDir.mkdirs()
            File(assetDir, "abc_xyz.txt").createNewFile()

            val outputDir = File(tempDir, "output")
            outputDir.mkdirs()

            val task = project.tasks.create("testTask", GenerateKotlinFileTask::class.java)
            task.outputSrcDir.set(outputDir)
            task.className.set("MultiReplaceAssets")
            task.packageName.set("com.test")
            task.constValueReplacementExpressions.set(
                listOf(
                    mapOf("match" to "abc", "replaceWith" to "def"),
                    mapOf("match" to "xyz", "replaceWith" to "123"),
                ),
            )
            task.assetFiles.from(assetDir)

            // when
            task.generateKotlinFile()

            // then
            val generatedFile = File(outputDir, "com/test/MultiReplaceAssets.kt")
            val content = generatedFile.readText()
            assertTrue(content.contains("def_123.txt"), "Both replacements should be applied")
        }

        @Test
        @DisplayName("Should handle assets with Unicode characters")
        fun shouldHandleUnicodeCharactersInAssetNames() {
            // given
            val assetDir = File(tempDir, "assets")
            assetDir.mkdirs()
            File(assetDir, "file_\u4E2D\u6587.txt").createNewFile() // Chinese characters

            val outputDir = File(tempDir, "output")
            outputDir.mkdirs()

            val task = project.tasks.create("testTask", GenerateKotlinFileTask::class.java)
            task.outputSrcDir.set(outputDir)
            task.className.set("UnicodeAssets")
            task.packageName.set("com.test")
            task.constValueReplacementExpressions.set(emptyList())
            task.assetFiles.from(assetDir)

            // when
            task.generateKotlinFile()

            // then
            val generatedFile = File(outputDir, "com/test/UnicodeAssets.kt")
            assertTrue(generatedFile.exists())
            val content = generatedFile.readText()
            assertTrue(content.contains("const val"))
        }
    }
}
