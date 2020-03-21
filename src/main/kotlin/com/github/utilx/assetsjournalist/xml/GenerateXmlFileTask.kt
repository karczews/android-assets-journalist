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

package com.github.utilx.assetsjournalist.xml

import com.android.build.gradle.api.AndroidSourceSet
import com.github.utilx.assetsjournalist.common.listAssets
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import kotlin.math.absoluteValue

private const val RESOURCE_XML_TAG = "resources"
private const val STRING_XML_TAG = "string"
private const val NAME_XML_ATTRIBUTE = "name"

private const val NOT_ALLOWED_STRING_NAME_CHAR_PATTERN = "[^A-Za-z0-9]"
private const val DEFAULT_NAME_REPLACEMENT_CHAR = "_"

open class GenerateXmlFileTask : DefaultTask() {

    private val notAllowedStringNameCharsRegex by lazy { NOT_ALLOWED_STRING_NAME_CHAR_PATTERN.toRegex() }

    @get:OutputFile
    lateinit var outputFile: File
    @get:Input
    var stringNameCharMapping = emptyList<Map<String, String>>()
    @get:Input
    var stringNamePrefix = ""
    lateinit var sourceSet: AndroidSourceSet

    /**
     * This is mainly to capture all input files and prevent running task multiple times to the same file set
     */
    @InputFiles
    fun getInputFiles(): FileTree {
        return sourceSet.assets.sourceFiles
    }

    @TaskAction
    fun generateXml() {
        FileWriter(outputFile).use { fileWriter ->
            val writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileWriter)
            val list = sourceSet.listAssets()

            writer.document {
                writeComment(
                    "This is XML file generated with asset file generator. " +
                            "All changes done here will be overwritten."
                )
                element(RESOURCE_XML_TAG) {
                    list.forEach {
                        element(STRING_XML_TAG) {
                            attribute(NAME_XML_ATTRIBUTE, createStringName(it))
                            writeCharacters(it)
                        }
                    }
                }
            }

        }

        logger.lifecycle("Created xml asset file at ${outputFile.absolutePath}")
    }

    private fun createStringName(filePath: String): String =
        filePath.replace(notAllowedStringNameCharsRegex, DEFAULT_NAME_REPLACEMENT_CHAR)
            .let { it + DEFAULT_NAME_REPLACEMENT_CHAR + filePath.hashCode().absoluteValue }
            .let { stringNamePrefix + it }

    /**
     * Configure task using provided config
     */
    fun configureUsing(config: XmlFileConfig) {
        this.stringNamePrefix = config.stringNamePrefix
        this.stringNameCharMapping = config.stringNameCharMapping
    }
}


private fun XMLStreamWriter.document(init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartDocument()
    this.init()
    this.writeEndDocument()
    return this
}

private fun XMLStreamWriter.element(name: String, init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartElement(name)
    this.init()
    this.writeEndElement()
    return this
}

private fun XMLStreamWriter.element(name: String, content: String) {
    element(name) {
        writeCharacters(content)
    }
}

private fun XMLStreamWriter.attribute(name: String, value: String) = writeAttribute(name, value)