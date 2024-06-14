package com.aisleron.domain.product.usecase

import com.aisleron.domain.product.Product

interface UpdateProductStatusUseCase {
    suspend operator fun invoke(id: Int, inStock: Boolean): Product?
}

class UpdateProductStatusUseCaseImpl(
    private val getProductUseCase: GetProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase
) : UpdateProductStatusUseCase {
    override suspend operator fun invoke(id: Int, inStock: Boolean): Product? {
        val product = getProductUseCase(id)?.copy(inStock = inStock)

        if (product != null) {
            updateProductUseCase(product)
        }
        return product
    }
}