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

import com.github.utilx.assetsjournalist.SourceFileConfig.Companion.CONST_VALUE_REPLACEMENT_EXPRESSION_MATCH_KEY
import com.github.utilx.assetsjournalist.SourceFileConfig.Companion.CONST_VALUE_REPLACEMENT_EXPRESSION_REPLACE_WITH_KEY
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StringTransformerTest {
    @Test
    @DisplayName("Configuration from plugin extension should be processed correctly")
    fun shouldCreateCorrectlyConfiguredTransformerFromExtension() {
        // given - starting a will be replaced by b
        val inputValue = "aaab"
        val expectedValue = "baab"
        val transformer =
            buildStringTransformerUsing(
                listOf(
                    replacementEntry {
                        match = "^a"
                        replaceWith = "b"
                    },
                ),
            )

        // when
        val actual = transformer(inputValue)

        // then
        assertEquals(expectedValue, actual)
    }

    @Test
    @DisplayName("String replacements should be applied in correct order")
    fun shouldApplyReplacementsInCorrectOrder() {
        // given
        val inputValue = "aaab"
        val expectedValue = "caab"

        val transformer =
            StringTransformer(
                listOf(
                    Replacement("^a".toRegex(), "b"),
                    Replacement("^b".toRegex(), "c"),
                ),
            )

        // when
        val actual = transformer(inputValue)

        // then
        assertEquals(expectedValue, actual)
    }

    @Test
    @DisplayName("Should handle empty replacement list")
    fun shouldHandleEmptyReplacementList() {
        // given
        val inputValue = "test/file.txt"
        val transformer = StringTransformer(emptyList())

        // when
        val actual = transformer(inputValue)

        // then
        assertEquals(inputValue, actual)
    }

    @Test
    @DisplayName("Should handle no matches")
    fun shouldHandleNoMatches() {
        // given
        val inputValue = "test/file.txt"
        val transformer =
            StringTransformer(
                listOf(Replacement("xyz".toRegex(), "abc")),
            )

        // when
        val actual = transformer(inputValue)

        // then
        assertEquals(inputValue, actual)
    }

    @Test
    @DisplayName("Should handle multiple matches in same string")
    fun shouldHandleMultipleMatchesInSameString() {
        // given
        val inputValue = "test_test_file"
        val transformer =
            StringTransformer(
                listOf(Replacement("test".toRegex(), "prod")),
            )

        // when
        val actual = transformer(inputValue)

        // then
        assertEquals("prod_prod_file", actual)
    }

    @Test
    @DisplayName("Should handle empty string input")
    fun shouldHandleEmptyStringInput() {
        // given
        val transformer =
            StringTransformer(
                listOf(Replacement("test".toRegex(), "prod")),
            )

        // when
        val actual = transformer("")

        // then
        assertEquals("", actual)
    }

    @Test
    @DisplayName("Should handle complex regex patterns")
    fun shouldHandleComplexRegexPatterns() {
        // given
        val inputValue = "file123.txt"
        val transformer =
            StringTransformer(
                listOf(Replacement("\\d+".toRegex(), "NUM")),
            )

        // when
        val actual = transformer(inputValue)

        // then
        assertEquals("fileNUM.txt", actual)
    }

    @Test
    @DisplayName("Should handle replacement with empty string")
    fun shouldHandleReplacementWithEmptyString() {
        // given
        val inputValue = "test/file.txt"
        val transformer =
            StringTransformer(
                listOf(Replacement("/".toRegex(), "")),
            )

        // when
        val actual = transformer(inputValue)

        // then
        assertEquals("testfile.txt", actual)
    }

    @Test
    @DisplayName("Should throw exception for invalid replacement configuration")
    fun shouldThrowExceptionForInvalidConfig() {
        // given - missing match key
        val invalidConfig =
            listOf(
                mapOf(CONST_VALUE_REPLACEMENT_EXPRESSION_REPLACE_WITH_KEY to "replacement"),
            )

        // when/then
        assertThrows(IllegalStateException::class.java) {
            buildStringTransformerUsing(invalidConfig)
        }
    }

    @Test
    @DisplayName("Should throw exception for missing replaceWith in configuration")
    fun shouldThrowExceptionForMissingReplaceWith() {
        // given - missing replaceWith key
        val invalidConfig =
            listOf(
                mapOf(CONST_VALUE_REPLACEMENT_EXPRESSION_MATCH_KEY to "test"),
            )

        // when/then
        assertThrows(IllegalStateException::class.java) {
            buildStringTransformerUsing(invalidConfig)
        }
    }

    @Test
    @DisplayName("Should handle multiple complex replacements")
    fun shouldHandleMultipleComplexReplacements() {
        // given
        val inputValue = "dir1/subdir2/file3.txt"
        val transformer =
            buildStringTransformerUsing(
                listOf(
                    replacementEntry {
                        match = "\\d+"
                        replaceWith = "X"
                    },
                    replacementEntry {
                        match = "/"
                        replaceWith = "_"
                    },
                ),
            )

        // when
        val actual = transformer(inputValue)

        // then
        assertEquals("dirX_subdirX_fileX.txt", actual)
    }

    @Test
    @DisplayName("Should use invoke operator")
    fun shouldUseInvokeOperator() {
        // given
        val transformer =
            StringTransformer(
                listOf(Replacement("test".toRegex(), "prod")),
            )

        // when - using invoke operator
        val actual = transformer.invoke("test/file.txt")

        // then
        assertEquals("prod/file.txt", actual)
    }
}

private data class ReplacementEntry(
    var match: String = "",
    var replaceWith: String = "",
) {
    fun asMap(): Map<String, String> =
        mapOf(
            CONST_VALUE_REPLACEMENT_EXPRESSION_MATCH_KEY to match,
            CONST_VALUE_REPLACEMENT_EXPRESSION_REPLACE_WITH_KEY to replaceWith,
        )
}

/**
 * Creates replacement entry map similar to one provided via plugin extension
 */
private fun replacementEntry(block: ReplacementEntry.() -> Unit): Map<String, String> =
    ReplacementEntry()
        .apply(block)
        .asMap()
