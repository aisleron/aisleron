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

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class ProductPreferencesImpl : ProductPreferences {
    override fun showExtraOptions(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            SHOW_PRODUCT_EXTRA_OPTIONS, false
        )

    override fun setShowExtraOptions(context: Context, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(SHOW_PRODUCT_EXTRA_OPTIONS, value)
        }
    }

    companion object {
        private const val SHOW_PRODUCT_EXTRA_OPTIONS = "show_product_extra_options"
    }
}