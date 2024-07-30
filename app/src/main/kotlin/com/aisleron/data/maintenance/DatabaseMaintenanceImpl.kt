package com.aisleron.data.maintenance

import com.aisleron.data.AisleronDatabase
import com.aisleron.domain.backup.DatabaseMaintenance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class DatabaseMaintenanceImpl(
    private val database: AisleronDatabase,
    coroutineScopeProvider: CoroutineScope? = null
) : DatabaseMaintenance {

    private val coroutineScope = coroutineScopeProvider ?: CoroutineScope(Dispatchers.IO)

    override fun getDatabaseName(): String {
        return "Aisleron.db"
    }

    override fun backupDatabase(backupUri: String, backupFileName: String) {

    }

    override fun restoreDatabase(backupUri: String) {

    }
}