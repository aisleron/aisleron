/*
 * Copyright (C) 2026 aisleron.com
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

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false
    id("com.google.devtools.ksp") version "2.2.20-2.0.3" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.3.0" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.kotlin.jvm") version "2.3.0" apply false
    // id("androidx.navigation.safeargs.kotlin") version "2.9.6" apply false

    id("com.autonomousapps.dependency-analysis") version "3.5.1"
    // To check dependencies, run: ./gradlew buildHealth
}

dependencyAnalysis {
    structure {
        ignoreKtx(true) // default is false
    }
}