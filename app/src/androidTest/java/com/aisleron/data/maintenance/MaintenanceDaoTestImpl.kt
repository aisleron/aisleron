package com.aisleron.data.maintenance

class MaintenanceDaoTestImpl : MaintenanceDao {
    override suspend fun checkpoint(supportSQLiteQuery: androidx.sqlite.db.SupportSQLiteQuery): Int {
        return 0
    }
}