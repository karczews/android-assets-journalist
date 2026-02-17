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

package com.github.utilx.playground

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.utilx.AssetFilesJava
import com.github.utilx.AssetFilesKotlin
import com.github.utilx.testapp.R

private const val TAG = "LOG"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TEST", getString(R.string.prefix_az_123_txt_1028221614))

        Log.d(TAG, "Java asset file smoketest 1 ${AssetFilesJava.CONSTPREFIXJAVA_REPLACEJAVA_123_TXT_2038126485}")
        Log.d(TAG, "Java asset file smoketest 2 ${AssetFilesJava.CONSTPREFIXJAVA_ROOT_JAVA_TXT_1351089311}")

        Log.d(TAG, "Kotlin asset file smoketest 1 ${AssetFilesKotlin.CONSTPREFIXKT_REPLACEKT_123_TXT_371122798}")
        Log.d(TAG, "Kotlin asset file smoketest 2 ${AssetFilesKotlin.CONSTPREFIXKT_ROOT_KT_TXT_203164056}")

        setContentView(R.layout.activity_main)
    }
}
