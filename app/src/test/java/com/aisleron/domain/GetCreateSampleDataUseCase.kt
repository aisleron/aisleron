package com.aisleron.domain

import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
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
            IsProductNameUniqueUseCase(productRepository)
        )

        val addAisleUseCase = AddAisleUseCaseImpl(
            aisleRepository, GetLocationUseCase(locationRepository)
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