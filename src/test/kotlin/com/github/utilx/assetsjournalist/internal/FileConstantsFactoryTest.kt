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

package com.github.utilx.assetsjournalist.internal

import assertk.assertThat
import assertk.assertions.endsWith
import assertk.assertions.isEqualTo
import assertk.assertions.startsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue

class FileConstantsFactoryTest {

    @Test
    fun shouldPrefixValuesCorrectly() {
        // given
        val expectedPrefix = "testPrefix"
        val factory = createFactory(constValuePrefix = expectedPrefix)

        // when
        val result = factory.toConstNameValuePair("assets/file.txt")

        // then
        assertThat(result.value).startsWith(expectedPrefix)
    }

    @Test
    fun shouldPrefixNameCorrectly() {
        // given
        val expectedPrefix = "testPrefix"
        val factory = createFactory(constNamePrefix = expectedPrefix)

        // when
        val result = factory.toConstNameValuePair("assets/file.txt")

        // then
        assertThat(result.name).startsWith(expectedPrefix, ignoreCase = true)
    }

    @Test
    fun shouldTransformValuesCorrectly() {
        // given
        val replacement1 = Replacement("^a".toRegex(), "b")
        val replacement2 = Replacement("^b".toRegex(), "c")

        val factory = createFactory(constValueTransformer = StringTransformer(listOf(replacement1, replacement2)))

        // when
        val result = factory.toConstNameValuePair("afolder/file.txt")

        // then transform starting a -> b -> c
        assertThat(result.value).isEqualTo("cfolder/file.txt")
    }

    @Test
    fun shouldTransformNamesCorrectly() {
        // given
        val replacement1 = Replacement("^a".toRegex(), "b")
        val replacement2 = Replacement("^b".toRegex(), "c")

        val factory = createFactory(constNameTransformer = StringTransformer(listOf(replacement1, replacement2)))

        // when
        val result = factory.toConstNameValuePair("afolder/file.txt")

        // then transform starting a -> b -> c
        assertThat(result.name).startsWith("cfolder/file.txt", ignoreCase = true)
    }

    @Test
    fun shouldSuffixNameWithHashcode() {
        // given
        val originalPath = "assets/file.txt"
        val expectedHash = originalPath.hashCode().absoluteValue
        val factory = createFactory()

        // when
        val result = factory.toConstNameValuePair(originalPath)

        // then
        assertThat(result.name).endsWith("_" + expectedHash.toString())
    }

    @Test
    @DisplayName("Should replace not allowed chars when generating source code const names")
    fun shouldReplaceNotAllowedCharsInNameByDefault() {
        // given
        val expectedValue = "FOLDER_F_FILE_TXT"
        val factory = FileConstantsFactory(
            constValuePrefix = "",
            constValueTransformer = StringTransformer(emptyList()),
            constNamePrefix = ""
        )

        // when
        val result = factory.toConstNameValuePair("folder/f file.txt")

        // then
        assertThat(result.name).startsWith(expectedValue)
    }

    private fun createFactory(
        constValuePrefix: String = "",
        constValueTransformer: StringTransformer = StringTransformer(emptyList()),
        constNamePrefix: String = "",
        constNameTransformer: StringTransformer = StringTransformer(emptyList())
    ) = FileConstantsFactory(
        constValuePrefix = constValuePrefix,
        constValueTransformer = constValueTransformer,
        constNamePrefix = constNamePrefix,
        constNameTransformer = constNameTransformer
    )
}