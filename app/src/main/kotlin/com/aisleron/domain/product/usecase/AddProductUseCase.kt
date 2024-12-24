package com.aisleron.domain.product.usecase

import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

interface AddProductUseCase {
    suspend operator fun invoke(product: Product): Int
}

class AddProductUseCaseImpl(
    private val productRepository: ProductRepository,
    private val getDefaultAislesUseCase: GetDefaultAislesUseCase,
    private val addAisleProductsUseCase: AddAisleProductsUseCase,
    private val isProductNameUniqueUseCase: IsProductNameUniqueUseCase

) : AddProductUseCase {
    override suspend operator fun invoke(product: Product): Int {

        if (!isProductNameUniqueUseCase(product)) {
            throw AisleronException.DuplicateProductNameException("Product Name must be unique")
        }

        if (productRepository.get(product.id) != null) {
            throw AisleronException.DuplicateProductException("Cannot add a duplicate of an existing Product")
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