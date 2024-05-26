package com.aisleron.domain.product.usecase

import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

class GetProductUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(id: Int): Product? {
        return productRepository.get(id)
    }
}