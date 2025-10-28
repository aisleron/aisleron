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
import com.aisleron.BuildConfig

class WelcomePreferencesImpl : WelcomePreferences {
    override fun isInitialized(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(IS_INITIALIZED, false)

    override fun setInitialised(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit(commit = true) {
            putBoolean(IS_INITIALIZED, true)
        }
    }

    override fun getLastUpdateVersionCode(context: Context): Int =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(LAST_UPDATE_CODE, 11)

    override fun getLastUpdateVersionName(context: Context): String =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getString(LAST_UPDATE_NAME, "2025.8.0") ?: "2025.8.0"

    override fun setLastUpdateValues(context: Context) {
        setLastUpdateValues(context, BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)
    }

    override fun setLastUpdateValues(
        context: Context, lastUpdateVersionCode: Int, lastUpdateVersionName: String
    ) {
        PreferenceManager.getDefaultSharedPreferences(context).edit(commit = true) {
            putInt(LAST_UPDATE_CODE, lastUpdateVersionCode)
            putString(LAST_UPDATE_NAME, lastUpdateVersionName)
        }
    }

    companion object {
        private const val IS_INITIALIZED = "is_initialised"
        private const val LAST_UPDATE_CODE = "last_update_code"
        private const val LAST_UPDATE_NAME = "last_update_name"
    }
}