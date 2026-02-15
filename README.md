# Android Assets Journalist
[![N|Solid](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/utilx/android-assets-journalist/com.github.utilx.android-assets-journalist.gradle.plugin/maven-metadata.xml.svg?label=gradle)](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/utilx/android-assets-journalist/com.github.utilx.android-assets-journalist.gradle.plugin/maven-metadata.xml.svg?label=gradle)
[![CI](https://github.com/karczews/android-assets-journalist/actions/workflows/ci.yml/badge.svg)](https://github.com/karczews/android-assets-journalist/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/karczews/android-assets-journalist/branch/master/graph/badge.svg)](https://codecov.io/gh/karczews/android-assets-journalist)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=karczews_android-assets-journalist&metric=alert_status)](https://sonarcloud.io/dashboard?id=karczews_android-assets-journalist)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fkarczews%2Fandroid-assets-journalist.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fkarczews%2Fandroid-assets-journalist?ref=badge_shield)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/karczews/android-assets-journalist/blob/master/LICENSE)

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
Once plugin is applied, in order to configure plugin use `androidAssetsJournalist` extension. 
Following configuration presents all configuration options with default values or example values. Default values will be applied if not specified by integrator.
```groovy
androidAssetsJournalist {

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

## What's new:
1.0.0 - Major update to support modern Gradle and Android toolchain
- Updated to Gradle 9.3.1
- Updated Kotlin to 2.0.21
- Updated Android Gradle Plugin to 8.8.0
- Modernized build configuration
- Requires Java 17

0.11.0 - Added library/application variant support

## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fkarczews%2Fandroid-assets-journalist.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fkarczews%2Fandroid-assets-journalist?ref=badge_large)

   [gradle-plugin-repository]: <https://plugins.gradle.org/plugin/com.github.utilx.android-assets-journalist>
