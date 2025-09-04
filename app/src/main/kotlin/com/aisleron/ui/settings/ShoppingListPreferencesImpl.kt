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

class ShoppingListPreferencesImpl : ShoppingListPreferences {
    override fun isStatusChangeSnackBarHidden(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            PREF_HIDE_STATUS_CHANGE_SNACK_BAR, false
        )

    override fun showEmptyAisles(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            PREF_SHOW_EMPTY_AISLES, false
        )

    override fun setShowEmptyAisles(context: Context, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putBoolean(PREF_SHOW_EMPTY_AISLES, value)
            }
    }

    override fun stockMethod(context: Context): ShoppingListPreferences.StockMethod {
        val method = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_STOCK_METHOD, CHECKBOX)

        return when (method) {
            QUANTITIES -> ShoppingListPreferences.StockMethod.QUANTITIES
            CHECKBOX_QUANTITIES -> ShoppingListPreferences.StockMethod.CHECKBOX_QUANTITIES
            NONE -> ShoppingListPreferences.StockMethod.NONE
            else -> ShoppingListPreferences.StockMethod.CHECKBOX
        }
    }

    companion object {
        const val PREF_HIDE_STATUS_CHANGE_SNACK_BAR = "hide_status_change_snack_bar"
        const val PREF_SHOW_EMPTY_AISLES = "show_empty_aisles"
        const val PREF_STOCK_METHOD = "stock_method"

        const val CHECKBOX = "checkbox"
        const val QUANTITIES = "quantities"
        const val CHECKBOX_QUANTITIES = "checkbox_quantities"
        const val NONE = "none"
    }
}