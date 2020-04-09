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

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.github.utilx.assetsjournalist.java.GenerateJavaFileTask
import com.github.utilx.assetsjournalist.java.JavaFileConfig
import com.github.utilx.assetsjournalist.kotlin.GenerateKotlinFileTask
import com.github.utilx.assetsjournalist.kotlin.KotlinFileConfig
import com.github.utilx.assetsjournalist.xml.GenerateXmlFileTask
import com.github.utilx.assetsjournalist.xml.XmlFileConfig
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.gradle.util.GradleVersion
import java.io.File

internal const val MIN_GRADLE_VERSION = "5.3"

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
        if (GradleVersion.current() < GradleVersion.version(MIN_GRADLE_VERSION)) {
            throw GradleException(
                "Current gradle versions is ${GradleVersion.current()}, " +
                    "AssetsJournalistPlugin supports only gradle $MIN_GRADLE_VERSION+"
            )
        }

        if (project.extensions.findByType<BaseExtension>() == null) {
            throw GradleException("Failed to locate android plugin extension, make sure plugin is applied after android gradle plugin")
        }

        val extension = project.extensions.create(ROOT_EXTENSION_NAME, AssetFileGeneratorConfig::class)
        val xmlExtension = extension.xmlFile
        val javaExtension = extension.javaFile
        val kotlinExtension = extension.kotlinFile

        project.afterEvaluate {
            buildVariants.configureEach {
                if (!xmlExtension.enabled && !javaExtension.enabled && !kotlinExtension.enabled) {
                    project.logger.warn("No file type enabled, enabling java file generation")
                    javaExtension.enabled = true
                }

                if (xmlExtension.enabled) {
                    configureXmlTask(xmlExtension, this)
                }

                if (javaExtension.enabled) {
                    configureJavaTask(javaExtension, this)
                }

                if (kotlinExtension.enabled) {
                    configureKotlinTask(kotlinExtension, this)
                }

            }
        }
    }

    private fun configureXmlTask(
        xmlConfig: XmlFileConfig,
        variant: BaseVariant
    ) {
        val task = project.tasks
            .register<GenerateXmlFileTask>("generateAssetsXmlFile${variant.name.capitalize()}") {

                //Register new res directory to provided sourceSet so all generated xml files are accessible in the project
                val generatedResDirectory = getGeneratedResOutputDirForVariant(variant.name)
                val generatedXmlFile = getOutputXmFileForVariant(variant.name)

                assetFiles.setFrom(variant.assetDirs())

                outputFile.set(generatedXmlFile)
                configureUsing(xmlConfig)

                project.logger.debug(
                    "Configured xml generation task for [${variant.name}] variant\n" +
                        "Registered new res directory - $generatedResDirectory\n" +
                        "Asset xml file will be generated at $generatedXmlFile"
                )
            }

        // Have to configure task here until agp supports task provider https://issuetracker.google.com/issues/150799913
        variant.registerGeneratedResFolders(task.get().outputs.files)
    }

    private fun configureJavaTask(
        extension: JavaFileConfig,
        variant: BaseVariant
    ) {
        val outputSrcDir = getGeneratedJavaOutputDirForVariant(variant.name)

        val task = project
            .tasks.register<GenerateJavaFileTask>("generateAssetsJavaFile${variant.name.capitalize()}") {
                assetFiles.setFrom(variant.assetDirs())
                this.outputSrcDir.set(outputSrcDir)

                configureUsing(extension)

                project.logger.debug(
                    "Configured java generation task for [${variant.name}] variant set\n" +
                        "Registered new java source directory - $outputSrcDir"
                )
            }

        // Have to configure task here until agp supports task provider https://issuetracker.google.com/issues/150799913
        variant.registerJavaGeneratingTask(task.get(), outputSrcDir)
    }

    private fun configureKotlinTask(
        extension: KotlinFileConfig,
        variant: BaseVariant
    ) {
        val outputSrcDir = getGeneratedKotlinOutputDirForVariant(variant.name)

        val task =
            project.tasks
                .register<GenerateKotlinFileTask>("generateAssetsKotlinFile${variant.name.capitalize()}") {
                    assetFiles.setFrom(variant.assetDirs())
                    this.outputSrcDir.set(outputSrcDir)
                    configureUsing(extension)

                    project.logger.debug(
                        "Configured kotlin generation task for [${variant.name}] variant set\n" +
                            "Registered new kotlin source directory - $outputSrcDir"
                    )
                }

        // Have to configure task here until agp supports task provider https://issuetracker.google.com/issues/150799913
        variant.registerJavaGeneratingTask(task.get(), outputSrcDir)
    }

    private fun BaseVariant.assetDirs(): Collection<File> = sourceSets.flatMap { it.assetsDirectories }

    /**
     * Returns java source root directory where files will be generated for given variant.
     *
     * ex. <Project>/build/generated/assetsjournalist/src/<main>/java
     */
    private fun getGeneratedJavaOutputDirForVariant(variantName: String) = rootGeneratedBuildDir
        .resolve(variantName).resolve("java")

    /**
     * Returns res directory where files will be generated.
     *
     * ex. <Project>/build/generated/assetsjournalist/src/<main>/res/
     */
    private fun getGeneratedResOutputDirForVariant(variantName: String) = rootGeneratedBuildDir
        .resolve(variantName).resolve("res")

    /**
     * Returns path to xml file to be generated.
     *
     * ex. <Project>/build/generated/assetsjournalist/src/<main>/res/values/assets-strings.xml
     */
    private fun getOutputXmFileForVariant(variantName: String) = rootGeneratedBuildDir
        .resolve(variantName).resolve("res").resolve("values").resolve("assets-strings.xml")

    /**
     * Returns kotlin source root directory where files will be generated for given variant.
     *
     * ex. <Project>/build/generated/assetsjournalist/src/<main>/kotlin
     */
    private fun getGeneratedKotlinOutputDirForVariant(variantName: String) = rootGeneratedBuildDir
        .resolve(variantName).resolve("kotlin")
}
