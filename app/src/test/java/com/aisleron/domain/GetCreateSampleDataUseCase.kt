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

package com.aisleron.domain

import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisle.usecase.IsAisleNameUniqueUseCase
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.aisleproduct.usecase.GetAisleMaxRankUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.usecase.AddLocationUseCaseImpl
import com.aisleron.domain.location.usecase.GetHomeLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.IsLocationNameUniqueUseCase
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.AddProductUseCaseImpl
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import com.aisleron.domain.product.usecase.IsProductNameUniqueUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCaseImpl
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase

class GetCreateSampleDataUseCase {

    operator fun invoke(
        locationRepository: LocationRepository,
        aisleRepository: AisleRepository,
        productRepository: ProductRepository,
        aisleProductRepository: AisleProductRepository
    ): CreateSampleDataUseCase {
        val getShoppingListUseCase = GetShoppingListUseCase(locationRepository)
        val getAllProductsUseCase = GetAllProductsUseCase(productRepository)
        val getHomeLocationUseCase = GetHomeLocationUseCase(locationRepository)
        val addAisleProductsUseCase = AddAisleProductsUseCase(aisleProductRepository)
        val updateAisleProductRankUseCase = UpdateAisleProductRankUseCase(aisleProductRepository)

        val addProductUseCase = AddProductUseCaseImpl(
            productRepository,
            GetDefaultAislesUseCase(aisleRepository),
            addAisleProductsUseCase,
            IsProductNameUniqueUseCase(productRepository),
            GetAisleMaxRankUseCase(aisleProductRepository)
        )

        val addAisleUseCase = AddAisleUseCaseImpl(
            aisleRepository,
            GetLocationUseCase(locationRepository),
            IsAisleNameUniqueUseCase(aisleRepository)
        )

        val addLocationUseCase = AddLocationUseCaseImpl(
            locationRepository,
            addAisleUseCase,
            getAllProductsUseCase,
            addAisleProductsUseCase,
            IsLocationNameUniqueUseCase(locationRepository)
        )

        return CreateSampleDataUseCaseImpl(
            addProductUseCase = addProductUseCase,
            addAisleUseCase = addAisleUseCase,
            getShoppingListUseCase = getShoppingListUseCase,
            updateAisleProductRankUseCase = updateAisleProductRankUseCase,
            addLocationUseCase = addLocationUseCase,
            getAllProductsUseCase = getAllProductsUseCase,
            getHomeLocationUseCase = getHomeLocationUseCase
        )
    }
}