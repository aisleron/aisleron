import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

apply("../gradle/jacoco.gradle")

// Create a variable called keystorePropertiesFile, and initialize it to your
// keystore.properties file, in the rootProject folder.
val keystorePropertiesFile = rootProject.file("keystore.properties")

// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()

// Load your keystore.properties file into the keystoreProperties object.
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
    signingConfigs {
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
        versionCode = 1
        versionName = "0.2.0"
        base.archivesName = "$applicationId-$versionName"

        testInstrumentationRunner = "com.aisleron.di.KoinInstrumentationTestRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
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

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.6")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.datastore:datastore-preferences:1.1.2")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.databinding:databinding-runtime:8.8.0")
    implementation("androidx.navigation:navigation-testing:2.8.6")
    implementation("androidx.test.espresso:espresso-contrib:3.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    //Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation(project(":testData"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(project(":testData"))
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    androidTestImplementation("io.insert-koin:koin-test:4.0.2")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")
    debugImplementation("androidx.fragment:fragment-testing:1.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-testing:2.8.7")

    //Room
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    //Koin
    implementation("io.insert-koin:koin-android:4.0.2")

    //Coroutines
    implementation("androidx.room:room-ktx:2.6.1")
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

