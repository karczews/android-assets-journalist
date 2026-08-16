# Contributing to Android Assets Journalist

Thank you for your interest in contributing to Android Assets Journalist! This guide will help you get started with the project.

## Table of Contents

- [Project Overview](#project-overview)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [How to Run Tests](#how-to-run-tests)
- [Building the Playground Project](#building-the-playground-project)
- [Making Changes](#making-changes)
- [Submitting Changes](#submitting-changes)

## Project Overview

Android Assets Journalist is a Gradle plugin that automatically generates type-safe constants for Android assets. It hooks into the Android build process and creates Kotlin constants or XML string resources for all files in the asset directories of each build variant.

### How It Works

1. **Plugin Application**: When applied to an Android project, the plugin registers tasks for each build variant
2. **Task Registration**: Depending on the enabled generators, tasks are registered for each variant (e.g., `debug`, `release`):
   - `generateAssetsKotlinFile{Variant}` - Generates Kotlin constants object
   - `generateAssetsXmlFile{Variant}` - Generates Android string resources

   Both generators are disabled by default. When neither is enabled the plugin logs a warning and
   turns Kotlin generation on, so a default configuration registers only the Kotlin task.
3. **Asset Discovery**: Tasks scan every asset source directory the variant contributes
   (`variant.sources.assets`), not just `src/main/assets/`. For a `fooDebug` variant that
   includes `src/main/assets/`, `src/foo/assets/` and `src/debug/assets/`
4. **Code Generation**: Uses [KotlinPoet](https://github.com/square/kotlinpoet) to generate type-safe code
5. **Integration**: Generated sources are automatically added to the variant's source sets

### Key Components

- **`AssetsJournalistPlugin.kt`** - Main plugin entry point, configures the extension and tasks
- **`AssetFileGeneratorConfig.kt`** - Plugin extension DSL for user configuration
- **`GenerateKotlinFileTask.kt`** - Task that generates Kotlin constants
- **`GenerateXmlFileTask.kt`** - Task that generates XML string resources
- **`FileConstantsFactory.kt`** - Transforms asset paths into valid constant names

## Development Setup

### Prerequisites

- **Java**: JDK 17 or higher
- **Android SDK**: API level 28+ (for running tests)
- **Git**: For version control

### Clone the Repository

```bash
git clone https://github.com/karczews/android-assets-journalist.git
cd android-assets-journalist
```

### Environment Variables

Ensure `ANDROID_HOME` is set:

```bash
export ANDROID_HOME=/path/to/your/android/sdk
```

If you have both `ANDROID_HOME` and `ANDROID_SDK_ROOT` set, the build may fail. The project prefers `ANDROID_HOME`.

## Project Structure

```
android-assets-journalist/
├── build.gradle.kts                    # Main build configuration
├── settings.gradle.kts                 # Project settings
├── gradle/
│   └── wrapper/                        # Gradle wrapper files
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       └── com/github/utilx/assetsjournalist/
│   │           ├── AssetsJournalistPlugin.kt          # Main plugin
│   │           ├── AssetFileGeneratorConfig.kt        # Extension DSL
│   │           ├── common/
│   │           │   ├── FileConstantsFactory.kt        # Path transformation
│   │           │   └── StringTransformer.kt            # Regex replacements
│   │           ├── kotlin/
│   │           │   └── GenerateKotlinFileTask.kt      # Kotlin generation
│   │           └── xml/
│   │               └── GenerateXmlFileTask.kt         # XML generation
│   ├── test/                           # Unit tests
│   │   └── kotlin/
│   │       └── com/github/utilx/assetsjournalist/
│   │           ├── PluginTest.kt                       # Plugin tests
│   │           ├── AssetFileGeneratorConfigTest.kt     # Config tests
│   │           ├── common/
│   │           │   ├── FileConstantsFactoryTest.kt     # Factory tests
│   │           │   └── StringTransformerTest.kt        # Transformer tests
│   │           └── kotlin/
│   │               └── GenerateKotlinFileTaskTest.kt   # Kotlin task tests
│   └── functionalTest/                   # Integration tests
│       └── kotlin/
│           └── com/github/utilx/assetsjournalist/
│               └── AssetsJournalistPluginTest.kt       # End-to-end tests
├── src/functionalTest/
│   └── testProject/                    # Test Android project
│       ├── app/
│       │   ├── build.gradle.kts
│       │   └── src/
│       ├── build.gradle.kts
│       └── settings.gradle.kts
├── playground/                         # Local testing project
│   ├── app/
│   │   ├── build.gradle
│   │   └── src/
│   ├── build.gradle
│   └── settings.gradle
├── .github/
│   └── workflows/
│       └── ci.yml                       # GitHub Actions CI
└── README.md
```

## How to Run Tests

### Unit Tests

Run all unit tests:

```bash
./gradlew test
```

Run tests with coverage report:

```bash
./gradlew test jacocoTestReport
```

The coverage report will be generated at `build/reports/jacoco/test/html/index.html`.

### Functional Tests

Functional tests verify the plugin works in a real Android project:

```bash
./gradlew functionalTest
```

**Note**: Functional tests require:
- Android SDK installed
- `ANDROID_HOME` environment variable set
- No conflicting `ANDROID_SDK_ROOT` variable (or set to the same path)

If you encounter "Multiple SDK locations" errors, unset `ANDROID_SDK_ROOT`:

```bash
unset ANDROID_SDK_ROOT
./gradlew functionalTest
```

### All Tests

Run both unit and functional tests:

```bash
./gradlew check
```

This is the same command used in CI.

### Test Reports

- **Unit tests**: `build/reports/tests/test/index.html`
- **Functional tests**: `build/reports/tests/functionalTest/index.html`
- **Coverage**: `build/reports/jacoco/test/html/index.html`

## Building the Playground Project

The `playground/` directory contains a sample Android app used for manual testing.

### Publishing to Maven Local

First, publish the plugin to your local Maven repository under a version you choose:

```bash
./gradlew publishToMavenLocal -Pversion=1.0.0-SNAPSHOT
```

This installs the plugin to `~/.m2/repository/com/github/utilx/android-assets-journalist/`.

### Building the Playground

The playground resolves the plugin from `mavenLocal()`, so it needs the version you just
published. Pass it with `-PpluginVersion`:

```bash
cd playground
./gradlew clean build -PpluginVersion=1.0.0-SNAPSHOT
```

Or use the provided script, which defaults to `1.0.0-SNAPSHOT`:

```bash
./.scripts/build_playground.sh
```

The script also takes an optional second argument to override the AGP version, which is how
the compatibility matrix exercises the playground:

```bash
./.scripts/build_playground.sh 1.0.0-SNAPSHOT 8.13.2
```

### Verify Generated Files

After building, check the generated asset constants:

```bash
# Kotlin constants
ls playground/app/build/generated/assetsjournalist/src/debug/kotlin/com/github/utilx/

# XML resources
ls playground/app/build/generated/assetsjournalist/src/debug/res/values/
```

### Troubleshooting Playground Build

**Issue**: "Could not find com.github.utilx:android-assets-journalist:X.X.X"

**Solution**: The version the playground asks for must match the version you published. Publish
from the root project and pass the same version to the playground:

```bash
./gradlew publishToMavenLocal -Pversion=1.0.0-SNAPSHOT
cd playground && ./gradlew clean build -PpluginVersion=1.0.0-SNAPSHOT
```

Without `-PpluginVersion` the playground falls back to the hardcoded default in
`playground/build.gradle`, which is unlikely to be the version you published.

**Issue**: "Multiple SDK locations"

**Solution**: 
```bash
unset ANDROID_SDK_ROOT
./gradlew clean build
```

**Issue**: JVM target compatibility errors

**Solution**: Ensure you're using Java 17:
```bash
java -version  # Should show 17
```

## Making Changes

### Development Workflow

1. **Create a branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** to the source code

3. **Test your changes**:
   ```bash
   ./gradlew test functionalTest
   ```

4. **Test in playground** (if needed):
   ```bash
   ./gradlew publishToMavenLocal
   cd playground
   ./gradlew clean build
   ```

5. **Check code style** (Detekt is temporarily disabled but will be re-enabled):
   ```bash
   # ./gradlew detekt  # Currently disabled
   ```

### Key Files to Modify

- **Adding new configuration options**: `AssetFileGeneratorConfig.kt`
- **Changing code generation**: `GenerateKotlinFileTask.kt`, `GenerateXmlFileTask.kt`
- **Modifying path transformations**: `FileConstantsFactory.kt`, `StringTransformer.kt`
- **Adding tests**: `PluginTest.kt`, `AssetsJournalistPluginTest.kt`

### Testing Changes

Always ensure:
1. Unit tests pass: `./gradlew test`
2. Functional tests pass: `./gradlew functionalTest`
3. Playground builds: `./gradlew publishToMavenLocal -Pversion=1.0.0-SNAPSHOT && ./.scripts/build_playground.sh`

## Submitting Changes

### Commit Messages

We follow [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification. All commit messages MUST be structured as follows:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

#### Commit Types

The accepted types are configured in [`.commisery.yml`](../.commisery.yml) and enforced in CI by
[commisery-action](https://github.com/tomtom-international/commisery-action).

| Type | Purpose | Bumps version |
|------|---------|---------------|
| `feat` | New feature (correlates to MINOR in SemVer) | yes |
| `fix` | Bug fix (correlates to PATCH in SemVer) | yes |
| `improvement` | An improvement to an existing feature | yes |
| `revert` | Reverts a previous commit | yes |
| `docs` | Documentation only changes | no |
| `test` | Adding or correcting tests | no |
| `build` | Changes to build system or dependencies | no |
| `ci` | Changes to CI configuration | no |
| `refactor` | Code refactoring without behavior change | no |
| `perf` | Performance improvements | no |
| `style` | Code style changes (formatting, missing semicolons, etc.) | no |
| `chore` | Routine maintenance tasks | no |

#### Breaking Changes

Breaking changes MUST be indicated by either:
1. Appending `!` after the type/scope: `feat!: remove deprecated API`
2. Including `BREAKING CHANGE:` footer:
   ```
   feat: update configuration format

   BREAKING CHANGE: configuration file format changed from JSON to YAML
   ```

#### Description Rules

- Use lowercase after the colon
- Use imperative, present tense: "add feature" not "added feature"
- No period at the end
- Keep it concise. CI rejects subject lines longer than 80 characters
  (`max-subject-length` in `.commisery.yml`)

#### Examples

Simple fix:
```
fix: handle null pointer in asset generator

Add null check before accessing asset path to prevent crashes
when assets directory is empty.
```

Feature with scope:
```
feat(kotlin): generate const val for asset paths

Add support for generating Kotlin const val declarations
instead of regular val properties for better performance.
```

Breaking change:
```
feat(config)!: rename extension property from assetsDir to assetDirectory

Rename the configuration property to be more descriptive and
follow Android Gradle Plugin naming conventions.

BREAKING CHANGE: assetsDir property renamed to assetDirectory
```

### Pull Request Process

1. Fork the repository
2. Create your feature branch
3. Make changes and test
4. Commit with conventional commit messages
5. Push to your fork
6. Create a Pull Request

### Before Submitting

Ensure:
- [ ] All tests pass (`./gradlew check`)
- [ ] Playground builds successfully
- [ ] Documentation updated (if needed)
- [ ] Commit messages follow conventional format
- [ ] GPG sign your commits (if you have setup)

## Questions?

- Open an [issue](https://github.com/karczews/android-assets-journalist/issues) for bugs or feature requests
- Check existing issues before creating new ones

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
