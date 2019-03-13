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

/**
 * Common extension class for all source code generating tasks
 */
abstract class SourceFileConfig {
    var enabled = false
    var className = "AssetFiles"
    var packageName = "com.github.utilx"
    var constNamePrefix = "asset_"

    var constValuePrefix = ""

    /**
     * constValueReplacementExpressions = [
     * [match: 'az', replaceWith: 'replaceAZ'],
     * [match: 'd', replaceWith: 'ds']
     * ]
     */
    var constValueReplacementExpressions = emptyList<Map<String, String>>()

    companion object {
        const val CONST_VALUE_REPLACEMENT_EXPRESSION_MATCH_KEY = "match"
        const val CONST_VALUE_REPLACEMENT_EXPRESSION_REPLACE_WITH_KEY = "replaceWith"
    }
}