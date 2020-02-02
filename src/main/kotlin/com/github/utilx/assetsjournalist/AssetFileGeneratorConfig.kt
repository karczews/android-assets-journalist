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

import com.android.build.gradle.api.AndroidSourceSet
import com.github.utilx.assetsjournalist.java.JavaFileConfig
import com.github.utilx.assetsjournalist.kotlin.KotlinFileConfig
import com.github.utilx.assetsjournalist.xml.XmlFileConfig
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.util.ConfigureUtil

open class AssetFileGeneratorConfig {

    var sourceSets = emptyList<AndroidSourceSet>()

    val xmlFile = XmlFileConfig()
    val javaFile = JavaFileConfig()
    val kotlinFile = KotlinFileConfig()

    fun xmlFile(closure: Closure<*>) {
        ConfigureUtil.configure(closure, xmlFile)
    }

    fun xmlFile(action: Action<XmlFileConfig>) {
        action.execute(xmlFile)
    }

    fun javaFile(closure: Closure<*>) {
        ConfigureUtil.configure(closure, javaFile)
    }

    fun javaFile(action: Action<JavaFileConfig>) {
        action.execute(javaFile)
    }

    fun kotlinFile(closure: Closure<*>) {
        ConfigureUtil.configure(closure, kotlinFile)
    }

    fun kotlinFile(action: Action<KotlinFileConfig>) {
        action.execute(kotlinFile)
    }
}
