package com.aisleron.domain.product.usecase

import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

class GetProductsUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(): List<Product> {
        return productRepository.getAll()
    }
}