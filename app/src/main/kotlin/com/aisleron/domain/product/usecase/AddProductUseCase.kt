package com.aisleron.domain.product.usecase

import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

class AddProductUseCase(
    private val productRepository: ProductRepository,
    private val getDefaultAislesUseCase: GetDefaultAislesUseCase,
    private val addAisleProductsUseCase: AddAisleProductsUseCase,
    private val isProductNameUniqueUseCase: IsProductNameUniqueUseCase

) {
    suspend operator fun invoke(product: Product): Int {

        if (!isProductNameUniqueUseCase(product)) {
            throw AisleronException.DuplicateLocationNameException("Product Name must be unique")
        }

        val newProduct = Product(
            id = productRepository.add(product),
            name = product.name,
            inStock = product.inStock
        )

        addAisleProductsUseCase(getDefaultAislesUseCase().map {
            AisleProduct(
                aisleId = it.id,
                product = newProduct,
                rank = 0,
                id = 0
            )
        })

        return newProduct.id
    }
}