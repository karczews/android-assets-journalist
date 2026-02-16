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

Android Assets Journalist is a Gradle plugin that automatically generates type-safe constants for Android assets. It hooks into the Android build process and creates Java/Kotlin constants or XML string resources for all files in your `src/main/assets/` directory.

### How It Works

1. **Plugin Application**: When applied to an Android project, the plugin registers tasks for each build variant
2. **Task Registration**: For each variant (e.g., `debug`, `release`), three tasks are created:
   - `generateAssetsJavaFile{Variant}` - Generates Java constants class
   - `generateAssetsKotlinFile{Variant}` - Generates Kotlin constants object  
   - `generateAssetsXmlFile{Variant}` - Generates Android string resources
3. **Asset Discovery**: Tasks scan the `src/main/assets/` directory at build time
4. **Code Generation**: Uses [JavaPoet](https://github.com/square/javapoet) and [KotlinPoet](https://github.com/square/kotlinpoet) to generate type-safe code
5. **Integration**: Generated sources are automatically added to the variant's source sets

### Key Components

- **`AssetsJournalistPlugin.kt`** - Main plugin entry point, configures the extension and tasks
- **`AssetFileGeneratorConfig.kt`** - Plugin extension DSL for user configuration
- **`GenerateJavaFileTask.kt`** - Task that generates Java constants
- **`GenerateKotlinFileTask.kt`** - Task that generates Kotlin constants
- **`GenerateXmlFileTask.kt`** - Task that generates XML string resources
- **`FileConstantsFactory.kt`** - Transforms asset paths into valid constant names

## Development Setup

### Prerequisites

- **Java**: JDK 17 or higher (required for AGP 8.x)
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
│   │           ├── java/
│   │           │   └── GenerateJavaFileTask.kt        # Java generation
│   │           ├── kotlin/
│   │           │   └── GenerateKotlinFileTask.kt      # Kotlin generation
│   │           └── xml/
│   │               └── GenerateXmlFileTask.kt         # XML generation
│   ├── test/                           # Unit tests
│   │   └── kotlin/
│   │       └── com/github/utilx/assetsjournalist/
│   │           ├── PluginTest.kt                       # Plugin tests
│   │           ├── AssetFileGeneratorConfigTest.kt     # Config tests
│   │           ├── FileConstantsFactoryTest.kt         # Factory tests
│   │           └── StringTransformerTest.kt           # Transformer tests
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

First, publish the plugin to your local Maven repository:

```bash
./gradlew publishToMavenLocal
```

This installs the plugin to `~/.m2/repository/com/github/utilx/android-assets-journalist/`.

### Building the Playground

```bash
cd playground
./gradlew clean build
```

Or use the provided script:

```bash
./.scripts/build_playground.sh
```

### Verify Generated Files

After building, check the generated asset constants:

```bash
# Java constants
ls playground/app/build/generated/assetsjournalist/src/debug/java/com/github/utilx/

# Kotlin constants  
ls playground/app/build/generated/assetsjournalist/src/debug/kotlin/com/github/utilx/

# XML resources
ls playground/app/build/generated/assetsjournalist/src/debug/res/values/
```

### Troubleshooting Playground Build

**Issue**: "Could not find com.github.utilx:android-assets-journalist:X.X.X"

**Solution**: Run `./gradlew publishToMavenLocal` from the root project first.

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
- **Changing code generation**: `GenerateJavaFileTask.kt`, `GenerateKotlinFileTask.kt`
- **Modifying path transformations**: `FileConstantsFactory.kt`, `StringTransformer.kt`
- **Adding tests**: `PluginTest.kt`, `AssetsJournalistPluginTest.kt`

### Testing Changes

Always ensure:
1. Unit tests pass: `./gradlew test`
2. Functional tests pass: `./gradlew functionalTest`
3. Playground builds: `./gradlew publishToMavenLocal && ./scripts/build_playground.sh`

## Submitting Changes

### Commit Messages

We follow [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `test:` - Test changes
- `build:` - Build system changes
- `ci:` - CI/CD changes
- `refactor:` - Code refactoring

Example:
```
feat: Add support for custom file extensions

Add configuration option to filter assets by extension pattern.
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
