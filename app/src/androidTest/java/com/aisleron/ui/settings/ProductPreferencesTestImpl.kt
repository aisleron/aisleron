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

class ProductPreferencesTestImpl : ProductPreferences {
    private var _showExtraOptions = false
    private var _lastSelectedTab = 0

    override fun showExtraOptions(context: Context): Boolean = _showExtraOptions

    override fun setShowExtraOptions(context: Context, value: Boolean) {
        _showExtraOptions = value
    }

    override fun getLastSelectedTab(context: Context): Int = _lastSelectedTab

    override fun setLastSelectedTab(context: Context, position: Int) {
        _lastSelectedTab = position
    }
}