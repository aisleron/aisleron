package com.aisleron.domain.backup.usecase

import com.aisleron.domain.backup.DatabaseMaintenance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface BackupDatabaseUseCase {
    operator fun invoke(backupFolder: String): Boolean
}

class BackupDatabaseUseCaseImpl(private val backupDatabase: DatabaseMaintenance) :
    BackupDatabaseUseCase {

    override operator fun invoke(backupFolder: String): Boolean {
        val dbName = backupDatabase.getDatabaseName()
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val backupDbName =
            "${dbName.substringBeforeLast(".")}-backup-$dateStr.${dbName.substringAfterLast(".")}"

        backupDatabase.backupDatabase(backupFolder, backupDbName)
        return true
    }
}