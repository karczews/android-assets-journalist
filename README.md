# Android Asset File Generator
[![N|Solid](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/utilx/android-asset-file-generator/com.github.utilx.android-asset-file-generator.gradle.plugin/maven-metadata.xml.svg?label=gradle)](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/utilx/android-asset-file-generator/com.github.utilx.android-asset-file-generator.gradle.plugin/maven-metadata.xml.svg?label=gradle)

Android Asset File Generator is gradle plugin that can be used in Android project to generate listing of android asset files in certain library.

Given project contains following asset files
```
src/main/assets/testName.testExtension
src/main/assets/subdir/testName.testExtension
```
## Assets listing file can be generated in 3 forms
####  Android strings xml file
Pliugin can generate xml file in `build/generated/aafg/src/main/res/values/asset-strings.xml` as:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<resources>
  <string name="testName_testExtension">testName.testExtension</string>
  <string name="subdir_testName_testExtension">subDir/testName.testExtension</string>
</resources>
```
####  Java source class
Plugin can generate java file in `/build/generated/aafg/src/main/java/com/github/utilx/AssetFiles.java`
```java
public final class AssetFiles {
  public static final String asset_testName_testExtension = "testName.testExtension";
  public static final String asset_subdir_testName_testExtension = "subDir/testName.testExtension";
  }
```
#### Kotlin source class
Plugin generate kotlin file in `/build/generated/aafg/src/main/kotlin/com/github/utilx/AssetFiles.kt`
```kotlin
object Assets {
    const val asset_testName_testExtension = "testName.testExtension"
    const val asset_subdir_testName_testExtension = "subDir/testName.testExtension"
}
```
# Configuration
TODO
[gradle-plugin-repository]

License
----

Apache 2


   [gradle-plugin-repository]: <https://plugins.gradle.org/plugin/com.github.utilx.android-asset-file-generator>
