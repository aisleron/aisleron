package com.aisleron.ui.settings

import android.net.Uri
import androidx.preference.Preference
import com.aisleron.R
import com.aisleron.domain.backup.usecase.BackupDatabaseUseCase
import org.koin.core.component.KoinComponent
import java.text.DateFormat.getDateTimeInstance
import java.util.Date

class BackupDbPreferenceHandler(
    private val preference: Preference?,
    private val backupDatabaseUseCase: BackupDatabaseUseCase
) :
    BackupRestoreDbPreferenceHandler, KoinComponent {

    init {
        updateSummary()
    }

    override fun getSummaryTemplate() =
        preference?.context?.getString(R.string.last_backup) + " %s"

    override fun getDefaultValue() = preference?.context?.getString(R.string.never) ?: ""

    override suspend fun handleOnPreferenceClick(uri: Uri) {
        backupDatabaseUseCase(uri)
        setValue(getDateTimeInstance().format(Date()))
    }

    override fun BackupRestoreDbPreferenceHandler.getPreference() = preference

    override fun getProcessingMessage(): String? {
        return preference?.context?.getString(R.string.db_backup_processing)
    }

    override fun getSuccessMessage(): String? {
        return preference?.context?.getString(R.string.db_backup_success)
    }
}