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

package com.github.utilx.assetsjournalist.common

import assertk.assertThat
import assertk.assertions.endsWith
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
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

        val factory =
            createFactory(
                constValueTransformer =
                    StringTransformer(
                        listOf(replacement1, replacement2),
                    ),
            )

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

        val factory =
            createFactory(
                constNameTransformer =
                    StringTransformer(
                        listOf(replacement1, replacement2),
                    ),
            )

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
        val factory =
            FileConstantsFactory(
                constValuePrefix = "",
                constValueTransformer =
                    StringTransformer(
                        emptyList(),
                    ),
                constNamePrefix = "",
            )

        // when
        val result = factory.toConstNameValuePair("folder/f file.txt")

        // then
        assertThat(result.name).startsWith(expectedValue)
    }

    @Test
    @DisplayName("Should handle empty file path")
    fun shouldHandleEmptyFilePath() {
        // given
        val factory = createFactory()

        // when
        val result = factory.toConstNameValuePair("")

        // then
        assertThat(result.name).endsWith("_0") // empty string hashcode is 0
        assertThat(result.value).isEqualTo("")
    }

    @Test
    @DisplayName("Should handle file path with only special characters")
    fun shouldHandleSpecialCharactersOnly() {
        // given
        val factory = createFactory()

        // when
        val result = factory.toConstNameValuePair("@#$%^&*()")

        // then
        // All special chars should be replaced with underscores, then uppercased
        // Note: $ is NOT replaced (it's allowed in the pattern), so we get "__$_______"
        assertThat(result.name).startsWith("__" + "$" + "_______")
    }

    @Test
    @DisplayName("Should handle very long file paths")
    fun shouldHandleVeryLongFilePaths() {
        // given
        val longPath = "a/".repeat(100) + "file.txt"
        val factory = createFactory()

        // when
        val result = factory.toConstNameValuePair(longPath)

        // then
        assertThat(result.value).isEqualTo(longPath)
        assertThat(result.name).endsWith("_${longPath.hashCode().absoluteValue}")
    }

    @Test
    @DisplayName("Should generate different names for different paths with same base name")
    fun shouldGenerateDifferentNamesForDifferentPaths() {
        // given
        val factory = createFactory()

        // when
        val result1 = factory.toConstNameValuePair("dir1/file.txt")
        val result2 = factory.toConstNameValuePair("dir2/file.txt")

        // then - names should be different due to different hashcodes
        assertThat(result1.name).isNotEqualTo(result2.name)
        // The hashcode suffix should be different
        val hash1 = "dir1/file.txt".hashCode().absoluteValue
        val hash2 = "dir2/file.txt".hashCode().absoluteValue
        assertThat(result1.name).endsWith("_$hash1")
        assertThat(result2.name).endsWith("_$hash2")
    }

    @Test
    @DisplayName("Should handle paths with dots and hyphens")
    fun shouldHandlePathsWithDotsAndHyphens() {
        // given
        val factory = createFactory()

        // when
        val result = factory.toConstNameValuePair("my-file.test.txt")

        // then
        // Dots and hyphens are replaced with underscores, then uppercased
        assertThat(result.name).startsWith("MY_FILE_TEST_TXT")
        assertThat(result.value).isEqualTo("my-file.test.txt")
    }

    @Test
    @DisplayName("Should uppercase constant names")
    fun shouldUppercaseConstantNames() {
        // given
        val factory = createFactory()

        // when
        val result = factory.toConstNameValuePair("lowercase/path.txt")

        // then
        // Dots are replaced with underscores, then everything is uppercased
        assertThat(result.name).startsWith("LOWERCASE_PATH_TXT")
    }

    @Test
    @DisplayName("Should handle consecutive special characters")
    fun shouldHandleConsecutiveSpecialCharacters() {
        // given
        val factory = createFactory()

        // when
        val result = factory.toConstNameValuePair("file///name...txt")

        // then
        // Both slashes and dots are replaced with underscores, then uppercased
        assertThat(result.name).startsWith("FILE___NAME___TXT")
        assertThat(result.value).isEqualTo("file///name...txt")
    }

    private fun createFactory(
        constValuePrefix: String = "",
        constValueTransformer: StringTransformer =
            StringTransformer(
                emptyList(),
            ),
        constNamePrefix: String = "",
        constNameTransformer: StringTransformer =
            StringTransformer(
                listOf(
                    Replacement(
                        FileConstantsFactory.DEFAULT_NOT_ALLOWED_CONST_NAME_CHAR_PATTERN.toRegex(),
                        FileConstantsFactory.DEFAULT_NAME_REPLACEMENT_CHAR,
                    ),
                ),
            ),
    ) = FileConstantsFactory(
        constValuePrefix = constValuePrefix,
        constValueTransformer = constValueTransformer,
        constNamePrefix = constNamePrefix,
        constNameTransformer = constNameTransformer,
    )
}
