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

import com.github.utilx.assetsjournalist.SourceFileConfig.Companion.CONST_VALUE_REPLACEMENT_EXPRESSION_MATCH_KEY
import com.github.utilx.assetsjournalist.SourceFileConfig.Companion.CONST_VALUE_REPLACEMENT_EXPRESSION_REPLACE_WITH_KEY
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
        val transformer = buildStringTransformerUsing(listOf(
            replacementEntry {
                match = "^a"
                replaceWith = "b"
            }
        ))

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

        val transformer = StringTransformer(
            listOf(
                Replacement("^a".toRegex(), "b"),
                Replacement("^b".toRegex(), "c")
            )
        )

        // when
        val actual = transformer(inputValue)

        // then
        assertEquals(expectedValue, actual)
    }
}

private data class ReplacementEntry(var match: String = "", var replaceWith: String = "") {
    fun asMap(): Map<String, String> {
        return mapOf(
            CONST_VALUE_REPLACEMENT_EXPRESSION_MATCH_KEY to match,
            CONST_VALUE_REPLACEMENT_EXPRESSION_REPLACE_WITH_KEY to replaceWith
        )
    }
}

/**
 * Creates replacement entry map similar to one provided via plugin extension
 */
private fun replacementEntry(block: ReplacementEntry.() -> Unit): Map<String, String> =
    ReplacementEntry()
        .apply(block)
        .asMap()
