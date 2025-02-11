package com.aisleron.di

import com.aisleron.data.AisleronDb
import com.aisleron.data.AisleronTestDatabase
import org.koin.dsl.module

val databaseTestModule = module {
    single<AisleronDb> { AisleronTestDatabase() }
}