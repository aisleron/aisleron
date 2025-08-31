package com.aisleron.ui.copyentity

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.CopyLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.CopyProductUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare

class CopyEntityViewModelTest : KoinTest {
    private lateinit var viewModel: CopyEntityViewModel

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        viewModel = get<CopyEntityViewModel>()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    @Test
    fun copyEntity_ExceptionRaised_UiStateIsError() {
        val exceptionMessage = "Error on copy Entity"

        declare<CopyLocationUseCase> {
            object : CopyLocationUseCase {
                override suspend fun invoke(source: Location, newLocationName: String): Int {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val vm = get<CopyEntityViewModel>()

        vm.copyEntity(CopyEntityType.Location(1), "Dummy Name")

        val uiState = vm.uiState.value
        Assert.assertTrue(uiState is CopyEntityViewModel.CopyUiState.Error)
        with(uiState as CopyEntityViewModel.CopyUiState.Error) {
            Assert.assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, this.errorCode)
            Assert.assertEquals(exceptionMessage, this.errorMessage)
        }
    }

    @Test
    fun copyEntity_InvalidProductId_UiStateIsInvalidProductException() {
        viewModel.copyEntity(CopyEntityType.Product(-1), "Dummy Name")

        val uiState = viewModel.uiState.value
        Assert.assertTrue(uiState is CopyEntityViewModel.CopyUiState.Error)
        with(uiState as CopyEntityViewModel.CopyUiState.Error) {
            Assert.assertEquals(
                AisleronException.ExceptionCode.INVALID_PRODUCT_EXCEPTION, this.errorCode
            )
        }
    }

    @Test
    fun copyEntity_ValidProductProvided_ProductCopied() = runTest {
        val entity = get<ProductRepository>().getAll().first()
        val copyName = "${entity.name} (Copy)"

        viewModel.copyEntity(CopyEntityType.Product(entity.id), copyName)

        val copiedEntity = get<ProductRepository>().getByName(copyName)

        val uiState = viewModel.uiState.value
        Assert.assertTrue(uiState is CopyEntityViewModel.CopyUiState.Success)
        with(uiState as CopyEntityViewModel.CopyUiState.Success) {
            Assert.assertEquals(this.newId, copiedEntity?.id)
        }
    }

    @Test
    fun copyEntity_InvalidLocationId_UiStateIsInvalidLocationException() {
        viewModel.copyEntity(CopyEntityType.Location(-1), "Dummy Name")

        val uiState = viewModel.uiState.value
        Assert.assertTrue(uiState is CopyEntityViewModel.CopyUiState.Error)
        with(uiState as CopyEntityViewModel.CopyUiState.Error) {
            Assert.assertEquals(
                AisleronException.ExceptionCode.INVALID_LOCATION_EXCEPTION, this.errorCode
            )
        }
    }

    @Test
    fun copyEntity_ValidLocationProvided_LocationCopied() = runTest {
        val entity = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        val copyName = "${entity.name} (Copy)"

        viewModel.copyEntity(CopyEntityType.Location(entity.id), copyName)

        val copiedEntity = get<LocationRepository>().getByName(copyName)

        val uiState = viewModel.uiState.value
        Assert.assertTrue(uiState is CopyEntityViewModel.CopyUiState.Success)
        with(uiState as CopyEntityViewModel.CopyUiState.Success) {
            Assert.assertEquals(this.newId, copiedEntity?.id)
        }
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_CopyEntityViewModelReturned() {
        val vm = CopyEntityViewModel(
            get<CopyLocationUseCase>(),
            get<CopyProductUseCase>(),
            get<GetProductUseCase>(),
            get<GetLocationUseCase>()
        )

        Assert.assertNotNull(vm)
    }

}