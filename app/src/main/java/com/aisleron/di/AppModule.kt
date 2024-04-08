package com.aisleron.di

import androidx.room.Room
import com.aisleron.data.AisleronDatabase
import com.aisleron.data.location.LocationRepositoryImpl
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationRepository
import com.aisleron.ui.shop.ShopViewModel
import com.aisleron.ui.shoppinglist.ShoppingListViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            AisleronDatabase::class.java,
            "aisleron.db"
        ).build()
    }

    factory<LocationRepository> {
        LocationRepositoryImpl(get())
    }

    viewModel { (locationId: Int, filterType: FilterType) ->
        ShoppingListViewModel(get(), locationId, filterType)
    }

    viewModel { ShopViewModel(get()) }
}