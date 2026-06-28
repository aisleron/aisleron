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

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dependency.analysis)
}

apply(file("../gradle/jacoco.gradle.kts"))

// Keep this list aligned with the values in the language_codes array in arrays.xml and with locale_config.xml
val supportedLocales =
    listOf(
        "en",
        "af",
        "ar",
        "bg",
        "br",
        "de",
        "es",
        "fr",
        "it",
        "pl",
        "pt",
        "ru",
        "sv",
        "ta",
        "tr",
        "uk"
    )

android {
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Sets dependency metadata when building Android App Bundles.
        includeInBundle = true
    }

    signingConfigs {
        val keystorePropertiesFile = rootProject.file("keystore.properties")

        if (keystorePropertiesFile.exists()) {
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    namespace = "com.aisleron"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aisleron"
        minSdk = 24
        targetSdk = 36
        versionCode = 24
        versionName = "2026.6.0"
        base.archivesName = "$applicationId-$versionName"

        testInstrumentationRunner = "com.aisleron.di.KoinInstrumentationTestRunner"

        testInstrumentationRunnerArguments["notPackage"] = "com.aisleron.screenshots"
    }

    val backendProperties = Properties()
    val backendPropertiesFile = rootProject.file("backend.properties")

    if (backendPropertiesFile.exists()) {
        FileInputStream(backendPropertiesFile).use { stream ->
            backendProperties.load(stream)
        }
    }

    fun getBackendProperty(key: String, defaultValue: String = ""): String {
        return backendProperties.getProperty(key) ?: defaultValue
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
            signingConfig = signingConfigs.findByName("release")

            buildConfigField(
                "String",
                "SUPABASE_URL",
                getBackendProperty("RELEASE_SUPABASE_URL")
            )

            buildConfigField(
                "String",
                "SUPABASE_ANON_KEY",
                getBackendProperty("RELEASE_SUPABASE_ANON_KEY")
            )
        }

        debug {
            isDebuggable = true
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"

            buildConfigField(
                "String",
                "SUPABASE_URL",
                getBackendProperty("DEBUG_SUPABASE_URL", "http://10.0.2.2:54321")
            )

            buildConfigField(
                "String",
                "SUPABASE_ANON_KEY",
                getBackendProperty("DEBUG_SUPABASE_ANON_KEY", "missing_debug_key")
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md"
            )
        )
    }

    androidResources {
        // This ensures that languages are bundled and not stripped by the Play Store
        @Suppress("UnstableApiUsage")
        localeFilters += supportedLocales
    }

    configurations.all {
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

    // Added to resolve issues with MockK agent (v1.14.11). Remove if issue is resolved.
    testOptions {
        packaging {
            jniLibs {
                useLegacyPackaging = true
            }
        }
    }
}

gradle.taskGraph.whenReady {
    val isReleaseTask = allTasks.any {
        it.name.contains("assembleRelease", ignoreCase = true) ||
                it.name.contains("bundleRelease", ignoreCase = true)
    }

    if (isReleaseTask) {
        val releaseConfig = android.signingConfigs.findByName("release")
        if (releaseConfig == null || releaseConfig.storeFile == null) {
            throw GradleException(
                "CRITICAL ERROR: You are attempting a Release build without a signing key.\n" +
                        "Production binaries must be signed. Check your keystore.properties file."
            )
        }
    }
}

val androidComponents = extensions.getByType<ApplicationAndroidComponentsExtension>()
androidComponents.onVariants { variant ->
    variant.androidTest?.sources?.assets?.addStaticSourceDirectory("$projectDir/schemas")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

tasks.withType<Test> {
    useJUnitPlatform() // Make all tests use JUnit 5
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
dependencies {
    // Implementation
    implementation(libs.activity)
    implementation(libs.activity.compose)
    implementation(libs.annotation)
    implementation(libs.appcompat)
    implementation(libs.cardview)
    implementation(libs.collection)
    implementation(libs.constraintlayout)
    implementation(libs.coordinatorlayout)
    implementation(libs.core.ktx) // 1.19.0 Requires Android 17, and moves to non-ktx
    implementation(libs.customview)
    implementation(libs.documentfile)
    implementation(libs.drawerlayout)
    implementation(libs.jetbrains.kotlinx.serialization.core)
    implementation(libs.kotlin.parcelize.runtime)
    implementation(libs.material)
    implementation(libs.preference.ktx)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)

    // Fragment
    implementation(libs.fragment.ktx) // 1.9.0 moves to non-ktx
    debugImplementation(libs.fragment.testing)

    // Jetpack Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.text)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.ui.unit)
    implementation(libs.material3)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.tooling)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation(libs.compose.ui.test.junit4)

    // Lifecycle
    implementation(libs.lifecycle.common)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.navigation.common)
    implementation(libs.navigation.compose)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.runtime)
    implementation(libs.navigation.ui)
    androidTestImplementation(libs.navigation.testing)

    // Consider moving to navigation3 once it matures
    // implementation("androidx.navigation3:navigation3-runtime:1.1.3")
    // implementation("androidx.navigation3:navigation3-ui:1.1.3")

    // Database
    ksp(libs.room.compiler)
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    implementation(libs.sqlite)
    debugImplementation(libs.room.testing.android)

    // Dependency Injection
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose)
    implementation(libs.koin.core)
    implementation(libs.koin.core.viewmodel)
    androidTestImplementation(platform(libs.koin.bom))
    androidTestImplementation(libs.koin.test)

    // Coroutines
    implementation(platform(libs.kotlinx.coroutines.bom))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(platform(libs.kotlinx.coroutines.bom))
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.kotlinx.coroutines.bom))
    androidTestImplementation(libs.kotlinx.coroutines.test)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.kt)
    implementation(libs.supabase.auth.kt)

    // Ktor
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.core)

    // Testing
    testImplementation(platform(libs.junit.bom))
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(project(":testData"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockk)
    testImplementation(libs.slf4j.nop)

    // Android Testing
    androidTestImplementation(project(":testData"))
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.mockk.agent)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.screengrab)
    androidTestImplementation(libs.test.core.ktx)
    androidTestImplementation(libs.uiautomator)

    debugImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.hamcrest)
}