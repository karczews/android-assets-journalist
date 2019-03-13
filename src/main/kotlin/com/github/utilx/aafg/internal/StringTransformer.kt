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

package com.github.utilx.aafg.internal

import com.github.utilx.aafg.SourceFileConfig.Companion.CONST_VALUE_REPLACEMENT_EXPRESSION_MATCH_KEY
import com.github.utilx.aafg.SourceFileConfig.Companion.CONST_VALUE_REPLACEMENT_EXPRESSION_REPLACE_WITH_KEY
import java.lang.IllegalStateException

class StringTransformer(
    val replacementsList: List<Replacement>,
    val prefix: String = ""
) {
    /**
     * Applies transformation to provided string and returns result
     */
    fun apply(stringValue: String): String {
        var result = stringValue
        replacementsList.forEach { replacement ->
            result = result.replace(replacement.match, replacement.replaceWith)
        }
        result = prefix + result
        return result
    }
}

/**
 * Creates String transformer using provided replacement list from extension/task configuration
 */
fun buildStringTrasformerUsing(
    extensionReplacementList: List<Map<String, String>>,
    prefix: String = ""
): StringTransformer {
    val replacementsList = extensionReplacementList
        .asSequence()
        .map {

            val regex = it[CONST_VALUE_REPLACEMENT_EXPRESSION_MATCH_KEY]?.toRegex()
            val replacement = it[CONST_VALUE_REPLACEMENT_EXPRESSION_REPLACE_WITH_KEY]
            if (regex == null || replacement == null) {
                throw IllegalStateException("problem with building value replacement map - provided entries: $extensionReplacementList")
            }
            Replacement(regex, replacement)
        }
        .toList()

    return StringTransformer(replacementsList, prefix)
}