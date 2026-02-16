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

import com.github.utilx.assetsjournalist.common.FileConstantsFactory
import com.github.utilx.assetsjournalist.common.buildStringTransformerUsing
import com.github.utilx.assetsjournalist.common.listAssets
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.lang.model.element.Modifier

open class GenerateJavaFileTask
    @javax.inject.Inject
    constructor(
        objects: ObjectFactory,
    ) : DefaultTask() {
        @get:OutputDirectory
        val outputSrcDir = objects.directoryProperty()

        @get:Input
        val className = objects.property<String>().value("AssetFile")

        @get:Input
        val packageName = objects.property<String>().value("")

        @get:Input
        val constNamePrefix = objects.property<String>().value("")

        @get:Input
        val constValuePrefix = objects.property<String>().value("")

        @get:Input
        val constValueReplacementExpressions = objects.listProperty<Map<String, String>>().value(emptyList())

        @get:InputFiles
        val assetFiles = objects.fileCollection()

        @TaskAction
        fun generateJavaFile() {
            val fileConstantsFactory =
                FileConstantsFactory(
                    constValuePrefix = constValuePrefix.get(),
                    constValueTransformer =
                        buildStringTransformerUsing(
                            constValueReplacementExpressions.get(),
                        ),
                    constNamePrefix = constNamePrefix.get(),
                )

            // converting asset listing to class fields specs
            val fields =
                assetFiles
                    .listAssets(project)
                    .asSequence()
                    .map(fileConstantsFactory::toConstNameValuePair)
                    // remove duplicate entries
                    .distinct()
                    .map {
                        FieldSpec
                            .builder(TypeName.get(String::class.java), it.name)
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("\"${it.value}\"")
                            .build()
                    }.asIterable()

            // creating class spec that includes previous field specs
            val typeSpec =
                TypeSpec
                    .classBuilder(className.get())
                    .addJavadoc(
                        "This class is generated using android-assets-journalist gradle plugin. \n" +
                            "Do not modify this class because all changes will be overwritten",
                    ).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addFields(fields)
                    .build()

            // generate class file at
            JavaFile
                .builder(packageName.get(), typeSpec)
                .build()
                .writeTo(outputSrcDir.asFile.get())

            logger.lifecycle("generating asset java class ${packageName.get()}.${className.get()} in ${outputSrcDir.get()}")
        }

        fun configureUsing(config: JavaFileConfig) {
            this.className.set(config.className)
            this.constNamePrefix.set(config.constNamePrefix)
            this.constValuePrefix.set(config.constValuePrefix)
            this.packageName.set(config.packageName)
            this.constValueReplacementExpressions.set(config.replaceInAssetsPath)
        }
    }
