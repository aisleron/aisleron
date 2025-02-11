package com.aisleron.di

import org.koin.dsl.module

val appModules = module {
    includes(
        daoModule,
        databaseModule,
        fragmentModule,
        generalModule,
        preferenceModule,
        repositoryModule,
        useCaseModule,
        viewModelModule
    )
}