package com.aisleron.domain.product.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows

class CopyProductUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var copyProductUseCase: CopyProductUseCase
    private lateinit var existingProduct: Product

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        val productRepository = dm.getRepository<ProductRepository>()
        copyProductUseCase = dm.getUseCase()
        existingProduct = runBlocking { productRepository.getAll().first() }
    }

    @Test
    fun copyProduct_IsDuplicateName_ThrowsException() = runTest {
        val existingName = "Existing Product Name"
        val productRepository = dm.getRepository<ProductRepository>()
        productRepository.add(existingProduct.copy(id = 0, name = existingName))

        assertThrows<AisleronException.DuplicateProductNameException> {
            copyProductUseCase(existingProduct, existingName)
        }
    }

    @Test
    fun copyProduct_IsValidName_ProductCreated() = runTest {
        val newName = "Copied Product Name"

        val newProductId = copyProductUseCase(existingProduct, newName)

        val newProduct = dm.getRepository<ProductRepository>().get(newProductId)
        assertNotNull(newProduct)
        assertEquals(newName, newProduct.name)

        val aisleProductRepository = dm.getRepository<AisleProductRepository>()
        val sourceAisles = aisleProductRepository.getProductAisles(existingProduct.id)
        val newAisles = aisleProductRepository.getProductAisles(newProductId)
        assertTrue(newAisles.any())
        assertEquals(sourceAisles.count(), newAisles.count())
    }

    @Test
    fun copyProduct_ProductHasNote_NoteCopied() = runTest {
        val noteText = "Copied note"
        val noteId = dm.getRepository<NoteRepository>().add(Note(0, noteText))
        val productRepository = dm.getRepository<ProductRepository>()
        productRepository.update(existingProduct.copy(noteId = noteId))
        val notedProduct = productRepository.get(existingProduct.id)!!
        val newName = "Copied Product Name"

        val newProductId = copyProductUseCase(notedProduct, newName)

        val newProduct = productRepository.get(newProductId)!!
        assertNotNull(newProduct.noteId)
        assertNotEquals(notedProduct.noteId, newProduct.noteId)

        val newNote = dm.getRepository<NoteRepository>().get(newProduct.noteId)
        assertEquals(noteText, newNote?.noteText)
        assertNotEquals(noteId, newNote?.id)
    }

}