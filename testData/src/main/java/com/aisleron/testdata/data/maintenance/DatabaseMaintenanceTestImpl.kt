package com.aisleron.testdata.data.maintenance

import android.net.Uri
import com.aisleron.domain.backup.DatabaseMaintenance

class DatabaseMaintenanceTestImpl : DatabaseMaintenance {
    override fun getDatabaseName() = "DummyDbName.sqlite"

    override suspend fun backupDatabase(backupFolderUri: Uri, backupFileName: String) {}

    override suspend fun restoreDatabase(restoreFileUri: Uri) {}
}