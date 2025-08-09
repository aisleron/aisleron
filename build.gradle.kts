// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.12.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.2.0" apply false
    id("com.android.library") version "8.12.0" apply false

    id("com.autonomousapps.dependency-analysis") version "2.19.0"
    id("org.jetbrains.kotlin.jvm") version "2.2.0" apply false
    // To check dependencies, run: ./gradlew buildHealth
}

dependencyAnalysis {
    structure {
        ignoreKtx(true) // default is false
    }
}