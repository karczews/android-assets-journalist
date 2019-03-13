/*
 *  Copyright (c) 2019-present, Android Asset File Generator Contributors.
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

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.github.utilx.assetsjournalist.java.JavaFileConfig
import com.github.utilx.assetsjournalist.kotlin.GenerateKotlinFileTask
import com.github.utilx.assetsjournalist.kotlin.KotlinFileConfig
import org.gradle.api.plugins.ExtensionAware
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class PluginTest {
    val project = ProjectBuilder.builder().build()

    @Test
    fun `should fail to apply if android plugin not applied`() {
        val exception = assertFails { project.pluginManager.apply(AssetsJournalistPlugin::class.java) }
        assertTrue { exception.cause is IllegalStateException }
    }

    @Test
    fun `testWIP`() {
        project.pluginManager.apply(AppPlugin::class.java)
        project.pluginManager.apply(AssetsJournalistPlugin::class.java)

        kotlinFileExtension().enabled = true
        androidExtension().compileSdkVersion(21)

        evaluateProject()

        println(project.tasks.names)
        project.tasks.findByPath("preBuild")

        assertTrue { project.tasks.findByPath("generateAssetsKotlinFileMain") is GenerateKotlinFileTask }
    }

    private fun evaluateProject() {
        //evaluation is triggered internally
        project.getTasksByName("build", false)
    }

    private fun androidExtension(): BaseExtension =
        project.extensions.findByType(BaseExtension::class.java)!!

    private fun pluginExtension(): AssetFileGeneratorConfig =
        project.extensions.findByType(AssetFileGeneratorConfig::class.java)!!

    private fun kotlinFileExtension(): KotlinFileConfig =
        ((pluginExtension() as ExtensionAware).extensions.findByType(KotlinFileConfig::class.java))!!

    private fun javaFileExtension(): JavaFileConfig =
        ((pluginExtension() as ExtensionAware).extensions.findByType(JavaFileConfig::class.java))!!

}