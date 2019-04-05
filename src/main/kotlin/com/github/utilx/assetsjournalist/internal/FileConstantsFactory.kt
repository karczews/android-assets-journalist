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

import kotlin.math.absoluteValue

class FileConstantsFactory(
    private val constValuePrefix: String = "",
    private val constValueTransformer: StringTransformer,
    private val constNamePrefix: String = "",
    private val constNameTransformer: StringTransformer = StringTransformer(
        listOf(
            Replacement(
                DEFAULT_NOT_ALLOWED_CONST_NAME_CHAR_PATTERN.toRegex(),
                DEFAULT_NAME_REPLACEMENT_CHAR
            )
        )
    )
) {

    /**
     * Converts provided file path to source code constant value-pair using provided configuration
     */
    fun toConstNameValuePair(filePath: String): ConstNameValuePair {
        return filePath
            // Transform file path using supplied value transformer.
            // Transformed path will be used as base for constant name and value.
            .let { constValueTransformer.apply(it) }
            .let {
                val constName = constNamePrefix +
                        constNameTransformer.apply(it) +
                        DEFAULT_NAME_REPLACEMENT_CHAR +
                        it.hashCode().absoluteValue

                val constValue = constValuePrefix + constValueTransformer.apply(it)

                ConstNameValuePair(constName.toUpperCase(), constValue)
            }
    }

    companion object {
        const val DEFAULT_NOT_ALLOWED_CONST_NAME_CHAR_PATTERN = "[^A-Za-z0-9$]"
        const val DEFAULT_NAME_REPLACEMENT_CHAR = "_"
    }

    data class ConstNameValuePair(val name: String, val value: String)
}
