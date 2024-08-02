package com.aisleron.ui.settings

import android.net.Uri
import androidx.preference.Preference

class BackupFolderPreferenceHandler(private val preference: Preference?) :
    BackupRestoreDbPreferenceHandler {

    init {
        updateSummary()
    }

    override fun handleOnPreferenceClick(backupUri: Uri) {
        setValue(backupUri.toString())
    }

    override fun getPreference() = preference
}