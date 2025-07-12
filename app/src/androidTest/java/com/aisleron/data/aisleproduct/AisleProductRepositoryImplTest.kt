package com.aisleron.data.aisleproduct

import com.aisleron.data.DbInitializer
import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.location.LocationDao
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoModule
import com.aisleron.di.inMemoryDatabaseTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.product.ProductRepository
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

class AisleProductRepositoryImplTest : KoinTest {
    private lateinit var repository: AisleProductRepositoryImpl

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

        repository = AisleProductRepositoryImpl(
            aisleProductDao = get<AisleProductDao>(),
            aisleProductRankMapper = AisleProductRankMapper()
        )
    }

    @Test
    fun add_SingleAisleProductProvided_AisleProductAdded() = runTest {
        val product = get<ProductRepository>().getAll().first()
        val item = AisleProduct(
            id = 0,
            rank = 999,
            aisleId = 101,
            product = product
        )

        val countBefore = repository.getAll().count()

        val newId = repository.add(item)
        val countAfter = repository.getAll().count()
        val newItem = repository.get(newId)

        assertEquals(countBefore + 1, countAfter)
        assertNotNull(newItem)
    }

    @Test
    fun remove_ValidAisleProductProvided_AisleProductRemoved() = runTest {
        val itemBefore = repository.getAll().first()
        val countBefore = repository.getAll().count()

        repository.remove(itemBefore)
        val countAfter = repository.getAll().count()
        val itemAfter = repository.get(itemBefore.id)

        assertEquals(countBefore - 1, countAfter)
        assertNull(itemAfter)
    }

    @Test
    fun remove_InvalidAisleProductProvided_NoAisleProductRemoved() = runTest {
        val product = get<ProductRepository>().getAll().first()
        val countBefore = repository.getAll().count()
        val item = AisleProduct(
            id = 999,
            rank = 999,
            aisleId = 101,
            product = product
        )

        repository.remove(item)
        val countAfter = repository.getAll().count()

        assertEquals(countBefore, countAfter)
    }
}