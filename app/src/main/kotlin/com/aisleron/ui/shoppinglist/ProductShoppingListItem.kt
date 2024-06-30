package com.aisleron.ui.shoppinglist

import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.RemoveProductUseCase

data class ProductShoppingListItem(
    override val aisleRank: Int,
    override val rank: Int,
    override val id: Int,
    override val name: String,
    override val aisleId: Int,
    val inStock: Boolean,
    val aisleProductId: Int,
    private val updateAisleProductRankUseCase: UpdateAisleProductRankUseCase,
    private val removeProductUseCase: RemoveProductUseCase
) : ShoppingListItem, ShoppingListItemViewModel {
    override val itemType: ShoppingListItem.ItemType
        get() = ShoppingListItem.ItemType.PRODUCT

    override suspend fun remove() {
        removeProductUseCase(id)
    }

    override suspend fun updateRank() {
        updateAisleProductRankUseCase(
            AisleProduct(
                rank = rank,
                aisleId = aisleId,
                id = aisleProductId,
                product = Product(
                    id = id,
                    name = name,
                    inStock = inStock
                )
            )
        )
    }
}