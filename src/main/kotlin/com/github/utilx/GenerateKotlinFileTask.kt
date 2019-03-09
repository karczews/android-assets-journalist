package com.github.utilx

import com.android.build.gradle.api.AndroidSourceSet
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

private const val NOT_ALLOWED_CONST_NAME_CHAR_PATTERN = "[^A-Za-z0-9$]"
private const val DEFAULT_NAME_REPLACEMENT_CHAR = "_"

open class GenerateKotlinFileTask : DefaultTask() {

    private val notAllowedConstNameCharsRegex by lazy { NOT_ALLOWED_CONST_NAME_CHAR_PATTERN.toRegex() }

    @get:OutputDirectory
    lateinit var outputSrcDir: File


    @get:Input
    var className = "AssetFile"

    @get:Input
    var packageName = ""

    @get:Input
    var constNameCharMapping = emptyList<Map<String, String>>()
    @get:Input
    var constNamePrefix = ""


    lateinit var sourceSet: AndroidSourceSet

    /**
     * This is mainly to capture all input files and prevent running task multiple times to the same file set
     */
    @InputFiles
    fun getInputFiles(): FileTree {
        return sourceSet.assets.sourceFiles
    }

    @TaskAction
    fun generateKotlinFile() {
        val assetsFileList = listAssetsIn(sourceSet)

        // converting asset listing to object property specs
        val properties = assetsFileList
            .map {
                val constName = generateConstName(it)

                PropertySpec.builder(constName, String::class)
                    .addModifiers(KModifier.CONST)
                    .initializer("\"${it}\"")
                    .build()
            }

        // create type spec for object and include all properties
        val objectSpec = TypeSpec.objectBuilder(className)
            .addProperties(properties)
            .addKdoc("This class is generated using android-asset-file-generator gradle plugin. \n" +
                    "Do not modify this class because all changes will be overwritten")
            .build()

        // generating kt file
        FileSpec.builder(packageName, className)
            .addType(objectSpec)
            .build()
            .writeTo(outputSrcDir)

        logger.quiet("generating asset kotlin file $packageName.$className in $outputSrcDir")


    }

    private fun generateConstName(assetFile: String): String {
        return assetFile.replace(notAllowedConstNameCharsRegex, DEFAULT_NAME_REPLACEMENT_CHAR)
    }
}