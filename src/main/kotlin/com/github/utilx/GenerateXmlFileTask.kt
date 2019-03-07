package com.github.utilx

import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import java.io.File
import java.io.FileWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

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
            val list = listAssetsIn(sourceSet)

            writer.document {
                writeComment("This is XML file generated with asset file generator. All changes done here will be overwritten.")
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

        logger.quiet("Created xml asset file at ${outputFile.absolutePath}")
    }

    private fun createStringName(filePath: String): String =
        filePath.replace(notAllowedStringNameCharsRegex, DEFAULT_NAME_REPLACEMENT_CHAR)
            .let { stringNamePrefix + it }

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

fun Project.declareGenerateXmlFileTask() = tasks.register<GenerateXmlFileTask>("generateXmlAssetFile")