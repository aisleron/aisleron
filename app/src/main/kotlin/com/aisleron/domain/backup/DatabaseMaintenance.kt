package com.aisleron.domain.backup


interface DatabaseMaintenance {
    fun getDatabaseName(): String
    fun backupDatabase(backupUri: String, backupFileName: String)
    fun restoreDatabase(backupUri: String)
}