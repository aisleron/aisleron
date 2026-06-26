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

package com.aisleron.ui.navigation

import android.content.Intent
import android.os.Build

fun Intent.getDestinationExtra(key: String): Destination? {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, Destination::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key) as? Destination
    }
}