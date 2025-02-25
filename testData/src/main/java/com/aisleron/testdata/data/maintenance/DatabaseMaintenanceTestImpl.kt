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

package com.aisleron.testdata.data.maintenance

import com.aisleron.domain.backup.DatabaseMaintenance
import java.net.URI

class DatabaseMaintenanceTestImpl : DatabaseMaintenance {

    private var _backupFolderUri = URI("")
    private var _backupFileName = String()
    private var _restoreFileUri = URI("")

    val backupFolderUri get() = _backupFolderUri
    val backupFileName get() = _backupFileName
    val restoreFileUri get() = _restoreFileUri

    override fun getDatabaseName() = "DummyDbName.db"

    override suspend fun backupDatabase(backupFolderUri: URI, backupFileName: String) {
        _backupFolderUri = backupFolderUri
        _backupFileName = backupFileName
    }

    override suspend fun restoreDatabase(restoreFileUri: URI) {
        _restoreFileUri = restoreFileUri
    }
}

class DatabaseMaintenanceDbNameTestImpl(private val databaseName: String?) : DatabaseMaintenance {
    override fun getDatabaseName(): String? = databaseName

    override suspend fun backupDatabase(backupFolderUri: URI, backupFileName: String) {}

    override suspend fun restoreDatabase(restoreFileUri: URI) {}
}