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

package com.aisleron.domain.product

enum class TrackingMode(val value: String) {
    CHECKBOX("checkbox"),
    QUANTITY("quantity"),
    CHECKBOX_QUANTITY("checkbox_quantity"),
    NONE("none"),
    DEFAULT("default");

    // TrackingMode needs to be aligned with the tracking_method_values array
    companion object {
        fun fromValue(value: String?): TrackingMode {
            return entries.find { it.value == value } ?: DEFAULT
        }
    }
}