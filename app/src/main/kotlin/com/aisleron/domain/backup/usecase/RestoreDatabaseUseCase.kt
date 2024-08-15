package com.aisleron.domain.backup.usecase

import com.aisleron.domain.backup.DatabaseMaintenance
import com.aisleron.domain.base.AisleronException
import java.net.URI

interface RestoreDatabaseUseCase {
    suspend operator fun invoke(restoreFileUri: URI)
}

class RestoreDatabaseUseCaseImpl(private val databaseMaintenance: DatabaseMaintenance) :
    RestoreDatabaseUseCase {

    override suspend operator fun invoke(restoreFileUri: URI) {
        if (restoreFileUri.toString().isBlank())
            throw AisleronException.InvalidDbRestoreFileException("Invalid database backup file/location")

        databaseMaintenance.restoreDatabase(restoreFileUri)
    }
}