package com.aisleron.ui.shoppinglist

import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.RemoveProductUseCase

data class ProductShoppingListItemViewModel(
    override val aisleRank: Int,
    override val rank: Int,
    override val id: Int,
    override val name: String,
    override val aisleId: Int,
    override val inStock: Boolean,
    private val aisleProductId: Int,
    private val updateAisleProductRankUseCase: UpdateAisleProductRankUseCase,
    private val removeProductUseCase: RemoveProductUseCase
) : ProductShoppingListItem, ShoppingListItemViewModel {

    override suspend fun remove() {
        removeProductUseCase(id)
    }

    override suspend fun updateRank(precedingItem: ShoppingListItem?) {
        updateAisleProductRankUseCase(
            AisleProduct(
                rank = if (precedingItem?.itemType == itemType) precedingItem.rank + 1 else 1,
                aisleId = precedingItem?.aisleId ?: aisleId,
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