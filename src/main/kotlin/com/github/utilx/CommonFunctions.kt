package com.github.utilx

import com.android.build.gradle.api.AndroidSourceSet

internal fun listAssetsIn(sourceSet: AndroidSourceSet): List<String> {
    return sourceSet
        .assets
        .sourceDirectoryTrees
        .flatMap { assetFileTree ->
            val assetBaseDir = assetFileTree.dir
            assetFileTree.asFileTree.files
                .map { it.relativeTo(assetBaseDir) }
                .map { it.toString() }
        }
}