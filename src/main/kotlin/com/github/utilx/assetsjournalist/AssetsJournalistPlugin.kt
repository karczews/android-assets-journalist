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

import com.android.build.gradle.AndroidConfig
import com.android.build.gradle.api.AndroidSourceSet
import com.github.utilx.assetsjournalist.java.GenerateJavaFileTask
import com.github.utilx.assetsjournalist.java.JavaFileConfig
import com.github.utilx.assetsjournalist.kotlin.GenerateKotlinFileTask
import com.github.utilx.assetsjournalist.kotlin.KotlinFileConfig
import com.github.utilx.assetsjournalist.xml.GenerateXmlFileTask
import com.github.utilx.assetsjournalist.xml.XmlFileConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register

private const val PRE_BUILD_TASK_NAME = "preBuild"
internal const val ROOT_EXTENSION_NAME = "androidAssetsJournalist"

open class AssetsJournalistPlugin : Plugin<Project> {
    override fun apply(target: Project) = ProjectScopedConfiguration(target).apply()
}

private class ProjectScopedConfiguration(private val project: Project) {
    /**
     * <builddir>/generated/assetsjournalist/src
     */
    private val rootGeneratedBuildDir = project.buildDir.resolve("generated").resolve("assetsjournalist").resolve("src")

    fun apply() {
        val extension = project.extensions.create(ROOT_EXTENSION_NAME, AssetFileGeneratorConfig::class)

        val xmlExtension = extension.xmlFile
        val javaExtension = extension.javaFile
        val kotlinExtension = extension.kotlinFile

        val androidConfig = runCatching { project.extensions.findByType<AndroidConfig>()!! }
            .onFailure {
                throw IllegalStateException(
                    "Failed to locate android plugin extension, " +
                        "make sure plugin is applied after android gradle plugin"
                )
            }
            .getOrThrow()

        // workaround - need to register sourceset here before evaluation
        registerSourceSets(androidConfig)

        project.afterEvaluate {
            extension.sourceSets
                .ifEmpty {
                    runCatching { androidConfig.sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME)!! }
                        .onFailure {
                            throw IllegalStateException("failed to locate ${SourceSet.MAIN_SOURCE_SET_NAME} sourceSet")
                        }
                        .map { listOf(it) }
                        .getOrThrow()
                }.forEach { sourceSet ->

                    if (!xmlExtension.enabled && !javaExtension.enabled && !kotlinExtension.enabled) {
                        project.logger.warn("No file type enabled, enabling java file generation")
                        javaExtension.enabled = true
                    }

                    if (xmlExtension.enabled) {
                        configureXmlTask(xmlExtension, sourceSet)
                    }

                    if (javaExtension.enabled) {
                        configureJavaTask(javaExtension, sourceSet)
                    }

                    if (kotlinExtension.enabled) {
                        configureKotlinTask(kotlinExtension, sourceSet)
                    }
                }
        }
    }

    private fun registerSourceSets(
        androidConfig: AndroidConfig
    ) {

        runCatching { androidConfig.sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME)!! }
            .onSuccess {
                it.java.srcDirs(
                    getGeneratedJavaOutputDirForSourceSet(SourceSet.MAIN_SOURCE_SET_NAME)
                )
            }
            .onSuccess {
                it.java.srcDirs(
                    getGeneratedKotlinOutputDirForSourceSet(SourceSet.MAIN_SOURCE_SET_NAME)
                )
            }.onSuccess {
                it.res.srcDirs(
                    getGeneratedResOutputDirForSourceSet(SourceSet.MAIN_SOURCE_SET_NAME)
                )
            }
    }

    private fun configureXmlTask(
        xmlConfig: XmlFileConfig,
        sourceSet: AndroidSourceSet
    ) {
        //Register new res directory to provided sourceSet so all generated xml files are accessible in the project
        val generatedResDirectory = getGeneratedResOutputDirForSourceSet(sourceSet.name)
        //sourceSet.res.srcDirs(generatedResDirectory)

        val generatedXmlFile = getOutputXmFileForSourceSet(sourceSet.name)

        val task = project.tasks
            .register<GenerateXmlFileTask>("generateAssetsXmlFile${sourceSet.name.capitalize()}") {
                this.sourceSet = sourceSet
                this.outputFile = generatedXmlFile

                configureUsing(xmlConfig)

                project.logger.lifecycle(
                    "Configured xml generation task for [${sourceSet.name}] source set\n" +
                        "Registered new res directory - $generatedResDirectory\n" +
                        "Asset xml file will be generated at $generatedXmlFile"
                )
            }

        project.tasks.named(PRE_BUILD_TASK_NAME).configure {
            dependsOn(task)
        }

    }

    private fun configureJavaTask(
        extension: JavaFileConfig,
        sourceSet: AndroidSourceSet
    ) {
        val outputSrcDir = getGeneratedJavaOutputDirForSourceSet(sourceSet.name)

        val task = project
            .tasks.register<GenerateJavaFileTask>("generateAssetsJavaFile${sourceSet.name.capitalize()}") {
                this.sourceSet = sourceSet
                this.outputSrcDir = outputSrcDir

                configureUsing(extension)

                project.logger.lifecycle(
                    "Configured java generation task for [${sourceSet.name}] source set\n" +
                        "Registered new java source directory - $outputSrcDir"
                )
            }

        project.tasks.named(PRE_BUILD_TASK_NAME).configure {
            dependsOn(task)
        }

    }

    private fun configureKotlinTask(
        extension: KotlinFileConfig,
        sourceSet: AndroidSourceSet
    ) {
        val outputSrcDir = getGeneratedKotlinOutputDirForSourceSet(sourceSet.name)

        val task =
            project.tasks
                .register<GenerateKotlinFileTask>("generateAssetsKotlinFile${sourceSet.name.capitalize()}") {
                    this.sourceSet = sourceSet
                    this.outputSrcDir = outputSrcDir
                    configureUsing(extension)

                    project.logger.lifecycle(
                        "Configured kotlin generation task for [${sourceSet.name}] source set\n" +
                            "Registered new kotlin source directory - $outputSrcDir"
                    )
                }

        project.tasks.named(PRE_BUILD_TASK_NAME).configure {
            dependsOn(task)
        }
    }

    /**
     * Returns java source root directory where files will be generated for given variant.
     *
     * ex. <Project>/build/generated/assetsjournalist/src/<main>/java
     */
    private fun getGeneratedJavaOutputDirForSourceSet(variantName: String) = rootGeneratedBuildDir
        .resolve(variantName).resolve("java")

    /**
     * Returns res directory where files will be generated.
     *
     * ex. <Project>/build/generated/assetsjournalist/src/<main>/res/
     */
    private fun getGeneratedResOutputDirForSourceSet(variantName: String) = rootGeneratedBuildDir
        .resolve(variantName).resolve("res")

    /**
     * Returns path to xml file to be generated.
     *
     * ex. <Project>/build/generated/assetsjournalist/src/<main>/res/values/assets-strings.xml
     */
    private fun getOutputXmFileForSourceSet(variantName: String) = rootGeneratedBuildDir
        .resolve(variantName).resolve("res").resolve("values").resolve("assets-strings.xml")

    /**
     * Returns kotlin source root directory where files will be generated for given variant.
     *
     * ex. <Project>/build/generated/assetsjournalist/src/<main>/kotlin
     */
    private fun getGeneratedKotlinOutputDirForSourceSet(variantName: String) = rootGeneratedBuildDir
        .resolve(variantName).resolve("kotlin")
}
