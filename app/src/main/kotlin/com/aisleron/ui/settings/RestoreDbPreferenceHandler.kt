package com.aisleron.ui.settings

import android.net.Uri
import androidx.preference.Preference
import com.aisleron.R
import com.aisleron.domain.backup.usecase.RestoreDatabaseUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.DateFormat.getDateTimeInstance
import java.util.Date

class RestoreDbPreferenceHandler(private val preference: Preference?) :
    BackupRestoreDbPreferenceHandler, KoinComponent {

    init {
        updateSummary()
    }

    private val restoreDatabaseUseCase: RestoreDatabaseUseCase by inject()

    override fun getSummaryTemplate() =
        preference?.context?.getString(R.string.last_restore) + " %s"

    override fun getDefaultValue() = preference?.context?.getString(R.string.never) ?: ""

    override suspend fun handleOnPreferenceClick(uri: Uri) {
        restoreDatabaseUseCase(uri)
        setValue(getDateTimeInstance().format(Date()))
    }

    override fun getPreference() = preference

    override fun getProcessingMessage(): String? {
        return preference?.context?.getString(R.string.db_restore_processing)
    }

    override fun getSuccessMessage(): String? {
        return preference?.context?.getString(R.string.db_restore_success)
    }
}