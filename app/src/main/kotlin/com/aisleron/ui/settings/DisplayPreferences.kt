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

package com.aisleron.ui.settings

import com.aisleron.ui.bundles.ShoppingListBundle

interface DisplayPreferences {

    enum class ApplicationTheme(val value: String) {
        SYSTEM_THEME("system_theme"),
        LIGHT_THEME("light_theme"),
        DARK_THEME("dark_theme");

        // ApplicationTheme needs to be aligned with the theme_values array
        companion object {
            fun fromValue(value: String?): ApplicationTheme {
                return ApplicationTheme.entries.find { it.value == value } ?: SYSTEM_THEME
            }
        }
    }

    enum class PureBlackStyle(val value: String) {
        DEFAULT("pure_black_default"),
        ECONOMY("pure_black_economy"),
        BUSINESS_CLASS("pure_black_business_class"),
        FIRST_CLASS("pure_black_first_class");

        // PureBlackStyle needs to be aligned with the pure_black_values array
        companion object {
            fun fromValue(value: String?): PureBlackStyle {
                return PureBlackStyle.entries.find { it.value == value } ?: DEFAULT
            }
        }
    }

    fun showOnLockScreen(): Boolean

    fun applicationTheme(): ApplicationTheme

    fun startingList(): ShoppingListBundle

    fun dynamicColor(): Boolean

    fun pureBlackStyle(): PureBlackStyle
}