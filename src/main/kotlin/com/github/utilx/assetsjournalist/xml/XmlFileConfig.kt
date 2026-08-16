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

package com.github.utilx.assetsjournalist.xml

open class XmlFileConfig {
    var enabled = false

    /**
     * Regex replacements applied to the asset path before it is turned into a string resource
     * name. Uses the same entry shape as `kotlinFile.replaceInAssetsPath`.
     *
     * stringNameCharMapping = [
     * [match: '/', replaceWith: '__'],
     * [match: '\\.', replaceWith: '_dot_']
     * ]
     *
     * Only the generated name is affected, the string value stays the original asset path.
     */
    var stringNameCharMapping = emptyList<Map<String, String>>()

    var stringNamePrefix = ""
}
