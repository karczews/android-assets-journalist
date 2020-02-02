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

import com.github.utilx.assetsjournalist.java.JavaFileConfig
import com.github.utilx.assetsjournalist.kotlin.KotlinFileConfig
import com.github.utilx.assetsjournalist.xml.XmlFileConfig
import org.gradle.kotlin.dsl.closureOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AssetFileGeneratorConfigTest {

    private val tested = AssetFileGeneratorConfig()

    @Nested
    inner class Java {
        @Test
        fun shouldBeDisabledByDefault() {
            assertFalse { tested.javaFile.enabled }
        }

        @Test
        fun shouldConfigureViaClosure() {
            // given
            val closure = closureOf<JavaFileConfig> { enabled = true }

            // when
            tested.javaFile(closure)

            // then
            assertTrue { tested.javaFile.enabled }
        }
    }

    @Nested
    inner class Kotlin {
        @Test
        fun shouldBeDisabledByDefault() {
            assertFalse { tested.kotlinFile.enabled }
        }

        @Test
        fun shouldConfigureViaClosure() {
            // given
            val closure = closureOf<KotlinFileConfig> { enabled = true }

            // when
            tested.kotlinFile(closure)

            // then
            assertTrue { tested.kotlinFile.enabled }
        }
    }

    @Nested
    inner class Xml {
        @Test
        fun shouldBeDisabledByDefault() {
            assertFalse { tested.xmlFile.enabled }
        }

        @Test
        fun shouldConfigureViaClosure() {
            // given
            val closure = closureOf<XmlFileConfig> { enabled = true }

            // when
            tested.xmlFile(closure)

            // then
            assertTrue { tested.xmlFile.enabled }
        }
    }
}