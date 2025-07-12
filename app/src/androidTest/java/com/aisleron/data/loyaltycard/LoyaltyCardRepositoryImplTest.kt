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

package com.aisleron.data.loyaltycard

import com.aisleron.data.DbInitializer
import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.location.LocationDao
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoModule
import com.aisleron.di.inMemoryDatabaseTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
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

class LoyaltyCardRepositoryImplTest : KoinTest {
    private lateinit var repository: LoyaltyCardRepositoryImpl

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    private fun getKoinModules(): List<Module> = listOf(
        daoModule, inMemoryDatabaseTestModule, repositoryModule, useCaseModule
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        DbInitializer(
            get<LocationDao>(), get<AisleDao>(), TestScope(UnconfinedTestDispatcher())
        ).invoke()

        runBlocking {
            get<CreateSampleDataUseCase>().invoke()
        }

        repository = LoyaltyCardRepositoryImpl(
            loyaltyCardDao = get<LoyaltyCardDao>(),
            locationLoyaltyCardDao = get<LocationLoyaltyCardDao>(),
            loyaltyCardMapper = LoyaltyCardMapper()
        )
    }

    private suspend fun addItems(): List<Int> {
        val newItems = listOf(
            LoyaltyCard(
                id = 0,
                name = "Card 1",
                provider = LoyaltyCardProviderType.CATIMA,
                intent = "Dummy Intent"
            ),
            LoyaltyCard(
                id = 0,
                name = "Card 2",
                provider = LoyaltyCardProviderType.CATIMA,
                intent = "Dummy Intent"
            )
        )

        return repository.add(newItems)
    }

    @Test
    fun add_MultipleCardsProvided_CardsAdded() = runTest {
        val countBefore = repository.getAll().count()

        val newIds = addItems()
        val countAfter = repository.getAll().count()
        val newItemOne = repository.get(newIds.first())
        val newItemTwo = repository.get(newIds.last())

        assertEquals(countBefore + 2, countAfter)
        assertEquals(2, newIds.count())
        assertNotNull(newItemOne)
        assertNotNull(newItemTwo)
    }

    @Test
    fun update_MultipleCardsProvided_CardsUpdated() = runTest {
        val newIds = addItems()
        val countBefore = repository.getAll().count()
        val itemOneBefore = repository.get(newIds.first())!!
        val itemTwoBefore = repository.get(newIds.last())!!
        val updateItems = listOf(
            itemOneBefore.copy(name = "${itemOneBefore.name} Updated"),
            itemTwoBefore.copy(name = "${itemTwoBefore.name} Updated")
        )

        repository.update(updateItems)
        val countAfter = repository.getAll().count()
        val itemOneAfter = repository.get(itemOneBefore.id)
        val itemTwoAfter = repository.get(itemTwoBefore.id)

        assertEquals(countBefore, countAfter)
        assertEquals(itemOneBefore.copy(name = "${itemOneBefore.name} Updated"), itemOneAfter)
        assertEquals(itemTwoBefore.copy(name = "${itemTwoBefore.name} Updated"), itemTwoAfter)
    }

    @Test
    fun remove_ValidCardProvided_CardRemoved() = runTest {
        val itemId = addItems().first()
        val itemBefore = repository.get(itemId)!!
        val countBefore = repository.getAll().count()

        repository.remove(itemBefore)
        val countAfter = repository.getAll().count()
        val itemAfter = repository.get(itemId)

        assertEquals(countBefore - 1, countAfter)
        assertNull(itemAfter)
    }

    @Test
    fun remove_InvalidCardProvided_NoCardsRemoved() = runTest {
        addItems()
        val countBefore = repository.getAll().count()
        val item = LoyaltyCard(
            id = 99,
            name = "Card 99",
            provider = LoyaltyCardProviderType.CATIMA,
            intent = "Dummy Intent"
        )

        repository.remove(item)
        val countAfter = repository.getAll().count()

        assertEquals(countBefore, countAfter)
    }
}