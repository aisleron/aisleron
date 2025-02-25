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

import android.net.Uri
import androidx.preference.Preference

interface BackupRestoreDbPreferenceHandler {
    fun getDefaultValue(): String = String()
    fun getSummaryTemplate(): String = "%s"
    fun BackupRestoreDbPreferenceHandler.getPreference(): Preference
    fun getProcessingMessage(): String?
    fun getSuccessMessage(): String
    suspend fun handleOnPreferenceClick(uri: Uri)

    fun getValue(): String {
        val preference = getPreference()
        return preference.sharedPreferences?.getString(preference.key, getDefaultValue())
            ?: getDefaultValue()
    }

    fun setValue(value: String) {
        val preference = getPreference()
        preference.sharedPreferences?.edit()
            ?.putString(preference.key, value)
            ?.apply()

        updateSummary()
    }

    fun updateSummary() {
        getPreference().setSummary(String.format(getSummaryTemplate(), getValue()))
    }

    fun setOnPreferenceClickListener(
        onPreferenceClickListener: Preference.OnPreferenceClickListener?
    ) {
        getPreference().onPreferenceClickListener = onPreferenceClickListener
    }
}