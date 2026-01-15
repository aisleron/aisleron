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

package com.aisleron.domain.preferences

enum class ApplicationTheme(override val value: String) : PreferenceEnum {
    SYSTEM_THEME("system_theme"),
    LIGHT_THEME("light_theme"),
    DARK_THEME("dark_theme");

    // ApplicationTheme needs to be aligned with the theme_values array
    companion object : PreferenceEnum.Factory<ApplicationTheme> {
        override val defaultValue = SYSTEM_THEME
    }
}