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

enum class PureBlackStyle(override val value: String) : PreferenceEnum{
    DEFAULT("pure_black_default"),
    ECONOMY("pure_black_economy"),
    BUSINESS_CLASS("pure_black_business_class"),
    FIRST_CLASS("pure_black_first_class");

    // PureBlackStyle needs to be aligned with the pure_black_values array
    companion object : PreferenceEnum.Factory<PureBlackStyle> {
        override val defaultValue = DEFAULT
    }
}