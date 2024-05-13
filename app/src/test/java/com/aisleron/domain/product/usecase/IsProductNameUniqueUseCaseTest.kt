package com.aisleron.domain.product.usecase

import com.aisleron.data.aisleproduct.AisleProductDaoTestImpl
import com.aisleron.data.product.ProductDaoTestImpl
import com.aisleron.data.product.ProductMapper
import com.aisleron.data.product.ProductRepositoryImpl
import com.aisleron.domain.product.Product
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IsProductNameUniqueUseCaseTest {

    private lateinit var isProductNameUniqueUseCase: IsProductNameUniqueUseCase

    @BeforeEach
    fun setUp() {
        isProductNameUniqueUseCase = IsProductNameUniqueUseCase(productRepository)
    }

    @AfterEach
    fun tearDown() {

    }

    @Test
    fun isNameUnique_NoMatchingNameExists_ReturnTrue() {
        val newProduct = existingProduct.copy(id = 0, name = "Product 2")
        val result = runBlocking {
            isProductNameUniqueUseCase(newProduct)
        }
        Assertions.assertNotEquals(existingProduct.name, newProduct.name)
        Assertions.assertTrue(result)
    }

    @Test
    fun isNameUnique_LocationIdsMatch_ReturnTrue() {
        val newProduct = existingProduct.copy(inStock = true)
        val result = runBlocking {
            isProductNameUniqueUseCase(newProduct)
        }
        Assertions.assertEquals(existingProduct.id, newProduct.id)
        Assertions.assertTrue(result)
    }

    @Test
    fun isNameUnique_NamesMatchIdsDiffer_ReturnFalse() {
        val newProduct = existingProduct.copy(id = 0)
        val result = runBlocking {
            isProductNameUniqueUseCase(newProduct)
        }
        Assertions.assertEquals(existingProduct.name, newProduct.name)
        Assertions.assertNotEquals(existingProduct.id, newProduct.id)
        Assertions.assertFalse(result)
    }

    companion object {

        private lateinit var productRepository: ProductRepositoryImpl
        private lateinit var existingProduct: Product

        @JvmStatic
        @BeforeAll
        fun beforeSpec() {
            productRepository = ProductRepositoryImpl(
                ProductDaoTestImpl(),
                AisleProductDaoTestImpl(),
                ProductMapper()
            )

            existingProduct = runBlocking {
                val id = productRepository.add(
                    Product(
                        id = 1,
                        name = "Product 1",
                        inStock = false
                    )
                )
                productRepository.get(id)!!
            }
        }
    }
}