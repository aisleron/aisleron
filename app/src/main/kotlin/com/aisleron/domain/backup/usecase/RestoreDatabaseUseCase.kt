package com.aisleron.domain.backup.usecase

interface RestoreDatabaseUseCase {
    operator fun invoke(backupFolder: String): Boolean
}

class RestoreDatabaseUseCaseImpl(
) : RestoreDatabaseUseCase {
    override operator fun invoke(backupFolder: String): Boolean {
        return true
    }
}