package com.github.utilx

import com.android.build.gradle.AndroidConfig
import com.android.build.gradle.api.AndroidSourceDirectorySet
import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.findByType

open class AssetFileGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("AFGConfig", AssetFileGeneratorExtension::class.java)

        project.afterEvaluate {

            val sourceSet = project.extensions.findByType<AndroidConfig>()
                ?.sourceSets
                ?.findByName(SourceSet.MAIN_SOURCE_SET_NAME)

            val sourceDirs = listAssetsIn(sourceSet)

            println("whoops, got ${sourceDirs}")
            println("sets" + project.extensions.findByType<AndroidConfig>()?.sourceSets?.names)

            project.extensions.findByType<AndroidConfig>()
                ?.sourceSets
        }

        val task = project.task("test2") {
            doLast {
                println("running test ${extension.xmlStringNameCharMapping[0]}")
            }
        }


        project.tasks.getByName("preBuild").dependsOn(task)
    }


    private fun listAssetsIn(sourceSet: AndroidSourceSet?) : List<String> {
        return sourceSet
            ?.assets
            ?.sourceDirectoryTrees
            ?.flatMap { assetFileTree ->
                val assetBaseDir = assetFileTree.dir
                assetFileTree.asFileTree.files
                    .map { it.relativeTo(assetBaseDir) }
                    .map { it.toString() }
            } ?: emptyList()
    }
}