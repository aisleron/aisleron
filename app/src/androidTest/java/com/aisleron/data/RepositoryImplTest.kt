/*
 * Copyright (C) 2025 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.data

import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.location.LocationDao
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoModule
import com.aisleron.di.inMemoryDatabaseTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.koin.test.get

abstract class RepositoryImplTest : KoinTest {
    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    private fun getKoinModules(): List<Module> = listOf(
        daoModule, inMemoryDatabaseTestModule, repositoryModule, useCaseModule
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun setup() {
        DbInitializer(
            get<LocationDao>(), get<AisleDao>(), TestScope(UnconfinedTestDispatcher())
        ).invoke()

        runBlocking {
            get<CreateSampleDataUseCase>().invoke()
        }
    }

    protected abstract suspend fun addSingleItem(): Int

    protected abstract suspend fun addMultipleItems(): List<Int>

    abstract fun add_SingleItemProvided_AddItem()

    abstract fun add_MultipleItemsProvided_AddItems()

    abstract fun get_ValidIdProvided_ReturnItem()

    abstract fun get_InvalidIdProvided_ReturnNull()

    abstract fun getAll_MultipleItemsExist_ReturnAll()

    abstract fun update_SingleItemProvided_UpdatedItem()

    abstract fun update_MultipleItemsProvided_UpdateItems()

    abstract fun remove_ValidItemProvided_RemoveItem()

    abstract fun remove_InvalidItemProvided_NoItemsRemoved()
}