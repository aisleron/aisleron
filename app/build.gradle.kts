/*
 * Copyright (C) 2025 aisleron.com
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
    const val JUNIT = "6.0.1"
    const val ESPRESSO = "3.7.0"
    const val FRAGMENT = "1.8.9"
    const val LIFECYCLE = "2.10.0"
    const val ROOM = "2.8.4"
    const val KOIN = "4.1.1"
    const val NAVIGATION = "2.9.6"
}

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
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aisleron"
        minSdk = 24
        targetSdk = 35
        versionCode = 14
        versionName = "2025.10.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
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

    //testOptions {
    //    animationsDisabled = true
    //}
}

dependencies {
    // Implementation
    implementation("androidx.core:core-ktx:1.16.0") // 1.17.0 requires API 36
    implementation("androidx.activity:activity-ktx:1.10.1") // 1.11.0 requires API 36
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
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:2.2.21")
    implementation("androidx.fragment:fragment-ktx:${Versions.FRAGMENT}")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-common:${Versions.LIFECYCLE}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.LIFECYCLE}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.LIFECYCLE}")

    // Navigation
    implementation("androidx.navigation:navigation-ui-ktx:${Versions.NAVIGATION}")
    implementation("androidx.navigation:navigation-common:${Versions.NAVIGATION}")
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
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.COROUTINES}")

    // Android Testing
    androidTestImplementation(project(":testData"))
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:core-ktx:1.7.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation("tools.fastlane:screengrab:2.1.1")

    debugImplementation("androidx.room:room-testing-android:${Versions.ROOM}")
    debugImplementation("androidx.fragment:fragment-testing:${Versions.FRAGMENT}")
    // implementation("androidx.room:room-testing:${Versions.ROOM}")
    androidTestImplementation("io.insert-koin:koin-test:${Versions.KOIN}")
    androidTestImplementation("androidx.navigation:navigation-testing:${Versions.NAVIGATION}")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.COROUTINES}")

    debugImplementation("androidx.test.espresso:espresso-contrib:${Versions.ESPRESSO}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Versions.ESPRESSO}")
    androidTestImplementation("androidx.test.espresso:espresso-intents:${Versions.ESPRESSO}")
    // Can't upgrade org.hamcrest:hamcrest to 3.0; 2.2 is a dependency of
    // androidx.test.espresso:espresso-intents:3.7.0
    androidTestImplementation("org.hamcrest:hamcrest:2.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
