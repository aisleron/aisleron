package com.aisleron.di

import kotlinx.coroutines.CoroutineScope
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class KoinModulesTest : KoinTest {
    @Test
    fun checkDaoModule() {
        daoModule.verify()
    }

    @Test
    fun checkDatabaseModule() {
        databaseModule.verify()
    }

    @Test
    fun checkFragmentModule() {
        fragmentModule.verify()
    }

    @Test
    fun checkGeneralModule() {
        generalModule.verify()
    }

    @Test
    fun checkPreferenceModule() {
        preferenceModule.verify()
    }

    @Test
    fun checkRepositoryModule() {
        repositoryModule.verify()
    }

    @Test
    fun checkUseCaseModule() {
        useCaseModule.verify()
    }

    @Test
    fun checkViewModelModule() {
        viewModelModule.verify(extraTypes = listOf(CoroutineScope::class))
    }
}