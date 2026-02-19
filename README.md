# Android Assets Journalist 🗂️

[![Gradle Plugin](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/utilx/android-assets-journalist/com.github.utilx.android-assets-journalist.gradle.plugin/maven-metadata.xml.svg?label=gradle)](https://plugins.gradle.org/plugin/com.github.utilx.android-assets-journalist)
[![CI](https://github.com/karczews/android-assets-journalist/actions/workflows/ci.yml/badge.svg)](https://github.com/karczews/android-assets-journalist/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/karczews/android-assets-journalist/branch/main/graph/badge.svg)](https://codecov.io/gh/karczews/android-assets-journalist)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fkarczews%2Fandroid-assets-journalist.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fkarczews%2Fandroid-assets-journalist?ref=badge_shield)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/karczews/android-assets-journalist/blob/main/LICENSE)

**Stop using error-prone string paths. Get compile-time safety and IDE autocomplete for your Android assets.**

Android Assets Journalist is a Gradle plugin that automatically generates type-safe constants for all your Android `assets/` files. No more typos, no more guessing paths, no more runtime crashes.

## The Problem 😫

```kotlin
// ❌ Error-prone string paths
val modelPath = "models/tesorflow_lite_model.tflite"  // Oops, typo!
val jsonPath = "configs/app_config.json"  // Where is this used? IDE can't find it

// ❌ Runtime crashes when files are renamed or moved
assets.open("old_path/file.json")  // Crash!

// ❌ No autocomplete - you have to remember every path
assets.open("???")  // Good luck remembering
```

## The Solution ✨

```kotlin
// ✅ Type-safe, generated constants
val modelPath = AssetFiles.MODELS_TENSORFLOW_LITE_MODEL_TFLITE
val jsonPath = AssetFiles.CONFIGS_APP_CONFIG_JSON

// ✅ Refactor-safe rename support
assets.open(AssetFiles.MODELS_TENSORFLOW_LITE_MODEL_TFLITE)  // IDE knows this!

// ✅ Full IDE autocomplete support
AssetFiles.  // Press Ctrl+Space and see all assets
```

## Perfect For 🎯

- **🎮 Games** - Managing sprite sheets, audio files, level data, game assets
- **🧠 Machine Learning** - TensorFlow Lite models, ONNX files, ML configs
- **🌐 WebView Apps** - Local HTML, CSS, JavaScript files
- **📄 Document Viewers** - PDF templates, custom fonts, document resources
- **📦 Asset-Heavy Apps** - Any app with 10+ files in `assets/`
- **🔧 Configuration-Heavy Apps** - JSON configs, XML schemas, property files

## Why Choose Android Assets Journalist? 🏆

| Feature | Benefit |
|---------|---------|
| **🔒 Compile-time safety** | Catch typos at build time, not runtime crashes |
| **🤖 IDE autocomplete** | Press Ctrl+Space and see all your assets instantly |
| **🔄 Refactoring support** | Rename assets safely - IDE updates all references |
| **⚡ Zero runtime overhead** | Generated constants, no reflection or runtime cost |
| **📱 AGP 8.x compatible** | Works with Android Gradle Plugin 8.8.0+ |
| **🎨 Multiple output formats** | XML strings or Kotlin constants |
| **⚙️ Highly configurable** | Prefixes, path transformations, filtering |

## Quick Start 🚀

### 1. Apply the plugin

```kotlin
// build.gradle.kts (plugins block)
plugins {
    id("com.github.utilx.android-assets-journalist") version "1.0.0"
}
```

### 2. Configure (optional)

```kotlin
androidAssetsJournalist {
    // Generate Kotlin constants (default: enabled)
    kotlinFile {
        enabled = true
        className = "AssetFiles"
        packageName = "com.yourcompany.yourapp"
    }
    
    // Or Android string resources
    xmlFile {
        enabled = false
    }
}
```

### 3. Build and use

```bash
./gradlew assembleDebug
```

```kotlin
import com.yourcompany.yourapp.AssetFiles

// Access any asset with type-safe constants
val model = AssetFiles.MODELS_ML_MODEL_TFLITE
val config = AssetFiles.CONFIGS_SETTINGS_JSON
```

## Generated Output Examples 📄

Given these assets:
```
src/main/assets/
├── models/
│   └── ml_model.tflite
└── configs/
    └── settings.json
```

### Kotlin Output
```kotlin
// AssetFiles.kt
package com.github.utilx

object AssetFiles {
    const val MODELS_ML_MODEL_TFLITE: String = "models/ml_model.tflite"
    const val CONFIGS_SETTINGS_JSON: String = "configs/settings.json"
}
```

### XML Output
```xml
<!-- res/values/assets-strings.xml -->
<resources>
    <string name="models_ml_model_tflite">models/ml_model.tflite</string>
    <string name="configs_settings_json">configs/settings.json</string>
</resources>
```

## Advanced Configuration ⚙️

### Custom Prefixes

```kotlin
androidAssetsJournalist {
    kotlinFile {
        className = "Assets"
        packageName = "com.myapp.util"
        constNamePrefix = "ASSET_"
        constValuePrefix = "file:///android_asset/"
    }
}
```

**Output:**
```kotlin
const val ASSET_MODELS_ML_MODEL_TFLITE: String = "file:///android_asset/models/ml_model.tflite"
```

### Path Transformations

```kotlin
androidAssetsJournalist {
    kotlinFile {
        // Replace "dev_" prefix with "prod_" in generated paths
        replaceInAssetsPath = [
            [match: '^dev_', replaceWith: 'prod_']
        ]
    }
}
```

## Configuration Reference 📖

```kotlin
androidAssetsJournalist {
    // XML String Resources
    xmlFile {
        enabled = false  // Enable to generate res/values/*.xml
        stringNamePrefix = "prefix_"
    }

    // Kotlin Constants Object
    kotlinFile {
        enabled = false
        className = "AssetFilesKt"
        packageName = "com.github.utilx"
        constNamePrefix = "asset_"
        constValuePrefix = ""
        replaceInAssetsPath = [
            [match: '^dev_', replaceWith: 'prod_'],
            [match: 'test_', replaceWith: '']
        ]
    }
}
```

## Requirements ✅

- **Java**: 17 or higher
- **Android Gradle Plugin**: 8.0.0 or higher
- **Gradle**: 8.0 or higher
- **Kotlin**: 1.9.0 or higher (for Kotlin DSL)

## Migration from 0.11.x to 1.0.0 🔄

### Breaking Changes
- **Java 17 is now required** (was Java 8)
- **AGP 8.x namespace required** - Ensure your `build.gradle` has:
  ```kotlin
  android {
      namespace = "com.yourcompany.yourapp"
      compileSdk = 35
  }
  ```

### What's New in 1.0.0 🎉

This release brings full compatibility with Android Gradle Plugin 8.x and modern toolchain:

**Highlights:**
- ✅ **AGP 8.8.0 support** - Full compatibility with latest Android build tools
- ✅ **Gradle 9.3.1** - Updated to latest Gradle version
- ✅ **Kotlin 2.0.21** - Modern Kotlin support
- ✅ **Java 17** - Requires Java 17 (aligned with AGP 8.x requirements)
- ✅ **Namespace support** - Properly handles AGP 8.x namespace requirements
- ✅ **GitHub Actions CI** - Migrated from Travis CI

### 0.11.0 - Added library/application variant support

## License

[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fkarczews%2Fandroid-assets-journalist.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Fkarczews%2Fandroid-assets-journalist?ref=badge_large)

[Gradle Plugin Portal]: <https://plugins.gradle.org/plugin/com.github.utilx.android-assets-journalist>
