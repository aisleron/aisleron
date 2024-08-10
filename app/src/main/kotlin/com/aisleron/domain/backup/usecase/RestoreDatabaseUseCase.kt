package com.aisleron.domain.backup.usecase

import com.aisleron.domain.backup.DatabaseMaintenance
import java.net.URI

interface RestoreDatabaseUseCase {
    suspend operator fun invoke(restoreFileUri: URI)
}

class RestoreDatabaseUseCaseImpl(private val databaseMaintenance: DatabaseMaintenance) :
    RestoreDatabaseUseCase {

    override suspend operator fun invoke(restoreFileUri: URI) {
        databaseMaintenance.restoreDatabase(restoreFileUri)
    }
}