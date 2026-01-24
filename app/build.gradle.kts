/*
 * Copyright (C) 2025-2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.parcelize")
    // id("androidx.navigation.safeargs.kotlin")

    id("com.autonomousapps.dependency-analysis")
}

apply("../gradle/jacoco.gradle")

object Versions {
    const val COROUTINES = "1.10.2"
    const val JUNIT = "6.0.2"
    const val ESPRESSO = "3.7.0"
    const val FRAGMENT = "1.8.9"
    const val LIFECYCLE = "2.10.0"
    const val ROOM = "2.8.4"
    const val KOIN = "4.1.1"
    const val NAVIGATION = "2.9.6"
}

// Keep this list aligned with the values in the language_codes array in arrays.xml and with locale_config.xml
val supportedLocales =
    listOf("en", "af", "bg", "de", "es", "fr", "it", "pl", "ru", "sv", "tr", "uk")

android {
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Sets dependency metadata when building Android App Bundles.
        includeInBundle = true
    }

    signingConfigs {
        // Create a variable called keystorePropertiesFile, and initialize it to your
        // keystore.properties file, in the rootProject folder.
        val keystorePropertiesFile = rootProject.file("keystore.properties")

        // Initialize a new Properties() object called keystoreProperties.
        val keystoreProperties = Properties()

        // Load your keystore.properties file into the keystoreProperties object.
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))

        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
    namespace = "com.aisleron"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aisleron"
        minSdk = 24
        targetSdk = 36
        versionCode = 18
        versionName = "2026.1.0"
        base.archivesName = "$applicationId-$versionName"

        testInstrumentationRunner = "com.aisleron.di.KoinInstrumentationTestRunner"

        testInstrumentationRunnerArguments["notPackage"] = "com.aisleron.screenshots"
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            isDebuggable = true
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    tasks.withType<Test> {
        useJUnitPlatform() // Make all tests use JUnit 5
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md"
            )
        )
    }

    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    androidResources {
        // This ensures that languages are bundled and not stripped by the Play Store
        @Suppress("UnstableApiUsage")
        localeFilters += supportedLocales
    }

    configurations.all {
        // Targets only the Android Test configurations
        resolutionStrategy {
            // 1. Force 3.0 to break the "strictly 2.2" consistent resolution constraint
            force("org.hamcrest:hamcrest:3.0")

            // 2. Redirect requests for old module names to the new 3.0 single-jar module
            dependencySubstitution {
                substitute(module("org.hamcrest:hamcrest-core"))
                    .using(module("org.hamcrest:hamcrest:3.0"))
                substitute(module("org.hamcrest:hamcrest-library"))
                    .using(module("org.hamcrest:hamcrest:3.0"))
            }
        }
    }
}

dependencies {
    // Implementation
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-ktx:1.12.2")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.collection:collection-ktx:1.5.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.3.0")
    implementation("androidx.customview:customview:1.2.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.documentfile:documentfile:1.1.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:2.3.0")
    implementation("androidx.fragment:fragment-ktx:${Versions.FRAGMENT}")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-common:${Versions.LIFECYCLE}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.LIFECYCLE}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.LIFECYCLE}")

    // Navigation
    implementation("androidx.navigation:navigation-ui-ktx:${Versions.NAVIGATION}")
    implementation("androidx.navigation:navigation-common-ktx:${Versions.NAVIGATION}")
    implementation("androidx.navigation:navigation-runtime-ktx:${Versions.NAVIGATION}")
    implementation("androidx.navigation:navigation-fragment-ktx:${Versions.NAVIGATION}")

    // Database
    implementation("androidx.sqlite:sqlite-ktx:2.6.2")
    implementation("androidx.room:room-ktx:${Versions.ROOM}")
    implementation("androidx.room:room-runtime:${Versions.ROOM}")
    implementation("androidx.room:room-common:${Versions.ROOM}")
    ksp("androidx.room:room-compiler:${Versions.ROOM}")

    // Dependency Injection
    implementation("io.insert-koin:koin-core:${Versions.KOIN}")
    implementation("io.insert-koin:koin-android:${Versions.KOIN}")
    implementation("io.insert-koin:koin-core-viewmodel:${Versions.KOIN}")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}")

    // Testing
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(project(":testData"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.JUNIT}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.JUNIT}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.COROUTINES}")

    // Android Testing
    androidTestImplementation(project(":testData"))
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:core-ktx:1.7.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation("tools.fastlane:screengrab:2.1.1")

    debugImplementation("androidx.room:room-testing-android:${Versions.ROOM}")
    debugImplementation("androidx.fragment:fragment-testing:${Versions.FRAGMENT}")
    androidTestImplementation("io.insert-koin:koin-test:${Versions.KOIN}")
    androidTestImplementation("androidx.navigation:navigation-testing:${Versions.NAVIGATION}")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.COROUTINES}")

    debugImplementation("androidx.test.espresso:espresso-contrib:${Versions.ESPRESSO}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Versions.ESPRESSO}")
    androidTestImplementation("androidx.test.espresso:espresso-intents:${Versions.ESPRESSO}")

    androidTestImplementation("org.hamcrest:hamcrest:3.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
