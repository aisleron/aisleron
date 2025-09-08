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

class ShoppingListPreferencesTestImpl : ShoppingListPreferences {

    private var _hideStatusChangeSnackBar: Boolean = false
    private var _showEmptyAisles: Boolean = false
    private var _keepScreenOn: Boolean = false
    private var _trackingMode: ShoppingListPreferences.TrackingMode =
        ShoppingListPreferences.TrackingMode.CHECKBOX

    override fun isStatusChangeSnackBarHidden(context: Context): Boolean = _hideStatusChangeSnackBar
    override fun showEmptyAisles(context: Context): Boolean = _showEmptyAisles
    override fun keepScreenOn(context: Context): Boolean = _keepScreenOn
    override fun trackingMode(context: Context): ShoppingListPreferences.TrackingMode =
        _trackingMode

    override fun setShowEmptyAisles(context: Context, value: Boolean) {
        _showEmptyAisles = value
    }

    fun setHideStatusChangeSnackBar(hideSnackBar: Boolean) {
        _hideStatusChangeSnackBar = hideSnackBar
    }

    fun setShowEmptyAisles(showEmptyAisles: Boolean) {
        _showEmptyAisles = showEmptyAisles
    }

    fun setTrackingMode(trackingMode: ShoppingListPreferences.TrackingMode) {
        _trackingMode = trackingMode
    }
}