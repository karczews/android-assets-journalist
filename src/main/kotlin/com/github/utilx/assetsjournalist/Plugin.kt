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
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.task
import java.io.File

/**
 * generated/assetsjournalist/src
 */
private val GENERATED_SRC_DIR_NAME = listOf("generated", "assetsjournalist", "src").toFilePath()

private const val RES_OUTPUT_DIR_NAME = "res"
private const val JAVA_OUTPUT_DIR_NAME = "java"
private const val KOTLIN_OUTPUT_DIR_NAME = "kotlin"

private const val PRE_BUILD_TASK_NAME = "preBuild"

internal const val ROOT_EXTENSION_NAME = "androidAssetsJournalist"
internal const val XML_GENERATOR_EXTENSION_NAME = "xmlFile"
internal const val JAVA_GENERATOR_EXTENSION_NAME = "javaFile"
internal const val KOTLIN_GENERATOR_EXTENSION_NANE = "kotlinFile"

/**
 * res/values/strings.xml
 */
private val XML_OUTPUT_FILE = listOf(RES_OUTPUT_DIR_NAME, "values", "assets-strings.xml").toFilePath()


open class AssetsJournalistPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(ROOT_EXTENSION_NAME, AssetFileGeneratorConfig::class.java)
        val extensionAware = extension as ExtensionAware

        val xmlExtension =
            extensionAware.extensions.create(XML_GENERATOR_EXTENSION_NAME, XmlFileConfig::class.java)

        val javaExtension = extensionAware.extensions.create(JAVA_GENERATOR_EXTENSION_NAME, JavaFileConfig::class.java)

        val kotlinExtension =
            extensionAware.extensions.create(KOTLIN_GENERATOR_EXTENSION_NANE, KotlinFileConfig::class.java)

        val androidConfig = runCatching { project.extensions.findByType<AndroidConfig>()!! }
            .onFailure { throw IllegalStateException("Failed to locate android plugin extension, make sure plugin is applied after android gradle plugin") }
            .getOrThrow()

        // workaround - need to register sourceset here before evaluation
        runCatching { androidConfig.sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME)!! }
            .onSuccess {
                it.java.srcDirs(
                    getGeneratedJavaOutputDirForSourceSet(
                        projectBuildDir = project.buildDir,
                        sourceSetName = SourceSet.MAIN_SOURCE_SET_NAME
                    )
                )
            }
            .onSuccess {
                it.java.srcDirs(
                    getGeneratedKotlinOutputDirForSourceSet(
                        projectBuildDir = project.buildDir,
                        sourceSetName = SourceSet.MAIN_SOURCE_SET_NAME
                    )
                )
            }.onSuccess {
                it.res.srcDirs(
                    getGeneratedResOutputDirForSourceSet(
                        projectBuildDir = project.buildDir,
                        sourceSetName = SourceSet.MAIN_SOURCE_SET_NAME
                    )
                )
            }


        project.afterEvaluate {
            extension.sourceSets
                .ifEmpty {
                    project.logger.quiet("No source set specified, using main")
                    runCatching { androidConfig.sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME)!! }
                        .onFailure { throw IllegalStateException("failed to locate ${SourceSet.MAIN_SOURCE_SET_NAME} sourceSet") }
                        .map { listOf(it) }
                        .getOrThrow()
                }.forEach { sourceSet ->

                    if (!xmlExtension.enabled && !javaExtension.enabled && !kotlinExtension.enabled) {
                        project.logger.warn("No file type enabled, enabling java file generation")
                        javaExtension.enabled = true
                    }

                    if (xmlExtension.enabled) {
                        configureXmlTask(project, xmlExtension, sourceSet)
                    }

                    if (javaExtension.enabled) {
                        configureJavaTask(project, javaExtension, sourceSet)
                    }

                    if (kotlinExtension.enabled) {
                        configureKotlinTask(project, kotlinExtension, sourceSet)
                    }
                }
        }
    }

    private fun configureXmlTask(
        project: Project,
        xmlConfig: XmlFileConfig,
        sourceSet: AndroidSourceSet
    ) {
        //Register new res directory to provided sourceSet so all generated xml files are accessible in the project
        val generatedResDirectory = getGeneratedResOutputDirForSourceSet(
            projectBuildDir = project.buildDir,
            sourceSetName = sourceSet.name
        )
        //sourceSet.res.srcDirs(generatedResDirectory)

        val generatedXmlFile = getOutpulXmFileForSourceSet(
            projectBuildDir = project.buildDir,
            sourceSetName = sourceSet.name
        )

        val xmlAssetFileTask =
            project.task<GenerateXmlFileTask>("generateAssetsXmlFile${sourceSet.name.capitalize()}") {
                this.sourceSet = sourceSet
                this.outputFile = generatedXmlFile
            }.apply { configureUsing(xmlConfig) }

        // register new xml generation task
        project.tasks.getByName(PRE_BUILD_TASK_NAME).dependsOn(xmlAssetFileTask)

        project.logger.quiet(
            "Configured xml generation task for [${sourceSet.name}] source set\n" +
                    "Registered new res directory - $generatedResDirectory\n" +
                    "Asset xml file will be generated at $generatedXmlFile"
        )
    }

    private fun configureJavaTask(
        project: Project,
        extension: JavaFileConfig,
        sourceSet: AndroidSourceSet
    ) {
        val outputSrcDir = getGeneratedJavaOutputDirForSourceSet(
            projectBuildDir = project.buildDir,
            sourceSetName = sourceSet.name
        )

        val generateJavaTask =
            project.task<GenerateJavaFileTask>("generateAssetsJavaFile${sourceSet.name.capitalize()}") {
                this.sourceSet = sourceSet
                this.outputSrcDir = outputSrcDir
            }.apply { configureUsing(extension) }

        // sourceSet.java.srcDirs(outputSrcDir)
        project.tasks.getByName(PRE_BUILD_TASK_NAME).dependsOn(generateJavaTask)

        project.logger.quiet(
            "Configured java generation task for [${sourceSet.name}] source set\n" +
                    "Registered new java source directory - $outputSrcDir"
        )
    }

    fun configureKotlinTask(
        project: Project,
        extension: KotlinFileConfig,
        sourceSet: AndroidSourceSet
    ) {
        val outputSrcDir = getGeneratedKotlinOutputDirForSourceSet(
            projectBuildDir = project.buildDir,
            sourceSetName = sourceSet.name
        )
        val generateKotlinTask =
            project.task<GenerateKotlinFileTask>("generateAssetsKotlinFile${sourceSet.name.capitalize()}") {
                this.sourceSet = sourceSet
                this.outputSrcDir = outputSrcDir
            }.apply { configureUsing(extension) }

        // sourceSet.java.srcDirs(outputSrcDir)
        project.tasks.getByName(PRE_BUILD_TASK_NAME).dependsOn(generateKotlinTask)

        project.logger.quiet(
            "Configured kotlin generation task for [${sourceSet.name}] source set\n" +
                    "Registered new kotlin source directory - $outputSrcDir"
        )

    }

    /**
     * Returns SourceSet dependant output directory where files will be generated
     * usually returns something like <Project>/build/generated/assetsjournalist/src/<main>/
     */
    fun getGeneratedSrcDirForSourceSet(
        projectBuildDir: File,
        sourceSetName: String
    ) = File(projectBuildDir, listOf(GENERATED_SRC_DIR_NAME, sourceSetName).toFilePath())


    /**
     * Returns SourceSet dependant res directory where files will be generated
     * usually returns something like <Project>/build/generated/assetsjournalist/src/<main>/res
     */
    fun getGeneratedResOutputDirForSourceSet(
        projectBuildDir: File,
        sourceSetName: String
    ) = File(getGeneratedSrcDirForSourceSet(projectBuildDir, sourceSetName), RES_OUTPUT_DIR_NAME)

    /**
     * Returns SourceSet dependant java source root directory where files will be generated
     * usually returns something like <Project>/build/generated/assetsjournalist/src/<main>/java
     */
    fun getGeneratedJavaOutputDirForSourceSet(
        projectBuildDir: File,
        sourceSetName: String
    ) = File(getGeneratedSrcDirForSourceSet(projectBuildDir, sourceSetName), JAVA_OUTPUT_DIR_NAME)

    /**
     * Returns SourceSet dependant kotlin source root directory where files will be generated
     * usually returns something like <Project>/build/generated/assetsjournalist/src/<main>/kotlin
     */
    fun getGeneratedKotlinOutputDirForSourceSet(
        projectBuildDir: File,
        sourceSetName: String
    ) = File(getGeneratedSrcDirForSourceSet(projectBuildDir, sourceSetName), KOTLIN_OUTPUT_DIR_NAME)

    /**
     * Returns SourceSet dependant res directory where files will be generated
     * usually returns something like <Project>/build/generated/assetsjournalist/src/<main>/res/values/strings.xml
     */
    fun getOutpulXmFileForSourceSet(
        projectBuildDir: File,
        sourceSetName: String
    ) = File(getGeneratedSrcDirForSourceSet(projectBuildDir, sourceSetName), XML_OUTPUT_FILE)
}

private fun <T> Iterable<T>.toFilePath(): String {
    return this.joinToString(separator = File.separator)
}