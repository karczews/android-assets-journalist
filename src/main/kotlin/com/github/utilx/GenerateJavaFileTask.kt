/**
 * Copyright (c) 2019-present, Android Asset File Generator Contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package com.github.utilx

import com.android.build.gradle.api.AndroidSourceSet
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.*
import java.io.File
import javax.lang.model.element.Modifier

private const val NOT_ALLOWED_CONST_NAME_CHAR_PATTERN = "[^A-Za-z0-9$]"
private const val DEFAULT_NAME_REPLACEMENT_CHAR = "_"

open class GenerateJavaFileTask : DefaultTask() {

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
    fun generateJavaFile() {
        val assetsFileList = listAssetsIn(sourceSet)

        // converting asset listing to class fields specs
        val fields = assetsFileList
            .map {
                val constName = generateConstName(it)
                FieldSpec.builder(TypeName.get(String::class.java), constName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\"$it\"")
                    .build()
            }

        // creating class spec that includes previous field specs
        val typeSpec = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addFields(fields)
            .build()

        // generate class file at
        JavaFile.builder(packageName, typeSpec)
            .build()
            .writeTo(outputSrcDir)

        logger.quiet("generating asset java class $packageName.$className in $outputSrcDir")

    }

    private fun generateConstName(assetFile: String): String {
        return assetFile.replace(notAllowedConstNameCharsRegex, DEFAULT_NAME_REPLACEMENT_CHAR)
    }
}