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

package com.aisleron.data.preferences

import android.content.Context
import androidx.preference.PreferenceManager
import com.aisleron.BuildConfig
import com.aisleron.domain.preferences.SyncPreferences

class SyncPreferencesImpl(context: Context) : SyncPreferences {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    override fun useDefaultBackEnd(): Boolean =
        prefs.getBoolean(USE_DEFAULT_BACKEND, true)

    override fun getBackendUrl(): String {
        return if (useDefaultBackEnd())
            BuildConfig.SUPABASE_URL
        else
            prefs.getString(CUSTOM_BACKEND_URL, "").orEmpty()
    }

    override fun getBackendKey(): String {
        return if (useDefaultBackEnd())
            BuildConfig.SUPABASE_ANON_KEY
        else
            prefs.getString(CUSTOM_BACKEND_KEY, "").orEmpty()
    }

    companion object {
        private const val USE_DEFAULT_BACKEND = "use_default_backend"
        private const val CUSTOM_BACKEND_URL = "custom_backend_url"
        private const val CUSTOM_BACKEND_KEY = "custom_backend_key"
    }
}