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

import org.gradle.api.tasks.SourceSet

open class AssetFileGeneratorExtension {

    //by default use only main source set
    var sourceSetNames = listOf(SourceSet.MAIN_SOURCE_SET_NAME)

    // xml generation config
    var generateXmlFile = true
    var xmlStringNameCharMapping = emptyList<Map<String, String>>()
    var xmlStringNamePrefix = ""

    // java generation config
    var generateJavaFile = true
    var javaClassName = "AssetFiles"
    var javaPackageName = "com.github.utilx"
    var javaFieldNamePrefix = "asset_"
    var javaFieldNameCharMapping = emptyList<Map<String, String>>()
}