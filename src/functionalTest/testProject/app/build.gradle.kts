plugins {
    id("com.android.application")
    id("com.github.utilx.android-assets-journalist")
}

android {
    namespace = "com.github.utilx.testapp"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.github.utilx.testapp"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += listOf("testDim")
    productFlavors {
        create("foo") {
            dimension = "testDim"
        }
        create("bar") {
            dimension = "testDim"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

androidAssetsJournalist {
    xmlFile {
        enabled = true
        stringNamePrefix = "prefix_"
    }

    kotlinFile {
        enabled = true
        className = "AssetFilesKotlin"
        packageName = "com.github.utilx"
        constNamePrefix = "constprefixkt_"
        constValuePrefix = "kotlinvalpref_"
        replaceInAssetsPath =
            listOf(
                listOf("match" to "^az", "replaceWith" to "replacekt").toMap(),
                listOf("match" to "d[abc]", "replaceWith" to "kt").toMap(),
            )
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
}
