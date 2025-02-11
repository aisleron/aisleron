package com.aisleron.domain.product.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAllProductsUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var getAllProductsUseCase: GetAllProductsUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        getAllProductsUseCase = GetAllProductsUseCase(testData.getRepository<ProductRepository>())
    }

    @Test
    fun getAllProducts_ProductsReturned_MatchesRepoList() {
        val getProductsList: List<Product>
        val repoProductsList: List<Product>

        runBlocking {
            repoProductsList = testData.getRepository<ProductRepository>().getAll()
            getProductsList = getAllProductsUseCase()
        }

        assertEquals(repoProductsList, getProductsList)
    }
}