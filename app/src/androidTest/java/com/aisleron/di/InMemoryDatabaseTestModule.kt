package com.aisleron.di

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.data.AisleronDatabase
import org.koin.dsl.module

val inMemoryDatabaseTestModule = module {
    single<AisleronDatabase> {
        Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            AisleronDatabase::class.java
        ).build()
    }
}