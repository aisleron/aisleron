package com.aisleron.domain.product.usecase

import com.aisleron.data.aisleproduct.AisleProductDaoTestImpl
import com.aisleron.data.product.ProductDaoTestImpl
import com.aisleron.data.product.ProductMapper
import com.aisleron.data.product.ProductRepositoryImpl
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateProductUseCaseTest {

    private lateinit var updateProductUseCase: UpdateProductUseCase
    private lateinit var productRepository: ProductRepositoryImpl
    private lateinit var existingProduct: Product

    @BeforeEach
    fun setUp() {
        productRepository = ProductRepositoryImpl(
            ProductDaoTestImpl(), AisleProductDaoTestImpl(), ProductMapper()
        )

        runBlocking {
            val id = productRepository.add(
                Product(
                    id = 1,
                    name = "Product 1",
                    inStock = false,
                )
            )
            existingProduct = productRepository.get(id)!!
        }

        updateProductUseCase = UpdateProductUseCase(
            productRepository,
            IsProductNameUniqueUseCase(productRepository)
        )
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun updateProduct_IsDuplicateName_ThrowsException() {
        runBlocking {
            val id = productRepository.add(
                Product(
                    id = 2,
                    name = "Product 2",
                    inStock = false
                )
            )

            val updateProduct = productRepository.get(id)!!.copy(name = existingProduct.name)
            assertThrows<AisleronException.DuplicateProductNameException> {
                updateProductUseCase(updateProduct)
            }
        }
    }

    @Test
    fun updateProduct_ProductExists_RecordUpdated() {
        val updateProduct =
            existingProduct.copy(
                name = existingProduct.name + " Updated",
                inStock = !existingProduct.inStock
            )
        val updatedProduct: Product?
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            countBefore = productRepository.getAll().count()
            updateProductUseCase(updateProduct)
            updatedProduct = productRepository.getByName(updateProduct.name)
            countAfter = productRepository.getAll().count()
        }
        assertNotNull(updatedProduct)
        assertEquals(countBefore, countAfter)
        assertEquals(updateProduct.id, updatedProduct?.id)
        assertEquals(updateProduct.name, updatedProduct?.name)
        assertEquals(updateProduct.inStock, updatedProduct?.inStock)
    }

    @Test
    fun updateProduct_ProductDoesNotExist_RecordCreated() {
        val newProduct = existingProduct.copy(
            id = existingProduct.id + 1,
            name = existingProduct.name + " Inserted"
        )
        val updatedProduct: Product?
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            countBefore = productRepository.getAll().count()
            updateProductUseCase(newProduct)
            updatedProduct = productRepository.getByName(newProduct.name)
            countAfter = productRepository.getAll().count()
        }
        assertNotNull(updatedProduct)
        assertEquals(countBefore + 1, countAfter)
        assertEquals(newProduct.name, updatedProduct?.name)
        assertEquals(newProduct.inStock, updatedProduct?.inStock)
    }
}