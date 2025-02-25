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

package com.aisleron.domain.backup.usecase

import com.aisleron.domain.backup.DatabaseMaintenance
import com.aisleron.domain.base.AisleronException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface BackupDatabaseUseCase {
    suspend operator fun invoke(backupFolderUri: URI)
}

class BackupDatabaseUseCaseImpl(private val databaseMaintenance: DatabaseMaintenance) :
    BackupDatabaseUseCase {

    override suspend operator fun invoke(backupFolderUri: URI) {
        val dbName = databaseMaintenance.getDatabaseName()
        if (dbName.isNullOrBlank()) throw AisleronException.InvalidDbNameException("Invalid database name")

        val fileName = dbName.substringBeforeLast(".")
        val fileExt = dbName.substringAfterLast(".")
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val backupFileName = "$fileName-backup-$dateStr.$fileExt"

        databaseMaintenance.backupDatabase(backupFolderUri, backupFileName)
    }
}