package com.github.utilx

import org.gradle.api.tasks.SourceSet

open class AssetFileGeneratorExtension {
    var msg = "message"

    //by default use only main source set
    var sourceSetNames = listOf(SourceSet.MAIN_SOURCE_SET_NAME)

    var generateXmlFile = true
    var generateJavaFile = true
    var xmlStringNameCharMapping = emptyList<Map<String, String>>()
    var xmlStringNamePrefix = ""
}