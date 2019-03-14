# Android Assets Journalist
[![N|Solid](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/utilx/android-assets-journalist/com.github.utilx.android-assets-journalist.gradle.plugin/maven-metadata.xml.svg?label=gradle)](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/utilx/android-assets-journalist/com.github.utilx.android-assets-journalist.gradle.plugin/maven-metadata.xml.svg?label=gradle)
[![Build Status](https://travis-ci.org/karczews/android-assets-journalist.svg?branch=master)](https://travis-ci.org/karczews/android-assets-journalist)

Android Assets Journalist is gradle plugin that can be used in Android project to generate listing of android asset files in certain library.

## Assets listing file can be generated in 3 ways
####  Android strings xml file
Given project contains following asset files:
`src/main/assets/testName.testExtension`,
`src/main/assets/subdir/testName.testExtension`
Plugin can generate xml file in `build/generated/assetsjournalist/src/main/res/values/asset-strings.xml` as:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<resources>
  <string name="testName_testExtension">testName.testExtension</string>
  <string name="subdir_testName_testExtension">subDir/testName.testExtension</string>
</resources>
```
####  Java source class
Given project contains following asset files:
`src/main/assets/testName.testExtension`,
`src/main/assets/subdir/testName.testExtension`
Plugin can generate java file in `/build/generated/assetsjournalist/src/main/java/com/github/utilx/AssetFiles.java`
```java
public final class AssetFiles {
  public static final String ASSET_TESTNAME_TESTEXTENSION = "testName.testExtension";
  public static final String ASSET_SUBDIR_TESTNAME_TESTEXTENSION = "subDir/testName.testExtension";
}
```
#### Kotlin source class
Given project contains following asset files:
`src/main/assets/testName.testExtension`,
`src/main/assets/subdir/testName.testExtension`
Plugin generate kotlin file in `/build/generated/assetsjournalist/src/main/kotlin/com/github/utilx/AssetFiles.kt`
```kotlin
object AssetFiles {
    const val ASSET_TESTNAME_TESTEXTENSION = "testName.testExtension"
    const val ASSET_SUBDIR_TESTNAME_TESTEXTENSION = "subDir/testName.testExtension"
}
```
# How to apply plugin
[gradle-plugin-repository]
# How to configure plugin
Once plugin is applied, in order to configure plugin use `androidAssetFileGenerator` extension. 
Following configuration presents all configuration options with default values or example values. Default values will be applied if not specified by integrator.
```groovy
androidAssetsJournalist {
    // specifies which source sets in library should be used. If not specified, main is used
    sourceSets = [android.sourceSets.main]

    xmlFile {
        // enables xml string resource file generation
        enabled = false
        // prefix for each string name
        stringNamePrefix = "prefix_"
    }

    javaFile {
        // enable java source file generation
        enabled = true
        // class name to generate
        className = "AssetFiles"
        // package name for generated class
        packageName = "com.github.utilx"
        // prefix for each constant
        constNamePrefix = "asset_"
        // prefix to applt for each path, empty by default
        constValuePrefix = ""
        // regexp for replacing matching part of asset path, none by default
        replaceInAssetsPath = [ ]
    }

    kotlinFile {
        // enable kotlin source file generation
        enabled = false
        // class name to generate
        className = "AssetFilesKt"
        // package name for generated class
        packageName = "com.github.utilx"
        // prefix for each constant
        constNamePrefix = "asset_"
        // prefix to apply to each asset path, example:
        constValuePrefix = "assetFile://"
        // regexp for replacing matching part of asset path with provided replacement string
        replaceInAssetsPath = [
                // example1: replace each "ab" at the start of each path with "zx"
                [match: '^ab', replaceWith: 'zx'],
                // example2: replace each "da" or "db" with "cd"
                [match: 'd[ab]', replaceWith: 'cd']
            ]
        
    }
}
```

License
----

Apache 2


   [gradle-plugin-repository]: <https://plugins.gradle.org/plugin/com.github.utilx.android-assets-journalist>
