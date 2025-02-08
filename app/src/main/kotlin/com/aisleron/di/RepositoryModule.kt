package com.aisleron.di

import com.aisleron.data.aisle.AisleMapper
import com.aisleron.data.aisle.AisleRepositoryImpl
import com.aisleron.data.aisleproduct.AisleProductRankMapper
import com.aisleron.data.aisleproduct.AisleProductRepositoryImpl
import com.aisleron.data.location.LocationMapper
import com.aisleron.data.location.LocationRepositoryImpl
import com.aisleron.data.product.ProductMapper
import com.aisleron.data.product.ProductRepositoryImpl
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.ProductRepository
import org.koin.dsl.module

val repositoryModule = module {
    factory<LocationRepository> {
        LocationRepositoryImpl(locationDao = get(), locationMapper = LocationMapper())
    }

    factory<AisleRepository> {
        AisleRepositoryImpl(
            aisleDao = get(), aisleMapper = AisleMapper())
    }

    factory<AisleProductRepository> {
        AisleProductRepositoryImpl(
            aisleProductDao = get(), aisleProductRankMapper = AisleProductRankMapper()
        )
    }

    factory<ProductRepository> {
        ProductRepositoryImpl(
            productDao = get(), aisleProductDao = get(), productMapper = ProductMapper()
        )
    }
}