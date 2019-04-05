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

package com.github.utilx.assetsjournalist.java

import com.android.build.gradle.api.AndroidSourceSet
import com.github.utilx.assetsjournalist.internal.FileConstantsFactory
import com.github.utilx.assetsjournalist.internal.buildStringTransformerUsing
import com.github.utilx.assetsjournalist.internal.listAssets
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.lang.model.element.Modifier

open class GenerateJavaFileTask : DefaultTask() {

    @get:OutputDirectory
    lateinit var outputSrcDir: File

    @get:Input
    var className = "AssetFile"
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
    fun generateJavaFile() {
        val fileConstantsFactory = FileConstantsFactory(
            constValuePrefix = constValuePrefix,
            constValueTransformer = buildStringTransformerUsing(constValueReplacementExpressions),
            constNamePrefix = constNamePrefix
        )

        // converting asset listing to class fields specs
        val fields = sourceSet.listAssets()
            .asSequence()
            .map(fileConstantsFactory::toConstNameValuePair)
            // remove duplicate entries
            .distinct()
            .map {
                FieldSpec.builder(TypeName.get(String::class.java), it.name)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\"${it.value}\"")
                    .build()
            }
            .asIterable()

        // creating class spec that includes previous field specs
        val typeSpec = TypeSpec.classBuilder(className)
            .addJavadoc(
                "This class is generated using android-assets-journalist gradle plugin. \n" +
                        "Do not modify this class because all changes will be overwritten"
            )
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addFields(fields)
            .build()

        // generate class file at
        JavaFile.builder(packageName, typeSpec)
            .build()
            .writeTo(outputSrcDir)

        logger.lifecycle("generating asset java class $packageName.$className in $outputSrcDir")

    }

    fun configureUsing(config: JavaFileConfig) {
        this.className = config.className
        this.constNamePrefix = config.constNamePrefix
        this.constValuePrefix = config.constValuePrefix
        this.packageName = config.packageName

        this.constValueReplacementExpressions = config.replaceInAssetsPath
    }
}
