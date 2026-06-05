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
    // TODO: Need to encrypt the access and refresh tokens, maybe using DataStore or AccountManager
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    override fun useDefault(): Boolean =
        prefs.getBoolean(USE_DEFAULT, true)

    override fun getBackendUrl(): String {
        return if (useDefault())
            BuildConfig.SUPABASE_URL
        else
            prefs.getString(CUSTOM_BACKEND_URL, "") ?: ""
    }

    override fun getBackendKey(): String {
        return if (useDefault())
            BuildConfig.SUPABASE_ANON_KEY
        else
            prefs.getString(CUSTOM_BACKEND_KEY, "") ?: ""
    }

    override fun getAccessToken(): String? {
        return prefs.getString(ACCESS_TOKEN, null)
    }

    override fun getRefreshToken(): String? {
        return prefs.getString(REFRESH_TOKEN, null)
    }

    companion object {
        private const val USE_DEFAULT = "use_default"
        private const val CUSTOM_BACKEND_URL = "custom_backend_url"
        private const val CUSTOM_BACKEND_KEY = "custom_backend_key"
        private const val ACCESS_TOKEN = "access_token"
        private const val REFRESH_TOKEN = "refresh_token"
    }
}