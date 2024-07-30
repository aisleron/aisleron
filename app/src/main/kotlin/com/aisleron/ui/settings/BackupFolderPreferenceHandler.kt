package com.aisleron.ui.settings

import androidx.preference.Preference

class BackupFolderPreferenceHandler(private val preference: Preference?) :
    BackupRestoreDbPreferenceHandler {

    init {
        updateSummary()
    }

    override fun handleOnPreferenceClick(backupUri: String) {
        setValue(backupUri)
    }

    override fun getPreference() = preference
}