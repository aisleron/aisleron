package com.aisleron.domain.backup.usecase

import android.net.Uri
import com.aisleron.domain.backup.DatabaseMaintenance
import com.aisleron.domain.base.AisleronException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface BackupDatabaseUseCase {
    suspend operator fun invoke(backupFolderUri: Uri)
}

class BackupDatabaseUseCaseImpl(private val databaseMaintenance: DatabaseMaintenance) :
    BackupDatabaseUseCase {

    override suspend operator fun invoke(backupFolderUri: Uri) {
        val dbName = databaseMaintenance.getDatabaseName()
        if (dbName.isNullOrBlank()) throw AisleronException.InvalidDbNameException("Invalid database name")

        val fileName = dbName.substringBeforeLast(".")
        val fileExt = dbName.substringAfterLast(".")
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val backupFileName = "$fileName-backup-$dateStr.$fileExt"

        databaseMaintenance.backupDatabase(backupFolderUri, backupFileName)
    }
}