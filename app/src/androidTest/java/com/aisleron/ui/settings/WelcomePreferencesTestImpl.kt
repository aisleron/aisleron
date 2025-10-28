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
import com.aisleron.BuildConfig

class WelcomePreferencesTestImpl : WelcomePreferences {

    private var _isInitialized: Boolean = false
    private var _lastUpdateVersionCode: Int = 11
    private var _lastUpdateVersionName: String = "2025.8.0"

    override fun isInitialized(context: Context): Boolean = _isInitialized

    override fun setInitialised(context: Context) {
        _isInitialized = true
    }

    override fun getLastUpdateVersionCode(context: Context): Int = _lastUpdateVersionCode

    override fun getLastUpdateVersionName(context: Context): String = _lastUpdateVersionName

    override fun setLastUpdateValues(context: Context) {
        setLastUpdateValues(context, BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)
    }

    override fun setLastUpdateValues(
        context: Context, lastUpdateVersionCode: Int, lastUpdateVersionName: String
    ) {
        _lastUpdateVersionCode = lastUpdateVersionCode
        _lastUpdateVersionName = lastUpdateVersionName
    }
}