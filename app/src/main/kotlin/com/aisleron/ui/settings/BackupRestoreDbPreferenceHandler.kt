package com.aisleron.ui.settings

import android.net.Uri
import androidx.preference.Preference

interface BackupRestoreDbPreferenceHandler {
    fun getDefaultValue(): String = String()
    fun getSummaryTemplate(): String = "%s"
    fun getPreference(): Preference?
    fun getProcessingMessage(): String?
    fun getSuccessMessage(): String?
    suspend fun handleOnPreferenceClick(uri: Uri)

    fun getValue(): String {
        val preference = getPreference()
        return preference?.sharedPreferences?.getString(preference.key, getDefaultValue())
            ?: getDefaultValue()
    }

    fun setValue(value: String) {
        val preference = getPreference()
        preference?.sharedPreferences?.edit()
            ?.putString(preference.key, value)
            ?.apply()

        updateSummary()
    }

    fun updateSummary() {
        val preference = getPreference()
        preference?.setSummary(String.format(getSummaryTemplate(), getValue()))
    }
}