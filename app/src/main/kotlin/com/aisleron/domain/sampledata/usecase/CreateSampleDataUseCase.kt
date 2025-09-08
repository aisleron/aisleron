/*
 * Copyright (C) 2025 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.domain.sampledata.usecase

import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.GetHomeLocationUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import kotlinx.coroutines.flow.first

interface CreateSampleDataUseCase {
    suspend operator fun invoke()
}

class CreateSampleDataUseCaseImpl(
    private val addProductUseCase: AddProductUseCase,
    private val addAisleUseCase: AddAisleUseCase,
    private val getShoppingListUseCase: GetShoppingListUseCase,
    private val updateAisleProductRankUseCase: UpdateAisleProductRankUseCase,
    private val addLocationUseCase: AddLocationUseCase,
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val getHomeLocationUseCase: GetHomeLocationUseCase
) : CreateSampleDataUseCase {

    companion object {
        private const val PRD_FROZEN_VEGES = "Frozen Vegetables"
        private const val PRD_APPLES = "Apples"
        private const val PRD_MILK = "Milk"
        private const val PRD_BUTTER = "Butter"
        private const val PRD_CEREAL = "Cereal"
        private const val PRD_BREAD = "Bread"
        private const val PRD_SOAP = "Soap"
        private const val PRD_TOOTHPASTE = "Toothpaste"
        private const val PRD_PET_FOOD = "Pet Food"
        private const val PRD_SALT = "Salt"

        private const val HOME_AISLE_FREEZER = "Freezer"
        private const val HOME_AISLE_FRIDGE = "Fridge"
        private const val HOME_AISLE_PANTRY = "Pantry"
        private const val HOME_AISLE_BATHROOM = "Bathroom"
        private const val HOME_AISLE_SPICES = "Spices"

        private const val SHOP_NAME = "Save Big Supermarket"

        private const val SHOP_AISLE_FRUIT_VEG = "Fruit and Vegetables"
        private const val SHOP_AISLE_1 = "Aisle 1"
        private const val SHOP_AISLE_2 = "Aisle 2"
        private const val SHOP_AISLE_3 = "Aisle 3 - Personal Care"
        private const val SHOP_AISLE_4 = "Aisle 4"
        private const val SHOP_AISLE_FROZEN_FOODS = "Frozen Foods"
    }

    override suspend operator fun invoke() {
        val products = getAllProductsUseCase()
        if (products.isNotEmpty()) {
            throw AisleronException.SampleDataCreationException("Cannot load sample data into an existing database")
        }

        addSampleProducts()
        addHomeAisles()
        addShop()
    }

    private suspend fun addSampleProducts() {
        val productList = listOf(
            Product(0, PRD_FROZEN_VEGES, true, 0),
            Product(0, PRD_APPLES, true, 0),
            Product(0, PRD_MILK, false, 0),
            Product(0, PRD_BUTTER, false, 0),
            Product(0, PRD_CEREAL, true, 0),
            Product(0, PRD_BREAD, true, 0),
            Product(0, PRD_SOAP, true, 0),
            Product(0, PRD_TOOTHPASTE, false, 0),
            Product(0, PRD_PET_FOOD, true, 0),
            Product(0, PRD_SALT, true, 0)
        )

        productList.forEach { addProductUseCase(it, null) }
    }

    private suspend fun addHomeAisles() {
        val homeLocation = getHomeLocationUseCase()

        val aisleList = listOf(
            Aisle(
                HOME_AISLE_FREEZER, emptyList(), homeLocation.id, 100, 0,
                isDefault = false,
                expanded = true
            ),
            Aisle(
                HOME_AISLE_FRIDGE, emptyList(), homeLocation.id, 200, 0,
                isDefault = false,
                expanded = true
            ),
            Aisle(
                HOME_AISLE_PANTRY, emptyList(), homeLocation.id, 300, 0,
                isDefault = false,
                expanded = true
            ),
            Aisle(
                HOME_AISLE_BATHROOM, emptyList(), homeLocation.id, 400, 0,
                isDefault = false,
                expanded = true
            ),
            Aisle(
                HOME_AISLE_SPICES, emptyList(), homeLocation.id, 500, 0,
                isDefault = false,
                expanded = true
            ),
        )

        aisleList.forEach { addAisleUseCase(it) }

        val homeList = getShoppingListUseCase(homeLocation.id).first()!!

        homeList.aisles.first { it.isDefault }.products.forEach {
            when (it.product.name) {
                PRD_FROZEN_VEGES -> moveProduct(homeList, it, 100, HOME_AISLE_FREEZER)
                PRD_APPLES -> moveProduct(homeList, it, 200, HOME_AISLE_FRIDGE)
                PRD_MILK -> moveProduct(homeList, it, 300, HOME_AISLE_FRIDGE)
                PRD_BUTTER -> moveProduct(homeList, it, 400, HOME_AISLE_FRIDGE)
                PRD_CEREAL -> moveProduct(homeList, it, 500, HOME_AISLE_PANTRY)
                PRD_BREAD -> moveProduct(homeList, it, 600, HOME_AISLE_PANTRY)
                PRD_SOAP -> moveProduct(homeList, it, 700, HOME_AISLE_BATHROOM)
                PRD_TOOTHPASTE -> moveProduct(homeList, it, 800, HOME_AISLE_BATHROOM)
                PRD_SALT -> moveProduct(homeList, it, 900, HOME_AISLE_SPICES)
            }
        }
    }

    private suspend fun addShop() {
        val location = Location(
            id = 0,
            type = LocationType.SHOP,
            defaultFilter = FilterType.NEEDED,
            name = SHOP_NAME,
            pinned = true,
            aisles = emptyList(),
            showDefaultAisle = true
        )

        val shopId = addLocationUseCase(location)

        val aisleList = listOf(
            Aisle(
                SHOP_AISLE_FRUIT_VEG, emptyList(), shopId, 100, 0,
                isDefault = false,
                expanded = true
            ),
            Aisle(SHOP_AISLE_1, emptyList(), shopId, 200, 0, isDefault = false, expanded = true),
            Aisle(SHOP_AISLE_2, emptyList(), shopId, 300, 0, isDefault = false, expanded = true),
            Aisle(SHOP_AISLE_3, emptyList(), shopId, 400, 0, isDefault = false, expanded = true),
            Aisle(SHOP_AISLE_4, emptyList(), shopId, 500, 0, isDefault = false, expanded = true),
            Aisle(
                SHOP_AISLE_FROZEN_FOODS, emptyList(), shopId, 600, 0,
                isDefault = false,
                expanded = true
            ),
        )

        aisleList.forEach { addAisleUseCase(it) }

        val shopList = getShoppingListUseCase(shopId).first()!!

        shopList.aisles.first { it.isDefault }.products.forEach {
            when (it.product.name) {
                PRD_FROZEN_VEGES -> moveProduct(shopList, it, 100, SHOP_AISLE_FROZEN_FOODS)
                PRD_APPLES -> moveProduct(shopList, it, 200, SHOP_AISLE_FRUIT_VEG)
                PRD_MILK -> moveProduct(shopList, it, 300, SHOP_AISLE_FROZEN_FOODS)
                PRD_BUTTER -> moveProduct(shopList, it, 400, SHOP_AISLE_FROZEN_FOODS)
                PRD_CEREAL -> moveProduct(shopList, it, 500, SHOP_AISLE_1)
                PRD_BREAD -> moveProduct(shopList, it, 600, SHOP_AISLE_1)
                PRD_SOAP -> moveProduct(shopList, it, 700, SHOP_AISLE_3)
                PRD_TOOTHPASTE -> moveProduct(shopList, it, 800, SHOP_AISLE_3)
                PRD_PET_FOOD -> moveProduct(shopList, it, 900, SHOP_AISLE_4)
            }
        }
    }

    private suspend fun moveProduct(
        shoppingList: Location,
        currentAisleProduct: AisleProduct,
        newRank: Int,
        newAisleName: String
    ) {
        val updatedAisleProduct = currentAisleProduct.copy(
            rank = newRank,
            aisleId = shoppingList.aisles.first { it.name == newAisleName }.id
        )

        updateAisleProductRankUseCase(updatedAisleProduct)
    }
}