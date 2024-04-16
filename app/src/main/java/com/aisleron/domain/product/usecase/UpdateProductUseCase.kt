package com.aisleron.domain.product.usecase

import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

class UpdateProductUseCase(private val productRepository: ProductRepository) {

    suspend operator fun invoke(product: Product) {
        productRepository.update(product)
    }
}
