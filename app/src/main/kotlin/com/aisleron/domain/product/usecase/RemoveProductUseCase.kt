package com.aisleron.domain.product.usecase

import com.aisleron.domain.product.ProductRepository

class RemoveProductUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(productId: Int) {
        val product = productRepository.get(productId)
        product?.let {
            productRepository.remove(it)
        }
    }
}