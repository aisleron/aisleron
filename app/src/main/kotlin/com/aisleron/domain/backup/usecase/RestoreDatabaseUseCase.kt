package com.aisleron.domain.backup.usecase

import android.net.Uri
import com.aisleron.domain.backup.DatabaseMaintenance

interface RestoreDatabaseUseCase {
    suspend operator fun invoke(restoreFileUri: Uri): Boolean
}

class RestoreDatabaseUseCaseImpl(private val databaseMaintenance: DatabaseMaintenance) :
    RestoreDatabaseUseCase {

    override suspend operator fun invoke(restoreFileUri: Uri): Boolean {
        databaseMaintenance.restoreDatabase(restoreFileUri)
        return true
    }
}