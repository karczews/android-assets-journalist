# Android Assets Journalist

[![Gradle Plugin](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/utilx/android-assets-journalist/com.github.utilx.android-assets-journalist.gradle.plugin/maven-metadata.xml.svg?label=gradle)](https://plugins.gradle.org/plugin/com.github.utilx.android-assets-journalist)
[![CI](https://github.com/karczews/android-assets-journalist/actions/workflows/ci.yml/badge.svg)](https://github.com/karczews/android-assets-journalist/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/karczews/android-assets-journalist/branch/main/graph/badge.svg)](https://codecov.io/gh/karczews/android-assets-journalist)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Fkarczews%2Fandroid-assets-journalist.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Fkarczews%2Fandroid-assets-journalist?ref=badge_shield)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/karczews/android-assets-journalist/blob/main/LICENSE)

Generate Kotlin constants or XML string resources from files in your Android `assets/` directories. Use generated names in code and keep raw asset paths out of call sites.

## Example

If your module contains:

```text
src/main/assets/
├── models/
│   └── ml_model.tflite
└── configs/
    └── settings.json
```

you can replace raw strings like this:

```kotlin
assets.open("models/ml_model.tflite")
assets.open("configs/settings.json")
```

with generated constants:

```kotlin
import com.yourcompany.yourapp.assets.AssetFiles

assets.open(AssetFiles.ASSET_MODELS_ML_MODEL_TFLITE_527533696)
assets.open(AssetFiles.ASSET_CONFIGS_SETTINGS_JSON_2053859403)
```

Expect a hash suffix on each generated name. The suffix keeps names unique when two paths normalize to the same identifier.

## Install

The Gradle badge above and the [Gradle Plugin Portal][Gradle Plugin Portal] show the latest published version.

Apply the plugin after `com.android.application` or `com.android.library` in the Android module that owns the `assets/` directory.

```kotlin
plugins {
    id("com.android.application")
    // or id("com.android.library")
    id("com.github.utilx.android-assets-journalist") version "<latest-version>"
}
```

## Configure Kotlin Output

If you leave both outputs disabled, the plugin generates Kotlin output. Unless you set `packageName`, it writes the generated file to `com.github.utilx`.

```kotlin
androidAssetsJournalist {
    kotlinFile {
        enabled = true
        packageName = "com.yourcompany.yourapp.assets"
        className = "AssetFiles"
        constNamePrefix = "ASSET_"
    }

    xmlFile {
        enabled = false
    }
}
```

## Configure XML Output

Enable XML generation when you want Android string resources instead of Kotlin constants.

```kotlin
androidAssetsJournalist {
    xmlFile {
        enabled = true
        stringNamePrefix = "asset_"
    }

    kotlinFile {
        enabled = false
    }
}
```

Use `stringNameCharMapping` when you want control over how path characters become resource name
characters. Replacements run before the plugin sanitizes whatever is left.

```kotlin
androidAssetsJournalist {
    xmlFile {
        enabled = true
        stringNameCharMapping = listOf(
            mapOf("match" to "/", "replaceWith" to "__")
        )
    }
}
```

```xml
<string name="configs__settings_json_2053859403">configs/settings.json</string>
```

## Build

Run your usual Android build.

```bash
./gradlew assembleDebug
```

Gradle registers variant-aware tasks such as `generateAssetsKotlinFileDebug` and `generateAssetsXmlFileDebug`.

You will find generated files under `build/generated/assetsjournalist/src/<variant>/`:

- Kotlin: `<variant>/kotlin/<package as directories>/<className>.kt`
- XML: `<variant>/res/values/assets-strings.xml`

## Generated Output

With the Kotlin config above, you get:

```kotlin
package com.yourcompany.yourapp.assets

object AssetFiles {
    const val ASSET_MODELS_ML_MODEL_TFLITE_527533696 = "models/ml_model.tflite"
    const val ASSET_CONFIGS_SETTINGS_JSON_2053859403 = "configs/settings.json"
}
```

With XML output enabled, you get:

```xml
<resources>
    <string name="asset_models_ml_model_tflite_527533696">models/ml_model.tflite</string>
    <string name="asset_configs_settings_json_2053859403">configs/settings.json</string>
</resources>
```

## Path Transforms And Value Prefixes

These options are Kotlin only. `xmlFile` has no value prefix and no `replaceInAssetsPath`; its
closest equivalent is `stringNameCharMapping`, which rewrites the resource name but never the
string value.

Use `replaceInAssetsPath` when you want the generated name and value to use a transformed path.

```kotlin
androidAssetsJournalist {
    kotlinFile {
        enabled = true
        replaceInAssetsPath = listOf(
            mapOf("match" to "^dev_", "replaceWith" to "prod_")
        )
    }
}
```

Use `constValuePrefix` when you need values such as `file:///android_asset/...`.

```kotlin
androidAssetsJournalist {
    kotlinFile {
        enabled = true
        className = "Assets"
        packageName = "com.yourcompany.yourapp.assets"
        constNamePrefix = "ASSET_"
        constValuePrefix = "file:///android_asset/"
    }
}
```

```kotlin
const val ASSET_MODELS_ML_MODEL_TFLITE_527533696 =
    "file:///android_asset/models/ml_model.tflite"
```

## How Names Are Built

Both generators sanitize the asset path and append a hash suffix, but the rules differ.

| | Kotlin constant | XML string resource |
|---|---|---|
| Characters replaced with `_` | everything outside `A-Z a-z 0-9 $` | everything outside `A-Z a-z 0-9` |
| Case | uppercased | left as-is |
| Hash suffix | from the path after `replaceInAssetsPath` | from the original path |
| Name prefix | `constNamePrefix`, uppercased with the rest | `stringNamePrefix`, case preserved |

So `configs/settings.json` becomes `ASSET_CONFIGS_SETTINGS_JSON_2053859403` in Kotlin and
`asset_configs_settings_json_2053859403` in XML.

The hash suffix keeps names unique when two different paths sanitize to the same identifier. Both
generators drop duplicates, which matters when several asset source directories of one variant hold
the same relative path.

## Configuration Reference

`kotlinFile`

- `enabled`: turns Kotlin generation on or off
- `className`: generated object name. Default `AssetFiles`
- `packageName`: generated package name. Default `com.github.utilx`
- `constNamePrefix`: prefix added before the sanitized path. Default `asset_`. The generator uppercases the final constant name.
- `constValuePrefix`: prefix added to each generated value. Default `""`
- `replaceInAssetsPath`: regex replacements applied before the plugin builds the generated name and value

`xmlFile`

- `enabled`: turns XML generation on or off
- `stringNamePrefix`: prefix added to each generated string resource name
- `stringNameCharMapping`: regex replacements applied to the asset path before it becomes a string
  resource name. Same entry shape as `replaceInAssetsPath`. Affects only the generated name, never
  the string value. A resource name cannot start with a digit, so the plugin prefixes `_` if your
  mapping or the asset name would produce one

Deprecated

- `sourceSets`: no longer read. The plugin uses the asset directories each variant declares. Remove
  it from your build file; it has no effect

## Requirements

- Java 17 or newer
- Gradle 8.0 or newer
- Android Gradle Plugin 8.0.0 or newer
- `namespace` set in your Android block when you use AGP 8+

## Notes

- Apply the plugin in the Android module that owns the `assets/` directory.
- Each variant gets its own generated output directory.
- Both generators remove duplicate entries.
- Examples in this README use Kotlin DSL.

## License

Apache 2.0. See [LICENSE](LICENSE).

[Gradle Plugin Portal]: <https://plugins.gradle.org/plugin/com.github.utilx.android-assets-journalist>
