package com.aisleron.domain.backup

import java.net.URI


interface DatabaseMaintenance {
    fun getDatabaseName(): String?
    suspend fun backupDatabase(backupFolderUri: URI, backupFileName: String)
    suspend fun restoreDatabase(restoreFileUri: URI)
}