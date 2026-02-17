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
import org.gradle.api.Action
import org.gradle.kotlin.dsl.closureOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
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

        @Test
        fun shouldConfigureViaAction() {
            // given
            val action =
                Action<JavaFileConfig> {
                    enabled = true
                    className = "TestJavaAssets"
                    packageName = "com.test.java"
                    constNamePrefix = "JAVA_PREFIX_"
                    constValuePrefix = "java://"
                }

            // when
            tested.javaFile(action)

            // then
            assertTrue { tested.javaFile.enabled }
            assertEquals("TestJavaAssets", tested.javaFile.className)
            assertEquals("com.test.java", tested.javaFile.packageName)
            assertEquals("JAVA_PREFIX_", tested.javaFile.constNamePrefix)
            assertEquals("java://", tested.javaFile.constValuePrefix)
        }

        @Test
        fun shouldConfigureReplacementsViaAction() {
            // given
            val replacements =
                listOf(
                    mapOf("match" to "^test", "replaceWith" to "prod"),
                )
            val action =
                Action<JavaFileConfig> {
                    replaceInAssetsPath = replacements
                }

            // when
            tested.javaFile(action)

            // then
            assertEquals(replacements, tested.javaFile.replaceInAssetsPath)
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

        @Test
        fun shouldConfigureViaAction() {
            // given
            val action =
                Action<KotlinFileConfig> {
                    enabled = true
                    className = "TestKotlinAssets"
                    packageName = "com.test.kotlin"
                    constNamePrefix = "KT_PREFIX_"
                    constValuePrefix = "kt://"
                }

            // when
            tested.kotlinFile(action)

            // then
            assertTrue { tested.kotlinFile.enabled }
            assertEquals("TestKotlinAssets", tested.kotlinFile.className)
            assertEquals("com.test.kotlin", tested.kotlinFile.packageName)
            assertEquals("KT_PREFIX_", tested.kotlinFile.constNamePrefix)
            assertEquals("kt://", tested.kotlinFile.constValuePrefix)
        }

        @Test
        fun shouldConfigureReplacementsViaAction() {
            // given
            val replacements =
                listOf(
                    mapOf("match" to "^az", "replaceWith" to "replacekt"),
                    mapOf("match" to "d[abc]", "replaceWith" to "kt"),
                )
            val action =
                Action<KotlinFileConfig> {
                    replaceInAssetsPath = replacements
                }

            // when
            tested.kotlinFile(action)

            // then
            assertEquals(replacements, tested.kotlinFile.replaceInAssetsPath)
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

        @Test
        fun shouldConfigureViaAction() {
            // given
            val action =
                Action<XmlFileConfig> {
                    enabled = true
                    stringNamePrefix = "xml_prefix_"
                }

            // when
            tested.xmlFile(action)

            // then
            assertTrue { tested.xmlFile.enabled }
            assertEquals("xml_prefix_", tested.xmlFile.stringNamePrefix)
        }

        @Test
        fun shouldConfigureStringNameCharMappingViaAction() {
            // given
            val charMapping =
                listOf(
                    mapOf("from" to "/", "to" to "_"),
                )
            val action =
                Action<XmlFileConfig> {
                    stringNameCharMapping = charMapping
                }

            // when
            tested.xmlFile(action)

            // then
            assertEquals(charMapping, tested.xmlFile.stringNameCharMapping)
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun shouldAllowMultipleConfigurationCalls() {
            // given
            val action1 =
                Action<KotlinFileConfig> {
                    enabled = true
                    className = "FirstClassName"
                }
            val action2 =
                Action<KotlinFileConfig> {
                    className = "SecondClassName"
                    packageName = "com.test"
                }

            // when
            tested.kotlinFile(action1)
            tested.kotlinFile(action2)

            // then - second call should override
            assertTrue { tested.kotlinFile.enabled }
            assertEquals("SecondClassName", tested.kotlinFile.className)
            assertEquals("com.test", tested.kotlinFile.packageName)
        }

        @Test
        fun shouldHandleEmptyReplacementsListViaAction() {
            // given
            val action =
                Action<JavaFileConfig> {
                    replaceInAssetsPath = emptyList()
                }

            // when
            tested.javaFile(action)

            // then
            assertTrue(tested.javaFile.replaceInAssetsPath.isEmpty())
        }
    }
}
