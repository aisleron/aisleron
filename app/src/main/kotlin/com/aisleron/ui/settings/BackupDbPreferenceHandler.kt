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
import com.aisleron.R
import com.aisleron.domain.backup.usecase.BackupDatabaseUseCase
import org.koin.core.component.KoinComponent
import java.net.URI
import java.text.DateFormat.getDateTimeInstance
import java.util.Date

class BackupDbPreferenceHandler(
    private val preference: Preference,
    private val backupDatabaseUseCase: BackupDatabaseUseCase
) :
    BackupRestoreDbPreferenceHandler, KoinComponent {

    init {
        updateSummary()
    }

    override fun getSummaryTemplate() =
        preference.context.getString(R.string.last_backup) + " %s"

    override fun getDefaultValue() = preference.context.getString(R.string.never)

    override suspend fun handleOnPreferenceClick(uri: Uri) {
        backupDatabaseUseCase(URI(uri.toString()))
        setValue(getDateTimeInstance().format(Date()))
    }

    override fun BackupRestoreDbPreferenceHandler.getPreference() = preference

    override fun getProcessingMessage(): String {
        return preference.context.getString(R.string.db_backup_processing)
    }

    override fun getSuccessMessage(): String {
        return preference.context.getString(R.string.db_backup_success)
    }
}