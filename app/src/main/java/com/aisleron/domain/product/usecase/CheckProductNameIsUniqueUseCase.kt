package com.aisleron.domain.product.usecase

import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

class CheckProductNameIsUniqueUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(product: Product): Boolean {
        val existingProduct: Product? = productRepository.getByName(product.name.trim())
        //Product name is unique if no existing product was found, or
        // the existing product has the same id
        return existingProduct?.let { it.id == product.id } ?: true

    }
}