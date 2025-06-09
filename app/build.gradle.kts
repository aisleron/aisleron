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

import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.parcelize")

    id("com.autonomousapps.dependency-analysis")
}

apply("../gradle/jacoco.gradle")



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
        targetSdk = 34
        versionCode = 6
        versionName = "2025.4.5"
        base.archivesName = "$applicationId-$versionName"

        testInstrumentationRunner = "com.aisleron.di.KoinInstrumentationTestRunner"
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
            resValue("string", "app_name", "Aisleron Debug")
            resValue("string", "nav_header_title", "Aisleron Debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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

    //testOptions {
    //    animationsDisabled = true
    //}
}

dependencies {

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.fragment:fragment-ktx:1.8.8")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.collection:collection-ktx:1.5.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.3.0")
    implementation("androidx.customview:customview:1.2.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.documentfile:documentfile:1.1.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.0")
    implementation("androidx.navigation:navigation-common:2.9.0")
    implementation("androidx.navigation:navigation-runtime-ktx:2.9.0")
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:2.1.21")

    //Database
    implementation("androidx.sqlite:sqlite-ktx:2.5.1")
    implementation("androidx.room:room-ktx:2.7.1")
    implementation("androidx.room:room-runtime:2.7.1")
    implementation("androidx.room:room-common:2.7.1")
    ksp("androidx.room:room-compiler:2.7.1")

    //Dependency Injection
    implementation("io.insert-koin:koin-android:4.0.4")
    implementation("io.insert-koin:koin-core-viewmodel:4.0.4")
    implementation("io.insert-koin:koin-core:4.0.4")

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    //Testing
    implementation("androidx.lifecycle:lifecycle-runtime-testing:2.9.1")
    implementation("androidx.test.espresso:espresso-contrib:3.6.1")

    testImplementation(project(":testData"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    androidTestImplementation(project(":testData"))
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("io.insert-koin:koin-test:4.0.4")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")
    androidTestImplementation("androidx.navigation:navigation-testing:2.9.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    //org.hamcrest:hamcrest:2.2 is a androidx.test.espresso:espresso-core:3.6.1 dependency
    androidTestImplementation("org.hamcrest:hamcrest:2.2")

    debugImplementation("androidx.fragment:fragment-testing:1.8.8")
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

