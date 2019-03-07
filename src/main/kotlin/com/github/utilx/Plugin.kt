package com.github.utilx

import com.android.build.gradle.AndroidConfig
import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.Try
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.task
import java.io.File

/**
 * generated/aafg/src
 */
private val GENERATED_SRC_DIR_NAME = listOf("generated", "aafg", "src").toFilePath()

private const val RES_OUTPUT_DIR_NAME = "res"

/**
 * res/values/strings.xml
 */
private val XML_OUTPUT_FILE = listOf(RES_OUTPUT_DIR_NAME, "values", "asset-strings.xml").toFilePath()

open class AssetFileGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("AFGConfig", AssetFileGeneratorExtension::class.java)

        val androidConfig = Try.ofFailable { project.extensions.findByType<AndroidConfig>() }
            .mapFailure { IllegalStateException("Failed to locate android plugin extension, make sure plugin is applied after android gradle plugin") }
            .get()

        project.afterEvaluate {
            extension.sourceSetNames.forEach { sourceSetName ->
                val sourceSet = Try.ofFailable { androidConfig.sourceSets.findByName(sourceSetName)!! }
                    .mapFailure { IllegalStateException("failed to locate $sourceSetName sourceSet") }
                    .get()

                if (extension.generateXmlFile) {
                    configureXmlTask(project, extension, sourceSet)
                }
            }
        }
    }

    fun configureXmlTask(
        project: Project,
        extension: AssetFileGeneratorExtension,
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
        println("after evaluate running, prefix - " + extension.xmlStringNamePrefix)

        val xmlAssetFileTask = project.task<GenerateXmlFileTask>("generateAssetXmlFile${sourceSet.name}") {
            this.sourceSet = sourceSet
            outputFile = generatedXmlFile
            stringNamePrefix = extension.xmlStringNamePrefix
            stringNameCharMapping = extension.xmlStringNameCharMapping
        }

        // register new xml generation task
        project.tasks.getByName("preBuild").dependsOn(xmlAssetFileTask)

        println(
            "Configured xml generation task for [${sourceSet.name}] source set\n" +
                    "Registered new res directory - $generatedResDirectory\n" +
                    "Asset xml file will be generated at $generatedXmlFile"
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