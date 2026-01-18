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

package com.aisleron.ui.settings

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class ProductPreferencesImpl(context: Context) : ProductPreferences {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    override fun showExtraOptions(): Boolean =
        prefs.getBoolean(SHOW_PRODUCT_EXTRA_OPTIONS, false)

    override fun setShowExtraOptions(value: Boolean) {
        prefs.edit { putBoolean(SHOW_PRODUCT_EXTRA_OPTIONS, value) }
    }

    override fun getLastSelectedTab(): Int =
        prefs.getInt(PRODUCT_LAST_SELECTED_TAB, 0)

    override fun setLastSelectedTab(position: Int) {
        prefs.edit {
            putInt(PRODUCT_LAST_SELECTED_TAB, position)
        }
    }

    companion object {
        private const val SHOW_PRODUCT_EXTRA_OPTIONS = "show_product_extra_options"
        private const val PRODUCT_LAST_SELECTED_TAB = "product_last_selected_tab"
    }
}