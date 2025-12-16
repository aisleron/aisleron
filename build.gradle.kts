// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.2.21" apply false
    id("com.google.devtools.ksp") version "2.2.20-2.0.3" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.2.21" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.kotlin.jvm") version "2.2.21" apply false

    id("com.autonomousapps.dependency-analysis") version "3.5.1"
    // To check dependencies, run: ./gradlew buildHealth
}

dependencyAnalysis {
    structure {
        ignoreKtx(true) // default is false
    }
}