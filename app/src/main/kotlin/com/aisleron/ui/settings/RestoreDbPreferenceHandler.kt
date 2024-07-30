package com.aisleron.ui.settings

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

    override fun handleOnPreferenceClick(backupUri: String) {
        restoreDatabaseUseCase(backupUri)
        setValue(getDateTimeInstance().format(Date()))
    }

    override fun getPreference() = preference
}