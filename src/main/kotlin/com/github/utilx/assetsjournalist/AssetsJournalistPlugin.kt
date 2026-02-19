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

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.github.utilx.assetsjournalist.java.GenerateJavaFileTask
import com.github.utilx.assetsjournalist.java.JavaFileConfig
import com.github.utilx.assetsjournalist.kotlin.GenerateKotlinFileTask
import com.github.utilx.assetsjournalist.kotlin.KotlinFileConfig
import com.github.utilx.assetsjournalist.xml.GenerateXmlFileTask
import com.github.utilx.assetsjournalist.xml.XmlFileConfig
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.util.GradleVersion
import java.io.File

internal const val MIN_GRADLE_VERSION = "8.0"

internal const val ROOT_EXTENSION_NAME = "androidAssetsJournalist"

open class AssetsJournalistPlugin : Plugin<Project> {
    override fun apply(target: Project) = ProjectScopedConfiguration(target).apply()
}

private class ProjectScopedConfiguration(
    private val project: Project,
) {
    /**
     * <builddir>/generated/assetsjournalist/src
     */
    private val rootGeneratedBuildDir: Provider<Directory> =
        project.layout.buildDirectory
            .dir("generated/assetsjournalist/src")

    fun apply() {
        if (GradleVersion.current() < GradleVersion.version(MIN_GRADLE_VERSION)) {
            throw GradleException(
                "Current gradle versions is ${GradleVersion.current()}, " +
                    "AssetsJournalistPlugin supports only gradle $MIN_GRADLE_VERSION+",
            )
        }

        val extension = project.extensions.create(ROOT_EXTENSION_NAME, AssetFileGeneratorConfig::class)
        val xmlExtension = extension.xmlFile
        val javaExtension = extension.javaFile
        val kotlinExtension = extension.kotlinFile

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class)

        val isKotlinEnabled by lazy(LazyThreadSafetyMode.NONE) {
            if (!xmlExtension.enabled && !javaExtension.enabled && !kotlinExtension.enabled) {
                project.logger.warn("No file type enabled, enabling kotlin file generation")
                kotlinExtension.enabled = true
            }
            kotlinExtension.enabled
        }

        androidComponents.onVariants { variant ->
            project.logger.debug("Processing variant ${variant.name}")

            // Check if any file type is enabled
            val variantAssets = variant.getAssetDirectories()

            if (xmlExtension.enabled) {
                configureXmlTask(xmlExtension, variant, variantAssets)
            }

            if (javaExtension.enabled) {
                configureJavaTask(javaExtension, variant, variantAssets)
            }

            if (isKotlinEnabled) {
                configureKotlinTask(kotlinExtension, variant, variantAssets)
            }
        }
    }

    private fun Variant.getAssetDirectories(): List<File> =
        sources.assets?.all?.orNull?.flatMap { dirs ->
            dirs.map { it.asFile }
        } ?: emptyList()

    private fun configureXmlTask(
        xmlConfig: XmlFileConfig,
        variant: Variant,
        assetDirs: Collection<File>,
    ) {
        val generatedResDirectory = getGeneratedResOutputDirForVariant(variant.name)
        val generatedXmlFile = getOutputXmFileForVariant(variant.name)

        val taskProvider =
            project.tasks
                .register<GenerateXmlFileTask>("generateAssetsXmlFile${variant.name.replaceFirstChar { it.uppercase() }}") {
                    assetFiles.setFrom(assetDirs)
                    outputFile.set(generatedXmlFile)
                    outputSrcDir.set(generatedResDirectory)
                    configureUsing(xmlConfig)

                    project.logger.debug(
                        "Configured xml generation task for [${variant.name}] variant\n" +
                            "Registered new res directory - ${generatedResDirectory.get()}\n" +
                            "Asset xml file will be generated at ${generatedXmlFile.get()}",
                    )
                }

        val resSources = variant.sources.res
        if (resSources != null) {
            resSources.addGeneratedSourceDirectory(taskProvider, GenerateXmlFileTask::outputSrcDir)
        } else {
            project.logger.error("Variant ${variant.name} does not support resources, skipping XML generation task registration")
        }
    }

    private fun configureJavaTask(
        extension: JavaFileConfig,
        variant: Variant,
        assetDirs: Collection<File>,
    ) {
        val taskProvider =
            project
                .tasks
                .register<GenerateJavaFileTask>("generateAssetsJavaFile${variant.name.replaceFirstChar { it.uppercase() }}") {
                    assetFiles.setFrom(assetDirs)
                    outputSrcDir.set(getGeneratedJavaOutputDirForVariant(variant.name))
                    configureUsing(extension)

                    project.logger.debug(
                        "Configured java generation task for [${variant.name}] variant set\n" +
                            "Registered new java source directory - ${outputSrcDir.get()}",
                    )
                }

        val javaSources = variant.sources.java
        if (javaSources != null) {
            javaSources.addGeneratedSourceDirectory(taskProvider, GenerateJavaFileTask::outputSrcDir)
        } else {
            project.logger.error("Variant ${variant.name} does not support Java sources, skipping Java generation task registration")
        }
    }

    private fun configureKotlinTask(
        extension: KotlinFileConfig,
        variant: Variant,
        assetDirs: Collection<File>,
    ) {
        val taskProvider =
            project.tasks
                .register<GenerateKotlinFileTask>("generateAssetsKotlinFile${variant.name.replaceFirstChar { it.uppercase() }}") {
                    assetFiles.setFrom(assetDirs)
                    outputSrcDir.set(getGeneratedKotlinOutputDirForVariant(variant.name))
                    configureUsing(extension)

                    project.logger.debug(
                        "Configured kotlin generation task for [${variant.name}] variant set\n" +
                            "Registered new kotlin source directory - ${outputSrcDir.get()}",
                    )
                }

        val javaSources = variant.sources.java
        if (javaSources != null) {
            javaSources.addGeneratedSourceDirectory(taskProvider, GenerateKotlinFileTask::outputSrcDir)
        } else {
            project.logger.error("Variant ${variant.name} does not support Java sources, skipping Kotlin generation task registration")
        }
    }

    /**
     * Returns java source root directory where files will be generated for given variant.
     *
     * ex. <Project>/build/generated/assetsjournalist/src/<main>/java
     */
    private fun getGeneratedJavaOutputDirForVariant(variantName: String): Provider<Directory> =
        rootGeneratedBuildDir
            .map { it.dir(variantName).dir("java") }

    /**
     * Returns res directory where files will be generated.
     *
     * ex. <Project>/build/generated/assetsjournalist/src/<main>/res/
     */
    private fun getGeneratedResOutputDirForVariant(variantName: String): Provider<Directory> =
        rootGeneratedBuildDir
            .map { it.dir(variantName).dir("res") }

    /**
     * Returns path to xml file to be generated.
     *
     * ex. <Project>/build/generated/assetsjournalist/src/<main>/res/values/assets-strings.xml
     */
    private fun getOutputXmFileForVariant(variantName: String): Provider<RegularFile> =
        rootGeneratedBuildDir
            .map {
                it
                    .dir(variantName)
                    .dir("res")
                    .dir("values")
                    .file("assets-strings.xml")
            }

    /**
     * Returns kotlin source root directory where files will be generated for given variant.
     *
     * ex. <Project>/build/generated/assetsjournalist/src/<main>/kotlin
     */
    private fun getGeneratedKotlinOutputDirForVariant(variantName: String): Provider<Directory> =
        rootGeneratedBuildDir
            .map { it.dir(variantName).dir("kotlin") }
}
