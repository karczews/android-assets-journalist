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

import com.github.utilx.assetsjournalist.common.listAssets
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import java.io.FileWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import kotlin.math.absoluteValue

private const val RESOURCE_XML_TAG = "resources"
private const val STRING_XML_TAG = "string"
private const val NAME_XML_ATTRIBUTE = "name"

private const val NOT_ALLOWED_STRING_NAME_CHAR_PATTERN = "[^A-Za-z0-9]"
private const val DEFAULT_NAME_REPLACEMENT_CHAR = "_"

open class GenerateXmlFileTask @javax.inject.Inject constructor(objects: ObjectFactory) : DefaultTask() {

    private val notAllowedStringNameCharsRegex by lazy { NOT_ALLOWED_STRING_NAME_CHAR_PATTERN.toRegex() }

    @get:Input
    val stringNameCharMapping = objects.listProperty<Map<String, String>>()

    @get:Input
    val stringNamePrefix = objects.property<String>()

    @get:InputFiles
    val assetFiles = objects.fileCollection()

    @get:OutputFile
    val outputFile = objects.fileProperty()

    @TaskAction
    fun generateXml() {
        FileWriter(outputFile.asFile.get()).use { fileWriter ->
            val writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileWriter)

            val list = assetFiles.listAssets(project)

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

            logger.lifecycle("Created xml asset file at ${outputFile.asFile.get().path}")
        }
    }

    private fun createStringName(filePath: String): String =
        filePath.replace(notAllowedStringNameCharsRegex, DEFAULT_NAME_REPLACEMENT_CHAR)
            .let { it + DEFAULT_NAME_REPLACEMENT_CHAR + filePath.hashCode().absoluteValue }
            .let { stringNamePrefix.get() + it }

    /**
     * Configure task using provided config
     */
    fun configureUsing(config: XmlFileConfig) {
        this.stringNamePrefix.set(config.stringNamePrefix)
        this.stringNameCharMapping.addAll(config.stringNameCharMapping)
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