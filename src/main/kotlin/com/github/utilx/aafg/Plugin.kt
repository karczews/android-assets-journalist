/*
 *  Copyright (c) 2019-present, Android Asset File Generator Contributors.
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 *  the License for the specific language governing permissions and limitations under the License.
 */

package com.github.utilx.aafg

import com.android.build.gradle.AndroidConfig
import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.internal.Try
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.task
import java.io.File

/**
 * generated/aafg/src
 */
private val GENERATED_SRC_DIR_NAME = listOf("generated", "aafg", "src").toFilePath()

private const val RES_OUTPUT_DIR_NAME = "res"
private const val JAVA_OUTPUT_DIR_NAME = "java"

private const val PRE_BUILD_TASK_NAME = "preBuild"

private const val ROOT_EXTENSION_NAME = "AAFGConfig"
private const val XML_GENERATOR_EXTENSION_NAME = "xml"

/**
 * res/values/strings.xml
 */
private val XML_OUTPUT_FILE = listOf(RES_OUTPUT_DIR_NAME, "values", "asset-strings.xml").toFilePath()


open class AssetFileGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(ROOT_EXTENSION_NAME, AssetFileGeneratorExtension::class.java)

        val xmlExtension = (extension as ExtensionAware).extensions.create(XML_GENERATOR_EXTENSION_NAME, XmlFileExtension::class.java)

        val androidConfig = Try.ofFailable { project.extensions.findByType<AndroidConfig>() }
            .mapFailure { IllegalStateException("Failed to locate android plugin extension, make sure plugin is applied after android gradle plugin") }
            .get()

        project.afterEvaluate {
            extension.sourceSetNames.forEach { sourceSetName ->
                val sourceSet = Try.ofFailable { androidConfig.sourceSets.findByName(sourceSetName)!! }
                    .mapFailure { IllegalStateException("failed to locate $sourceSetName sourceSet") }
                    .get()

                if (xmlExtension.enabled) {
                    configureXmlTask(project, xmlExtension, sourceSet)
                }

                if (extension.generateJavaFile) {
                    configureJavaTask(project, extension, sourceSet)
                }
            }
        }
    }

    private fun configureXmlTask(
        project: Project,
        xmlExtension: XmlFileExtension,
        sourceSet: AndroidSourceSet
    ) {
        //Register new res directory to provided sourceSet so all generated xml files are accessible in the project
        val generatedResDirectory = getGeneratedResOutputDirForSourceSet(
            projectBuildDir = project.buildDir,
            sourceSetName = sourceSet.name
        )
        sourceSet.res.srcDirs(generatedResDirectory)

        val generatedXmlFile = getOutpulXmFileForSourceSet(
            projectBuildDir = project.buildDir,
            sourceSetName = sourceSet.name
        )

        val xmlAssetFileTask = project.task<GenerateXmlFileTask>("generateAssetXmlFile${sourceSet.name}") {
            this.sourceSet = sourceSet
            this.outputFile = generatedXmlFile
            this.stringNamePrefix = xmlExtension.stringNamePrefix
            this.stringNameCharMapping = xmlExtension.stringNameCharMapping
        }

        // register new xml generation task
        project.tasks.getByName(PRE_BUILD_TASK_NAME).dependsOn(xmlAssetFileTask)

        println(
            "Configured xml generation task for [${sourceSet.name}] source set\n" +
                    "Registered new res directory - $generatedResDirectory\n" +
                    "Asset xml file will be generated at $generatedXmlFile"
        )
    }

    private fun configureJavaTask(
        project: Project,
        extension: AssetFileGeneratorExtension,
        sourceSet: AndroidSourceSet
    ) {
        val outputSrcDir = getGeneratedJavaOutputDirForSourceSet(
            projectBuildDir = project.buildDir,
            sourceSetName = sourceSet.name
        )

        val generateJavaTask = project.task<GenerateJavaFileTask>("generateAssetJavaFile${sourceSet.name}") {
            this.sourceSet = sourceSet
            this.outputSrcDir = outputSrcDir
            this.className = extension.javaClassName
            this.packageName = extension.javaPackageName
            this.constNamePrefix = extension.javaFieldNamePrefix
        }

        sourceSet.java.srcDirs(outputSrcDir)
        project.tasks.getByName(PRE_BUILD_TASK_NAME).dependsOn(generateJavaTask)

        println(
            "Configured java generation task for [${sourceSet.name}] source set\n" +
                    "Registered new java source directory - $outputSrcDir"
        )
    }

    /**
     * Returns SourceSet dependant output directory where files will be generated
     * usually returns something like <Project>/build/generated/aafg/src/<main>/
     */
    fun getGeneratedSrcDirForSourceSet(
        projectBuildDir: File,
        sourceSetName: String
    ) = File(projectBuildDir, listOf(GENERATED_SRC_DIR_NAME, sourceSetName).toFilePath())


    /**
     * Returns SourceSet dependant res directory where files will be generated
     * usually returns something like <Project>/build/generated/aafg/src/<main>/res
     */
    fun getGeneratedResOutputDirForSourceSet(
        projectBuildDir: File,
        sourceSetName: String
    ) = File(getGeneratedSrcDirForSourceSet(projectBuildDir, sourceSetName), RES_OUTPUT_DIR_NAME)

    /**
     * Returns SourceSet dependant res directory where files will be generated
     * usually returns something like <Project>/build/generated/aafg/src/<main>/java
     */
    fun getGeneratedJavaOutputDirForSourceSet(
        projectBuildDir: File,
        sourceSetName: String
    ) = File(getGeneratedSrcDirForSourceSet(projectBuildDir, sourceSetName), JAVA_OUTPUT_DIR_NAME)

    /**
     * Returns SourceSet dependant res directory where files will be generated
     * usually returns something like <Project>/build/generated/aafg/src/<main>/res/values/strings.xml
     */
    fun getOutpulXmFileForSourceSet(
        projectBuildDir: File,
        sourceSetName: String
    ) = File(getGeneratedSrcDirForSourceSet(projectBuildDir, sourceSetName), XML_OUTPUT_FILE)
}

private fun <T> Iterable<T>.toFilePath(): String {
    return this.joinToString(separator = File.separator)
}