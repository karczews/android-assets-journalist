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

package com.github.utilx.assetsjournalist

import com.android.build.gradle.AndroidConfig
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import com.github.utilx.assetsjournalist.java.GenerateJavaFileTask
import com.github.utilx.assetsjournalist.kotlin.GenerateKotlinFileTask
import com.github.utilx.assetsjournalist.xml.GenerateXmlFileTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PluginTest {
    private val project: Project = ProjectBuilder.builder().build()
    private val androidAssetsJournalist: AssetFileGeneratorConfig
        get() = project.extensions.getByName<AssetFileGeneratorConfig>("androidAssetsJournalist")
    private val kotlinFileExtension
        get() = androidAssetsJournalist.kotlinFile
    private val javaFileExtension
        get() = androidAssetsJournalist.javaFile
    private val xmlFileExtension
        get() = androidAssetsJournalist.xmlFile

    @Nested
    @DisplayName("Test failure cases")
    inner class Failure {
        @Test
        @DisplayName("Applying plugin should fail if Android Gradle Plugin is not applied")
        fun shouldFailToApplyIfAGPMissing() {
            val exception = assertFails { project.pluginManager.apply(AssetsJournalistPlugin::class.java) }
            assertTrue { exception.cause is GradleException }
        }
    }

    @Nested
    @DisplayName("Test Kotlin setup")
    inner class KotlinSetup {
        @BeforeEach
        fun setUp() {
            applyPlugin()
        }

        @Test
        @DisplayName("Should not make prebuild dependant on kotlin file generation task if extension disabled")
        fun shouldNotRegisteringPrebuildDependency() {
            // given plugin is applied and kotlin extension disabled
            kotlinFileExtension.enabled = false

            // when
            evaluateProject()

            // then
            assertTaskNotRegistered(GenerateKotlinFileTask::class.java)
        }

        // FIXME: verify that task output was configured for variant
        /* @Test
         @DisplayName("Should register kotlin source dir to project")
         fun shouldRegisterKotlinSrcDirectory() {
             // given plugin is applied and kotlin extension enabled
             kotlinFileExtension.enabled = true

             // when
             evaluateProject()

             // then
             assertSourceSetSrcDirRegistered(project.buildDir.path + "/generated/assetsjournalist/src/main/kotlin")
         }*/

        @Test
        @DisplayName("Test if task is configured correctly")
        fun shouldConfigureKotlinTaskCorrectly() {
            // given plugin is applied and extension enabled
            val expectedClassName = "testClassName"
            val expectedPackageName = "test.package.name"
            val expectedConstNamePrefix = "testPrefix_"
            val expectedConstValuePrefix = "testValuePrefix_"
            val expectedReplaceInAssetsPath =
                listOf(
                    mapOf(Pair(SourceFileConfig.CONST_VALUE_REPLACEMENT_EXPRESSION_MATCH_KEY, "testMatch")),
                    mapOf(Pair(SourceFileConfig.CONST_VALUE_REPLACEMENT_EXPRESSION_REPLACE_WITH_KEY, "testReplace")),
                )

            kotlinFileExtension.apply {
                enabled = true
                className = expectedClassName
                packageName = expectedPackageName
                constNamePrefix = expectedConstNamePrefix
                constValuePrefix = expectedConstValuePrefix
                replaceInAssetsPath = expectedReplaceInAssetsPath
            }

            // when
            evaluateProject()

            // then
            val task = project.tasks.find { it is GenerateKotlinFileTask } as GenerateKotlinFileTask
            assertTrue {
                task.className.get() == expectedClassName &&
                    task.packageName.get() == expectedPackageName &&
                    task.constNamePrefix.get() == expectedConstNamePrefix &&
                    task.constValuePrefix.get() == expectedConstValuePrefix &&
                    task.constValueReplacementExpressions.get() == expectedReplaceInAssetsPath
            }
        }
    }

    @Nested
    @DisplayName("Test Java setup")
    inner class JavaSetup {
        @BeforeEach
        fun setUp() {
            applyPlugin()
        }

        @Test
        @DisplayName("Should enable java file generation by default if no file generation enabled")
        fun shouldEnableJavaFileGenerationByDefault() {
            // given plugin is applied and extension enabled
            javaFileExtension.enabled = false
            kotlinFileExtension.enabled = false
            xmlFileExtension.enabled = false

            // when
            evaluateProject()

            // then
            assertTrue { project.tasks.withType<GenerateJavaFileTask>().isNotEmpty() }
            assertTrue { project.tasks.withType<GenerateKotlinFileTask>().isEmpty() }
            assertTrue { project.tasks.withType<GenerateXmlFileTask>().isEmpty() }
        }

        @Test
        @DisplayName("Should not make prebuild dependant on java file generation task if extension disabled")
        fun shouldNotRegisteringPrebuildDependency() {
            // given plugin is applied and extension disabled but other is enabled to avoid defaulting to java enabled
            kotlinFileExtension.enabled = true
            javaFileExtension.enabled = false

            // when
            evaluateProject()

            // then
            assertTaskNotRegistered(GenerateJavaFileTask::class.java)
        }

        // FIXME: verify that task output was configured for variant
        /*@Test
        @DisplayName("Should register java source dir to project")
        fun shouldRegisterJavaSrcDirectory() {
            // given plugin is applied and kotlin extension enabled
            javaFileExtension.enabled = true

            // when
            evaluateProject()

            // then
            assertSourceSetSrcDirRegistered(project.buildDir.path + "/generated/assetsjournalist/src/main/java")
        }*/

        @Test
        @DisplayName("Test if task is configured correctly")
        fun shouldConfigureJavaTaskCorrectly() {
            // given plugin is applied and extension enabled
            val expectedClassName = "testClassName"
            val expectedPackageName = "test.package.name"
            val expectedConstNamePrefix = "testPrefix_"
            val expectedConstValuePrefix = "testValuePrefix_"
            val expectedReplaceInAssetsPath =
                listOf(
                    mapOf(Pair(SourceFileConfig.CONST_VALUE_REPLACEMENT_EXPRESSION_MATCH_KEY, "testMatch")),
                    mapOf(Pair(SourceFileConfig.CONST_VALUE_REPLACEMENT_EXPRESSION_REPLACE_WITH_KEY, "testReplace")),
                )

            javaFileExtension.apply {
                enabled = true
                className = expectedClassName
                packageName = expectedPackageName
                constNamePrefix = expectedConstNamePrefix
                constValuePrefix = expectedConstValuePrefix
                replaceInAssetsPath = expectedReplaceInAssetsPath
            }

            // when
            evaluateProject()

            // then
            val task = project.tasks.find { it is GenerateJavaFileTask } as GenerateJavaFileTask
            assertTrue {
                task.className.get() == expectedClassName &&
                    task.packageName.get() == expectedPackageName &&
                    task.constNamePrefix.get() == expectedConstNamePrefix &&
                    task.constValuePrefix.get() == expectedConstValuePrefix &&
                    task.constValueReplacementExpressions.get() == expectedReplaceInAssetsPath
            }
        }
    }

    @Nested
    @DisplayName("Test Xml setup")
    inner class XmlSetup {
        @BeforeEach
        fun setUp() {
            applyPlugin()
        }

        @Test
        @DisplayName("Should not make prebuild dependant on xml file generation task if extension disabled")
        fun shouldNotRegisteringPrebuildDependency() {
            // given plugin is applied and extension disabled
            xmlFileExtension.enabled = false

            // when
            evaluateProject()

            // then
            assertTaskNotRegistered(GenerateXmlFileTask::class.java)
        }

        // FIXME: verify that task output was configured for variant
        /*
        @Test
        @DisplayName("Should register xml res dir to project")
        fun shouldRegisterXmlResDirectory() {
            // given plugin is applied and kotlin extension enabled
            xmlFileExtension.enabled = true

            // when
            evaluateProject()

            // then
            assertResDirRegistered(project.buildDir.path + "/generated/assetsjournalist/src/main/res")
        }*/

        @Test
        @DisplayName("Test if xml file task is configured correctly")
        fun shouldConfigureXmlTaskCorrectly() {
            // given plugin is applied and extension enabled
            val expectedStringNamePrefix = "testPrefix_"

            xmlFileExtension.apply {
                enabled = true
                stringNamePrefix = expectedStringNamePrefix
            }

            // when
            evaluateProject()

            // then
            val task = project.tasks.find { it is GenerateXmlFileTask } as GenerateXmlFileTask
            assertTrue {
                task.stringNamePrefix.get() == expectedStringNamePrefix
            }
        }
    }

    // DefaultAndroidSourceSet

    private fun applyPlugin() {
        project.pluginManager.apply(AppPlugin::class.java)
        project.pluginManager.apply(AssetsJournalistPlugin::class.java)

        androidExtension().apply {
            namespace = "com.github.utilx.testapp"
            compileSdkVersion(21)
        }
    }

    private fun <T : Task> assertTaskNotRegistered(taskClass: Class<T>) {
        val task = project.tasks.find { taskClass.isInstance(it) }
        assertNull(task, "task $taskClass should not be registered")
    }

    private inline fun <reified T : Task> assertPreBuildDependsOn(taskClass: Class<T>) {
        val result =
            project.tasks
                .findByPath("preBuild")
                ?.dependsOn
                ?.find { it is T || (it is Provider<*> && it.get() is T) }

        assertNotNull(result, "preBuild does not depend on $taskClass")
    }

    private fun assertSourceSetSrcDirRegistered(
        srcDir: String,
        setName: String = SourceSet.MAIN_SOURCE_SET_NAME,
    ) {
        val srcDirs = getSourceSet(setName).java.srcDirs
        assertTrue(
            srcDirs.contains(File(srcDir)),
            "srcDir $srcDir is not registered in $setName sourceSet. Currently registered sets are $srcDirs",
        )
    }

    private fun assertResDirRegistered(
        resDir: String,
        setName: String = SourceSet.MAIN_SOURCE_SET_NAME,
    ) {
        val resDirs = getSourceSet(setName).res.srcDirs
        assertTrue(
            resDirs.contains(File(resDir)),
            "resDir $resDir is not registered in $setName sourceSet. Currently registered sets are $resDirs",
        )
    }

    private fun evaluateProject() {
        // evaluation is triggered internally
        project.getTasksByName("build", false)
    }

    private fun androidExtension(): BaseExtension = project.extensions.getByType(BaseExtension::class)

    private fun getSourceSet(name: String = SourceSet.MAIN_SOURCE_SET_NAME): AndroidSourceSet =
        project.extensions
            .getByType<AndroidConfig>()
            .sourceSets
            .getByName(name)
}
