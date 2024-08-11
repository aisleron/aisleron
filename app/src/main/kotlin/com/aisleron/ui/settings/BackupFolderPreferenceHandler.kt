package com.aisleron.ui.settings

import android.net.Uri
import androidx.preference.Preference
import com.aisleron.R

class BackupFolderPreferenceHandler(private val preference: Preference) :
    BackupRestoreDbPreferenceHandler {

    init {
        updateSummary()
    }

    override suspend fun handleOnPreferenceClick(uri: Uri) {
        setValue(uri.toString())
    }

    override fun BackupRestoreDbPreferenceHandler.getPreference() = preference
    override fun getProcessingMessage() = null
    override fun getSuccessMessage(): String {
        return preference.context.getString(R.string.backup_folder_success)
    }
}