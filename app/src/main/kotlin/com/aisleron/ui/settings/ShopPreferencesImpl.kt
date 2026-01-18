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

class ShopPreferencesImpl(context: Context) : ShopPreferences {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)


    override fun showExtraOptions(): Boolean =
        prefs.getBoolean(SHOW_SHOP_EXTRA_OPTIONS, false)

    override fun setShowExtraOptions(value: Boolean) {
        prefs.edit { putBoolean(SHOW_SHOP_EXTRA_OPTIONS, value) }
    }

    companion object {
        private const val SHOW_SHOP_EXTRA_OPTIONS = "show_shop_extra_options"
    }
}