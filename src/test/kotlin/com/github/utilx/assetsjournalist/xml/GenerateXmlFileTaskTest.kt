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

package com.github.utilx.assetsjournalist.xml

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.math.absoluteValue
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GenerateXmlFileTaskTest {
    private val project = ProjectBuilder.builder().build()

    @TempDir
    lateinit var tempDir: File

    private fun createTask(
        assetDir: File,
        stringNamePrefix: String = "",
        stringNameCharMapping: List<Map<String, String>> = emptyList(),
    ): GenerateXmlFileTask {
        val outputDir = File(tempDir, "output")
        outputDir.mkdirs()

        return project.tasks.create("testTask", GenerateXmlFileTask::class.java).apply {
            this.outputFile.set(File(outputDir, "assets-strings.xml"))
            this.outputSrcDir.set(outputDir)
            this.stringNamePrefix.set(stringNamePrefix)
            this.stringNameCharMapping.set(stringNameCharMapping)
            this.assetFiles.from(assetDir)
        }
    }

    private fun assetDirContaining(vararg fileNames: String): File {
        val assetDir = File(tempDir, "assets")
        assetDir.mkdirs()
        fileNames.forEach { name ->
            val file = File(assetDir, name)
            file.parentFile?.mkdirs()
            file.createNewFile()
        }
        return assetDir
    }

    @Nested
    @DisplayName("Configuration Tests")
    inner class Configuration {
        @Test
        @DisplayName("Should configure task using XmlFileConfig")
        fun shouldConfigureTaskUsingConfig() {
            // given
            val task = project.tasks.create("testTask", GenerateXmlFileTask::class.java)
            val config =
                XmlFileConfig().apply {
                    enabled = true
                    stringNamePrefix = "asset_"
                    stringNameCharMapping =
                        listOf(
                            mapOf("match" to "/", "replaceWith" to "__"),
                        )
                }

            // when
            task.configureUsing(config)

            // then
            assertEquals("asset_", task.stringNamePrefix.get())
            assertEquals(1, task.stringNameCharMapping.get().size)
        }
    }

    @Nested
    @DisplayName("String Name Char Mapping Tests")
    inner class StringNameCharMapping {
        @Test
        @DisplayName("Should apply configured char mapping to generated string names")
        fun shouldApplyCharMapping() {
            // given
            val assetDir = assetDirContaining("configs/settings.json")
            val task =
                createTask(
                    assetDir,
                    stringNameCharMapping = listOf(mapOf("match" to "/", "replaceWith" to "__")),
                )

            // when
            task.generateXml()

            // then
            val content = task.outputFile.asFile.get().readText()
            assertTrue(
                content.contains("configs__settings_json"),
                "Separator should be replaced by the configured mapping, was: $content",
            )
        }

        @Test
        @DisplayName("Should apply multiple mappings in configured order")
        fun shouldApplyMultipleMappings() {
            // given
            val assetDir = assetDirContaining("dev_model.tflite")
            val task =
                createTask(
                    assetDir,
                    stringNameCharMapping =
                        listOf(
                            mapOf("match" to "^dev", "replaceWith" to "prod"),
                            mapOf("match" to "tflite", "replaceWith" to "model"),
                        ),
                )

            // when
            task.generateXml()

            // then
            val content = task.outputFile.asFile.get().readText()
            assertTrue(content.contains("prod_model_model"), "Both mappings should apply, was: $content")
            assertFalse(content.contains("name=\"dev_model_tflite"), "Original name should not survive")
        }

        @Test
        @DisplayName("Should still sanitize characters the mapping leaves behind")
        fun shouldSanitizeRemainingCharacters() {
            // given
            val assetDir = assetDirContaining("my-file.txt")
            val task =
                createTask(
                    assetDir,
                    stringNameCharMapping = listOf(mapOf("match" to "-", "replaceWith" to "_dash_")),
                )

            // when
            task.generateXml()

            // then
            val content = task.outputFile.asFile.get().readText()
            // hyphen consumed by the mapping, dot still replaced by the default sanitizer
            assertTrue(content.contains("my_dash_file_txt"), "was: $content")
        }

        @Test
        @DisplayName("Should leave names unchanged when no mapping is configured")
        fun shouldNotChangeNamesWithoutMapping() {
            // given
            val assetDir = assetDirContaining("configs/settings.json")
            val task = createTask(assetDir)

            // when
            task.generateXml()

            // then
            val content = task.outputFile.asFile.get().readText()
            assertTrue(content.contains("configs_settings_json"), "was: $content")
        }

        @Test
        @DisplayName("Should derive hashcode suffix from the untransformed path")
        fun shouldHashUntransformedPath() {
            // given
            val path = "configs/settings.json"
            val assetDir = assetDirContaining(path)
            val task =
                createTask(
                    assetDir,
                    stringNameCharMapping = listOf(mapOf("match" to "configs", "replaceWith" to "cfg")),
                )

            // when
            task.generateXml()

            // then - suffix must match the original path so the mapping cannot introduce collisions
            val content = task.outputFile.asFile.get().readText()
            assertTrue(
                content.contains("cfg_settings_json_${path.hashCode().absoluteValue}"),
                "was: $content",
            )
        }

        @Test
        @DisplayName("Should not let a mapping produce a name starting with a digit")
        fun shouldGuardAgainstLeadingDigitFromMapping() {
            // given - a resource name is a field on R, so it cannot start with a digit
            val assetDir = assetDirContaining("config.json")
            val task =
                createTask(
                    assetDir,
                    stringNameCharMapping = listOf(mapOf("match" to "^config", "replaceWith" to "1config")),
                )

            // when
            task.generateXml()

            // then
            val content = task.outputFile.asFile.get().readText()
            assertTrue(content.contains("name=\"_1config_json"), "was: $content")
        }

        @Test
        @DisplayName("Should not let a numeric asset name produce a name starting with a digit")
        fun shouldGuardAgainstLeadingDigitFromAssetName() {
            // given
            val assetDir = assetDirContaining("1file.txt")
            val task = createTask(assetDir)

            // when
            task.generateXml()

            // then
            val content = task.outputFile.asFile.get().readText()
            assertTrue(content.contains("name=\"_1file_txt"), "was: $content")
        }

        @Test
        @DisplayName("Should not add a guard when the prefix already starts with a letter")
        fun shouldNotGuardWhenPrefixMakesNameValid() {
            // given
            val assetDir = assetDirContaining("1file.txt")
            val task = createTask(assetDir, stringNamePrefix = "asset_")

            // when
            task.generateXml()

            // then
            val content = task.outputFile.asFile.get().readText()
            assertTrue(content.contains("name=\"asset_1file_txt"), "was: $content")
            assertFalse(content.contains("name=\"_asset_"), "Guard should not fire, was: $content")
        }

        @Test
        @DisplayName("Should apply prefix after the mapping")
        fun shouldApplyPrefixAfterMapping() {
            // given
            val assetDir = assetDirContaining("settings.json")
            val task =
                createTask(
                    assetDir,
                    stringNamePrefix = "asset_",
                    stringNameCharMapping = listOf(mapOf("match" to "settings", "replaceWith" to "config")),
                )

            // when
            task.generateXml()

            // then
            val content = task.outputFile.asFile.get().readText()
            assertTrue(content.contains("name=\"asset_config_json"), "was: $content")
        }
    }

    @Nested
    @DisplayName("File Generation Tests")
    inner class FileGeneration {
        @Test
        @DisplayName("Should generate XML containing asset paths as values")
        fun shouldGenerateXmlWithAssetPaths() {
            // given
            val assetDir = assetDirContaining("settings.json")
            val task = createTask(assetDir)

            // when
            task.generateXml()

            // then
            val content = task.outputFile.asFile.get().readText()
            assertTrue(content.contains("<resources>"))
            assertTrue(content.contains(">settings.json<"), "Value should be the raw asset path, was: $content")
        }

        @Test
        @DisplayName("Should emit one entry when several asset dirs share a relative path")
        fun shouldDeduplicateAssetsSharedAcrossSourceDirs() {
            // given - mirrors src/main/assets and src/foo/assets both holding settings.json
            val mainAssets = File(tempDir, "main").apply { mkdirs() }
            File(mainAssets, "settings.json").createNewFile()
            val flavorAssets = File(tempDir, "foo").apply { mkdirs() }
            File(flavorAssets, "settings.json").createNewFile()

            val outputDir = File(tempDir, "output").apply { mkdirs() }
            val task =
                project.tasks.create("testTask", GenerateXmlFileTask::class.java).apply {
                    outputFile.set(File(outputDir, "assets-strings.xml"))
                    outputSrcDir.set(outputDir)
                    stringNamePrefix.set("")
                    stringNameCharMapping.set(emptyList())
                    assetFiles.from(mainAssets, flavorAssets)
                }

            // when
            task.generateXml()

            // then - a duplicate resource name would fail the resource merger
            val content = task.outputFile.asFile.get().readText()
            assertEquals(
                1,
                Regex("<string ").findAll(content).count(),
                "Expected a single string entry, was: $content",
            )
        }

        @Test
        @DisplayName("Should generate empty resources element for empty asset dir")
        fun shouldHandleEmptyAssetDirectory() {
            // given
            val emptyDir = File(tempDir, "empty").apply { mkdirs() }
            val task = createTask(emptyDir)

            // when
            task.generateXml()

            // then
            val content = task.outputFile.asFile.get().readText()
            assertTrue(content.contains("resources"))
            assertFalse(content.contains("<string"))
        }
    }
}
