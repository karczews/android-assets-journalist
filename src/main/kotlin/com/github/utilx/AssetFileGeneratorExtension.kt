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