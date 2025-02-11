package com.aisleron.di

import com.aisleron.data.AisleronDb
import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.location.LocationDao
import com.aisleron.data.product.ProductDao
import org.koin.dsl.module

val daoTestModule = module {
    includes(databaseTestModule)

    single<LocationDao> { get<AisleronDb>().locationDao() }
    single<AisleDao> { get<AisleronDb>().aisleDao() }
    single<AisleProductDao> { get<AisleronDb>().aisleProductDao() }
    single<ProductDao> { get<AisleronDb>().productDao() }
}