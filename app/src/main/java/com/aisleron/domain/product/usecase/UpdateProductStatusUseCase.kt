package com.aisleron.domain.product.usecase

import com.aisleron.domain.product.Product

class UpdateProductStatusUseCase(
    private val getProductUseCase: GetProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase
) {
    suspend operator fun invoke(id: Int, inStock: Boolean): Product? {
        val product = getProductUseCase(id)
        if (product != null) {
            product.inStock = inStock
            updateProductUseCase(product)
        }
        return product
    }
}