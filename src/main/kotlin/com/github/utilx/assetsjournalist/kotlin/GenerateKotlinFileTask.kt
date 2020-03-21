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

import com.android.build.gradle.api.AndroidSourceSet
import com.github.utilx.assetsjournalist.common.FileConstantsFactory
import com.github.utilx.assetsjournalist.common.buildStringTransformerUsing
import com.github.utilx.assetsjournalist.common.listAssets
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


open class GenerateKotlinFileTask : DefaultTask() {

    @get:OutputDirectory
    lateinit var outputSrcDir: File

    @get:Input
    var className = "AssetFileKt"

    @get:Input
    var packageName = ""

    @get:Input
    var constNamePrefix = ""
    @get:Input
    var constValuePrefix = ""
    @get:Input
    var constValueReplacementExpressions = emptyList<Map<String, String>>()

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
        val fileConstantsFactory = FileConstantsFactory(
            constValuePrefix = constValuePrefix,
            constValueTransformer = buildStringTransformerUsing(
                constValueReplacementExpressions
            ),
            constNamePrefix = constNamePrefix
        )

        val properties = sourceSet.listAssets()
            .asSequence()
            .map(fileConstantsFactory::toConstNameValuePair)
            // remove duplicate entries
            .distinct()
            .map {
                PropertySpec.builder(it.name, String::class)
                    .addModifiers(KModifier.CONST)
                    .initializer("\"${it.value}\"")
                    .build()
            }
            .asIterable()

        // create type spec for object and include all properties
        val objectSpec = TypeSpec.objectBuilder(className)
            .addProperties(properties)
            .addKdoc(
                "This class is generated using android-assets-journalist gradle plugin. \n" +
                        "Do not modify this class because all changes will be overwritten"
            )
            .build()

        // generating kt file
        FileSpec.builder(packageName, className)
            .addType(objectSpec)
            .build()
            .writeTo(outputSrcDir)

        logger.lifecycle("generating asset kotlin file $packageName.$className in $outputSrcDir")
    }

    fun configureUsing(config: KotlinFileConfig) {
        this.constNamePrefix = config.constNamePrefix
        this.constValuePrefix = config.constValuePrefix
        this.packageName = config.packageName
        this.className = config.className

        this.constValueReplacementExpressions = config.replaceInAssetsPath
    }
}
