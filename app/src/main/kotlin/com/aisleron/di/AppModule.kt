package com.aisleron.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aisleron.data.AisleronDatabase
import com.aisleron.data.DbInitializer
import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisle.AisleMapper
import com.aisleron.data.aisle.AisleRepositoryImpl
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.aisleproduct.AisleProductRankMapper
import com.aisleron.data.aisleproduct.AisleProductRepositoryImpl
import com.aisleron.data.location.LocationDao
import com.aisleron.data.location.LocationMapper
import com.aisleron.data.location.LocationRepositoryImpl
import com.aisleron.data.maintenance.DatabaseMaintenanceImpl
import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductMapper
import com.aisleron.data.product.ProductRepositoryImpl
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.GetAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.RemoveDefaultAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCaseImpl
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.aisleproduct.usecase.RemoveProductsFromAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.backup.DatabaseMaintenance
import com.aisleron.domain.backup.usecase.BackupDatabaseUseCase
import com.aisleron.domain.backup.usecase.BackupDatabaseUseCaseImpl
import com.aisleron.domain.backup.usecase.RestoreDatabaseUseCase
import com.aisleron.domain.backup.usecase.RestoreDatabaseUseCaseImpl
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.usecase.AddLocationUseCase
import com.aisleron.domain.location.usecase.AddLocationUseCaseImpl
import com.aisleron.domain.location.usecase.GetHomeLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.GetPinnedShopsUseCase
import com.aisleron.domain.location.usecase.GetShopsUseCase
import com.aisleron.domain.location.usecase.IsLocationNameUniqueUseCase
import com.aisleron.domain.location.usecase.RemoveLocationUseCase
import com.aisleron.domain.location.usecase.RemoveLocationUseCaseImpl
import com.aisleron.domain.location.usecase.UpdateLocationUseCase
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.AddProductUseCaseImpl
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.domain.product.usecase.IsProductNameUniqueUseCase
import com.aisleron.domain.product.usecase.RemoveProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCase
import com.aisleron.domain.product.usecase.UpdateProductStatusUseCaseImpl
import com.aisleron.domain.product.usecase.UpdateProductUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
import com.aisleron.ui.AddEditFragmentListener
import com.aisleron.ui.AddEditFragmentListenerImpl
import com.aisleron.ui.ApplicationTitleUpdateListener
import com.aisleron.ui.ApplicationTitleUpdateListenerImpl
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandlerImpl
import com.aisleron.ui.about.AboutViewModel
import com.aisleron.ui.product.ProductViewModel
import com.aisleron.ui.settings.DisplayPreferences
import com.aisleron.ui.settings.DisplayPreferencesImpl
import com.aisleron.ui.settings.SettingsViewModel
import com.aisleron.ui.settings.ShoppingListPreferences
import com.aisleron.ui.settings.ShoppingListPreferencesImpl
import com.aisleron.ui.settings.WelcomePreferences
import com.aisleron.ui.settings.WelcomePreferencesImpl
import com.aisleron.ui.shop.ShopViewModel
import com.aisleron.ui.shoplist.ShopListViewModel
import com.aisleron.ui.shoppinglist.ShoppingListViewModel
import com.aisleron.ui.welcome.WelcomeViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            AisleronDatabase::class.java,
            "aisleron.db"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                DbInitializer(get(), get()).invoke()
            }
        }).build()
    }

    /**
     * Data Access Objects
     */
    single<LocationDao> { get<AisleronDatabase>().locationDao() }

    single<AisleDao> { get<AisleronDatabase>().aisleDao() }

    single<AisleProductDao> { get<AisleronDatabase>().aisleProductDao() }

    single<ProductDao> { get<AisleronDatabase>().productDao() }

    /**
     * Repositories
     */
    factory<LocationRepository> {
        LocationRepositoryImpl(locationDao = get(), locationMapper = LocationMapper())
    }

    factory<AisleRepository> {
        AisleRepositoryImpl(aisleDao = get(), aisleMapper = AisleMapper())
    }

    factory<AisleProductRepository> {
        AisleProductRepositoryImpl(
            aisleProductDao = get(),
            aisleProductRankMapper = AisleProductRankMapper()
        )
    }

    factory<ProductRepository> {
        ProductRepositoryImpl(
            productDao = get(),
            aisleProductDao = get(),
            productMapper = ProductMapper()
        )
    }

    /**
     * Location Use Cases
     */
    factory<GetLocationUseCase> { GetLocationUseCase(locationRepository = get()) }

    factory<GetShopsUseCase> { GetShopsUseCase(locationRepository = get()) }

    factory<GetPinnedShopsUseCase> { GetPinnedShopsUseCase(locationRepository = get()) }

    factory<IsLocationNameUniqueUseCase> { IsLocationNameUniqueUseCase(locationRepository = get()) }

    factory<GetHomeLocationUseCase> { GetHomeLocationUseCase(locationRepository = get()) }

    factory<UpdateLocationUseCase> {
        UpdateLocationUseCase(
            locationRepository = get(),
            isLocationNameUniqueUseCase = get()
        )
    }

    factory<RemoveLocationUseCase> {
        RemoveLocationUseCaseImpl(
            locationRepository = get(),
            removeAisleUseCase = get(),
            removeDefaultAisleUseCase = get()
        )
    }

    factory<AddLocationUseCase> {
        AddLocationUseCaseImpl(
            locationRepository = get(),
            addAisleUseCase = get(),
            getAllProductsUseCase = get(),
            addAisleProductsUseCase = get(),
            isLocationNameUniqueUseCase = get()
        )
    }

    /**
     * Aisle Use Cases
     */
    factory<AddAisleUseCase> {
        AddAisleUseCaseImpl(
            aisleRepository = get(),
            getLocationUseCase = get()
        )
    }

    factory<GetDefaultAislesUseCase> { GetDefaultAislesUseCase(aisleRepository = get()) }

    factory<UpdateAisleUseCase> {
        UpdateAisleUseCaseImpl(
            aisleRepository = get(),
            getLocationUseCase = get()
        )
    }

    factory<UpdateAisleRankUseCase> { UpdateAisleRankUseCase(aisleRepository = get()) }

    factory<GetAisleUseCase> { GetAisleUseCaseImpl(aisleRepository = get()) }

    factory<RemoveDefaultAisleUseCase> {
        RemoveDefaultAisleUseCase(
            aisleRepository = get(),
            removeProductsFromAisleUseCase = get()
        )
    }

    factory<RemoveAisleUseCase> {
        RemoveAisleUseCaseImpl(
            aisleRepository = get(),
            updateAisleProductsUseCase = get(),
            removeProductsFromAisleUseCase = get()
        )
    }

    /**
     * Aisle Product Use Cases
     */
    factory<AddAisleProductsUseCase> { AddAisleProductsUseCase(aisleProductRepository = get()) }

    factory<UpdateAisleProductsUseCase> { UpdateAisleProductsUseCase(aisleProductRepository = get()) }

    factory<UpdateAisleProductRankUseCase> { UpdateAisleProductRankUseCase(aisleProductRepository = get()) }

    factory<RemoveProductsFromAisleUseCase> { RemoveProductsFromAisleUseCase(aisleProductRepository = get()) }

    /**
     * Product Use Cases
     */
    factory<GetAllProductsUseCase> { GetAllProductsUseCase(productRepository = get()) }

    factory<GetProductUseCase> { GetProductUseCase(productRepository = get()) }

    factory<RemoveProductUseCase> { RemoveProductUseCase(productRepository = get()) }

    factory<IsProductNameUniqueUseCase> { IsProductNameUniqueUseCase(productRepository = get()) }

    factory<UpdateProductUseCase> {
        UpdateProductUseCase(
            productRepository = get(),
            isProductNameUniqueUseCase = get()
        )
    }

    factory<AddProductUseCase> {
        AddProductUseCaseImpl(
            productRepository = get(),
            getDefaultAislesUseCase = get(),
            addAisleProductsUseCase = get(),
            isProductNameUniqueUseCase = get()
        )
    }

    factory<UpdateProductStatusUseCase> {
        UpdateProductStatusUseCaseImpl(
            getProductUseCase = get(),
            updateProductUseCase = get()
        )
    }

    /**
     * Shopping List Use Cases
     */
    factory<GetShoppingListUseCase> { GetShoppingListUseCase(locationRepository = get()) }

    /**
     * Backup Use Cases
     */
    factory<DatabaseMaintenance> {
        DatabaseMaintenanceImpl(database = get(), context = androidApplication())
    }

    factory<BackupDatabaseUseCase> { BackupDatabaseUseCaseImpl(databaseMaintenance = get()) }
    factory<RestoreDatabaseUseCase> { RestoreDatabaseUseCaseImpl(databaseMaintenance = get()) }

    /**
     * Sample Data Use Cases
     */
    factory<CreateSampleDataUseCase> {
        CreateSampleDataUseCase(
            addProductUseCase = get(),
            addAisleUseCase = get(),
            getShoppingListUseCase = get(),
            updateAisleProductRankUseCase = get(),
            addLocationUseCase = get(),
            getAllProductsUseCase = get(),
            getHomeLocationUseCase = get()
        )
    }

    /**
     * ViewModels
     */
    viewModel {
        ShoppingListViewModel(
            getShoppingListUseCase = get(),
            updateProductStatusUseCase = get(),
            addAisleUseCase = get(),
            updateAisleUseCase = get(),
            updateAisleProductRankUseCase = get(),
            updateAisleRankUseCase = get(),
            removeAisleUseCase = get(),
            removeProductUseCase = get(),
            getAisleUseCase = get()
        )
    }

    viewModel {
        ShopViewModel(
            addLocationUseCase = get(),
            updateLocationUseCase = get(),
            getLocationUseCase = get()
        )
    }

    viewModel {
        ShopListViewModel(
            getShopsUseCase = get(),
            getPinnedShopsUseCase = get(),
            removeLocationUseCase = get(),
            getLocationUseCase = get()
        )
    }

    viewModel {
        ProductViewModel(
            addProductUseCase = get(),
            updateProductUseCase = get(),
            getProductUseCase = get()
        )
    }

    viewModel {
        SettingsViewModel(
            backupDatabaseUseCase = get(),
            restoreDatabaseUseCase = get()
        )
    }

    viewModel {
        AboutViewModel()
    }

    viewModel {
        WelcomeViewModel(
            createSampleDataUseCase = get()
        )
    }

    /**
     * Misc
     */
    factory<FabHandler> { FabHandlerImpl() }

    factory<ShoppingListPreferences> { ShoppingListPreferencesImpl() }

    factory<WelcomePreferences> { WelcomePreferencesImpl() }

    factory<DisplayPreferences> { DisplayPreferencesImpl() }

    factory<ApplicationTitleUpdateListener> { ApplicationTitleUpdateListenerImpl() }

    factory<AddEditFragmentListener> { AddEditFragmentListenerImpl() }
}