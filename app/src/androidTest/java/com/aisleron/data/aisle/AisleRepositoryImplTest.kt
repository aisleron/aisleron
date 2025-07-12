package com.aisleron.data.aisle

import com.aisleron.data.DbInitializer
import com.aisleron.data.location.LocationDao
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoModule
import com.aisleron.di.inMemoryDatabaseTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
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

class AisleRepositoryImplTest : KoinTest {
    private lateinit var repository: AisleRepositoryImpl

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

        repository = AisleRepositoryImpl(
            aisleDao = get<AisleDao>(),
            aisleMapper = AisleMapper()
        )
    }

    private suspend fun addItems(locationId: Int): List<Int> {
        val newItems = listOf(
            Aisle(
                id = 0,
                name = "Aisle 100",
                products = emptyList(),
                locationId = locationId,
                rank = 100,
                isDefault = false,
                expanded = true
            ),
            Aisle(
                id = 0,
                name = "Aisle 101",
                products = emptyList(),
                locationId = locationId,
                rank = 101,
                isDefault = false,
                expanded = true
            )
        )

        return repository.add(newItems)
    }

    @Test
    fun add_MultipleAislesProvided_AislesAdded() = runTest {
        val location = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        val countBefore = repository.getAll().count()
        val newIds = addItems(location.id)

        val countAfter = repository.getAll().count()
        val newItemOne = repository.get(newIds.first())
        val newItemTwo = repository.get(newIds.last())

        assertEquals(countBefore + 2, countAfter)
        assertEquals(2, newIds.count())
        assertNotNull(newItemOne)
        assertNotNull(newItemTwo)
    }

    @Test
    fun update_MultipleAislesProvided_AislesUpdated() = runTest {
        val location = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        val newIds = addItems(location.id)
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
}