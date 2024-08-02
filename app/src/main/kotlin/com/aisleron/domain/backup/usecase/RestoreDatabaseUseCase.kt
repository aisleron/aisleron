package com.aisleron.domain.backup.usecase

import android.net.Uri

interface RestoreDatabaseUseCase {
    suspend operator fun invoke(backupUri: Uri): Boolean
}

class RestoreDatabaseUseCaseImpl(
) : RestoreDatabaseUseCase {
    override suspend operator fun invoke(backupUri: Uri): Boolean {
        return true
    }
}