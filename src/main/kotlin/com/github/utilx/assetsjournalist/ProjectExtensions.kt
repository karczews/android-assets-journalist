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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestExtension
import com.android.build.gradle.TestPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.hasPlugin

internal val Project.buildVariants: DomainObjectSet<out BaseVariant>
    get() {
        val plugins = project.plugins
        val extensions = project.extensions
        return when {
            plugins.hasPlugin(LibraryPlugin::class) -> extensions.getByType(LibraryExtension::class).libraryVariants
            plugins.hasPlugin(AppPlugin::class) -> extensions.getByType(AppExtension::class).applicationVariants
            plugins.hasPlugin(TestPlugin::class) -> extensions.getByType(TestExtension::class).applicationVariants
            else -> throw GradleException("Unsupported project type")
        }
    }
