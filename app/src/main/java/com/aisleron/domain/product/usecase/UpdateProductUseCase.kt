package com.aisleron.domain.product.usecase

import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

class UpdateProductUseCase(
    private val productRepository: ProductRepository,
    private val checkProductNameIsUniqueUseCase: CheckProductNameIsUniqueUseCase
) {
    suspend operator fun invoke(product: Product) {

        if (!checkProductNameIsUniqueUseCase(product)) {
            throw AisleronException.DuplicateProductNameException("Product Name must be unique")
        }

        productRepository.update(product)
    }
}
