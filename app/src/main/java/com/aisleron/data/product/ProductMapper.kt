package com.aisleron.data.product

import com.aisleron.data.base.MapperBaseImpl
import com.aisleron.domain.product.Product

class ProductMapper : MapperBaseImpl<ProductEntity, Product>() {
    override fun toModel(value: ProductEntity) = Product(
        id = value.id,
        name = value.name,
        inStock = value.inStock
    )

    override fun fromModel(value: Product) = ProductEntity(
        id = value.id,
        name = value.name,
        inStock = value.inStock
    )
}