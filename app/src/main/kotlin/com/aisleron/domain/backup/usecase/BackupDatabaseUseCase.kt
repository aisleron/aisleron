package com.aisleron.domain.backup.usecase

import android.net.Uri
import com.aisleron.domain.backup.DatabaseMaintenance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface BackupDatabaseUseCase {
    suspend operator fun invoke(backupUri: Uri): Boolean
}

class BackupDatabaseUseCaseImpl(private val databaseMaintenance: DatabaseMaintenance) :
    BackupDatabaseUseCase {

    override suspend operator fun invoke(backupUri: Uri): Boolean {
        val dbName = databaseMaintenance.getDatabaseName()
        if (dbName.isNullOrBlank()) return false

        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val backupDbName =
            "${dbName.substringBeforeLast(".")}-backup-$dateStr.${dbName.substringAfterLast(".")}"
        databaseMaintenance.backupDatabase(backupUri, backupDbName)


        return true
    }
}