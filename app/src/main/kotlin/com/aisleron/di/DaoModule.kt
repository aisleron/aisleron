package com.aisleron.di

import com.aisleron.data.AisleronDatabase
import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.location.LocationDao
import com.aisleron.data.product.ProductDao
import org.koin.dsl.module

val daoModule = module {
    single<LocationDao> { get<AisleronDatabase>().locationDao() }
    single<AisleDao> { get<AisleronDatabase>().aisleDao() }
    single<AisleProductDao> { get<AisleronDatabase>().aisleProductDao() }
    single<ProductDao> { get<AisleronDatabase>().productDao() }
}