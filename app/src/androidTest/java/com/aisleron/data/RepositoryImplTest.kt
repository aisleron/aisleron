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
import com.aisleron.domain.base.AisleronItem
import com.aisleron.domain.base.BaseRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

abstract class RepositoryImplTest<T : AisleronItem> : KoinTest {
    protected lateinit var repository: BaseRepository<T>

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    private fun getKoinModules(): List<Module> = listOf(
        daoModule, inMemoryDatabaseTestModule, repositoryModule, useCaseModule
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    open fun setUp() {
        DbInitializer(
            get<LocationDao>(), get<AisleDao>(), TestScope(UnconfinedTestDispatcher())
        ).invoke()

        runBlocking {
            get<CreateSampleDataUseCase>().invoke()
        }

        repository = initRepository()
    }

    protected abstract fun initRepository(): BaseRepository<T>

    protected abstract suspend fun getSingleNewItem(): T

    protected suspend fun addSingleItem(): Int {
        val item = getSingleNewItem()
        return repository.add(item)
    }

    protected abstract suspend fun getMultipleNewItems(): List<T>

    protected suspend fun addMultipleItems(): List<Int> {
        val items = getMultipleNewItems()
        return repository.add(items)
    }

    protected abstract suspend fun getInvalidItem(): T

    @Test
    open fun add_SingleItemProvided_AddItem() = runTest {
        val countBefore = repository.getAll().count()

        val newId = addSingleItem()

        val newItem = repository.get(newId)
        assertNotNull(newItem)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore + 1, countAfter)
    }

    @Test
    fun add_MultipleItemsProvided_AddItems() = runTest {
        val countBefore = repository.getAll().count()

        val newIds = addMultipleItems()

        assertTrue(newIds.count() > 1)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore + newIds.count(), countAfter)

        val newItemOne = repository.get(newIds.first())
        assertNotNull(newItemOne)

        val newItemTwo = repository.get(newIds.last())
        assertNotNull(newItemTwo)
    }

    @Test
    fun get_ValidIdProvided_ReturnItem() = runTest {
        val itemIds = addMultipleItems()

        val item = repository.get(itemIds.first())

        assertNotNull(item)
    }

    @Test
    fun get_InvalidIdProvided_ReturnNull() = runTest {
        addMultipleItems()

        val item = repository.get(-1)

        assertNull(item)
    }

    @Test
    fun getAll_MultipleItemsExist_ReturnAll() = runTest {
        val countBefore = repository.getAll().count()
        val itemIds = addMultipleItems()

        val items = repository.getAll()

        assertEquals(countBefore + itemIds.count(), items.count())
    }

    protected abstract fun getUpdatedItem(item: T): T

    @Test
    fun update_SingleItemProvided_UpdatedItem() = runTest {
        val itemIds = repository.add(getMultipleNewItems())
        val item = getUpdatedItem(repository.get(itemIds.first())!!)

        repository.update(item)

        val updatedItem = repository.get(item.id)
        assertEquals(item, updatedItem)
    }

    @Test
    fun update_MultipleItemsProvided_UpdateItems() = runTest {
        val itemIds = addMultipleItems()
        val countBefore = repository.getAll().count()
        val itemOneBefore = getUpdatedItem(repository.get(itemIds.first())!!)
        val itemTwoBefore = getUpdatedItem(repository.get(itemIds.last())!!)
        val updateItems = listOf(itemOneBefore, itemTwoBefore)

        repository.update(updateItems)

        val itemOneAfter = repository.get(itemOneBefore.id)
        assertEquals(itemOneBefore, itemOneAfter)

        val itemTwoAfter = repository.get(itemTwoBefore.id)
        assertEquals(itemTwoBefore, itemTwoAfter)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun remove_ValidItemProvided_RemoveItem() = runTest {
        val itemId = addMultipleItems().first()
        val itemBefore = repository.get(itemId)!!
        val countBefore = repository.getAll().count()

        repository.remove(itemBefore)

        val itemAfter = repository.get(itemId)
        assertNull(itemAfter)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore.dec(), countAfter)
    }

    @Test
    fun remove_InvalidItemProvided_NoItemsRemoved() = runTest {
        addMultipleItems()
        val countBefore = repository.getAll().count()
        val item = getInvalidItem()

        repository.remove(item)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore, countAfter)
    }
}