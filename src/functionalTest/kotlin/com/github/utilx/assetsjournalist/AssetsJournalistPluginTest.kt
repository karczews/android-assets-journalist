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

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths

class AssetsJournalistPluginTest {
    @TempDir
    lateinit var tempDir: File

    private val classpath = System.getProperty("java.class.path")
    private val testClasspath = classpath.split(File.pathSeparator.toRegex()).map { File(it) }

    @Test
    fun `Should register tasks`() {
        val resourceDirectory = Paths.get("src", "functionalTest", "testProject")

        // Setup the test build
        val projectDir = tempDir

        resourceDirectory.toFile().copyRecursively(projectDir, true)

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withPluginClasspath(runner.pluginClasspath + testClasspath)
        runner.withArguments("assembleFooDebug")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
    }
}
