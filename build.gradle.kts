// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "1.9.23" apply false
    id("com.android.library") version "8.9.1" apply false

    id("com.autonomousapps.dependency-analysis") version "2.12.0"
    id("org.jetbrains.kotlin.jvm") version "2.1.0" apply false
    // To check dependencies, run: ./gradlew buildHealth
}

dependencyAnalysis {
    structure {
        ignoreKtx(true) // default is false
    }
}