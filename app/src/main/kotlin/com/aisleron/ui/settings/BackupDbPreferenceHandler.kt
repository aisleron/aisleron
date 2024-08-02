package com.aisleron.ui.settings

import android.net.Uri
import androidx.preference.Preference
import com.aisleron.R
import com.aisleron.domain.backup.usecase.BackupDatabaseUseCase
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.DateFormat.getDateTimeInstance
import java.util.Date

class BackupDbPreferenceHandler(private val preference: Preference?) :
    BackupRestoreDbPreferenceHandler, KoinComponent {

    init {
        updateSummary()
    }

    private val backupDatabaseUseCase: BackupDatabaseUseCase by inject()

    override fun getSummaryTemplate() =
        preference?.context?.getString(R.string.last_backup) + " %s"

    override fun getDefaultValue() = preference?.context?.getString(R.string.never) ?: ""

    override fun handleOnPreferenceClick(backupUri: Uri) {
        runBlocking {
            //TODO: Change this to proper coroutine handling
            backupDatabaseUseCase(backupUri)
            setValue(getDateTimeInstance().format(Date()))
        }
    }

    override fun getPreference() = preference
}