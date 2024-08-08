package com.aisleron.domain.backup

import android.net.Uri


interface DatabaseMaintenance {
    fun getDatabaseName(): String?
    suspend fun backupDatabase(backupFolderUri: Uri, backupFileName: String)
    suspend fun restoreDatabase(restoreFileUri: Uri)
}