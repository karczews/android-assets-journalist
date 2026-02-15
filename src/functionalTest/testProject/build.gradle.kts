// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
   /* repositories {
        mavenLocal()
        google()
        jcenter()
    }*/

    dependencies {

        /*classpath("com.android.tools.build:gradle:3.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.21")
         */
        // uncomment when testing not via functional tests

        // classpath("com.github.utilx:android-assets-journalist:0.11.0-SNAPSHOT")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
//        mavenLocal()
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
