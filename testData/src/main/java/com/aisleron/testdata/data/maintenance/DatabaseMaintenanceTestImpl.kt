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