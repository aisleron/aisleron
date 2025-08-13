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

package com.aisleron.domain.location.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.IsAisleNameUniqueUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class AddLocationUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var addLocationUseCase: AddLocationUseCase
    private lateinit var existingLocation: Location

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val locationRepository = testData.getRepository<LocationRepository>()
        val aisleRepository = testData.getRepository<AisleRepository>()

        addLocationUseCase = AddLocationUseCaseImpl(
            locationRepository,
            AddAisleUseCaseImpl(
                aisleRepository,
                GetLocationUseCase(locationRepository),
                IsAisleNameUniqueUseCase(aisleRepository)
            ),
            GetAllProductsUseCase(testData.getRepository<ProductRepository>()),
            AddAisleProductsUseCase(testData.getRepository<AisleProductRepository>()),
            IsLocationNameUniqueUseCase(locationRepository)
        )

        existingLocation = runBlocking { locationRepository.get(1)!! }
    }

    @Test
    fun addLocation_IsDuplicateName_ThrowsException() {
        val newLocation = existingLocation.copy(id = 0, pinned = !existingLocation.pinned)
        runBlocking {
            assertThrows<AisleronException.DuplicateLocationNameException> {
                addLocationUseCase(newLocation)
            }
        }
    }

    @Test
    fun addLocation_IsExistingLocation_ThrowsException() {
        val updateLocation =
            existingLocation.copy(
                name = existingLocation.name + " Updated",
                pinned = !existingLocation.pinned
            )

        runBlocking {
            assertThrows<AisleronException.DuplicateLocationException> {
                addLocationUseCase(updateLocation)
            }
        }
    }

    private fun getNewLocation(showDefaultAisle: Boolean): Location {
        val newLocation = Location(
            id = 0,
            type = LocationType.SHOP,
            defaultFilter = FilterType.NEEDED,
            name = "Shop Add New Location",
            pinned = false,
            aisles = emptyList(),
            showDefaultAisle = showDefaultAisle
        )
        return newLocation
    }

    @ParameterizedTest(name = "Test AddLocation when showDefaultAisle is {0}")
    @MethodSource("showDefaultAisleArguments")
    fun addLocation_IsNewLocation_LocationCreated(showDefaultAisle: Boolean) {
        val newLocation = getNewLocation(showDefaultAisle)
        val countBefore: Int
        val countAfter: Int
        val insertedLocation: Location?
        runBlocking {
            val locationRepository = testData.getRepository<LocationRepository>()
            countBefore = locationRepository.getAll().count()
            val id = addLocationUseCase(newLocation)
            insertedLocation = locationRepository.get(id)
            countAfter = locationRepository.getAll().count()
        }
        assertNotNull(insertedLocation)
        assertEquals(countBefore + 1, countAfter)
        assertEquals(newLocation.name, insertedLocation?.name)
        assertEquals(newLocation.type, insertedLocation?.type)
        assertEquals(newLocation.pinned, insertedLocation?.pinned)
        assertEquals(newLocation.defaultFilter, insertedLocation?.defaultFilter)
        assertEquals(showDefaultAisle, insertedLocation?.showDefaultAisle)
    }

    @Test
    fun addLocation_LocationInserted_AddsDefaultAisle() {
        val newLocation = getNewLocation(true)
        val aisleCountBefore: Int
        val aisleCountAfter: Int
        val defaultAisle: Aisle?
        runBlocking {
            val aisleRepository = testData.getRepository<AisleRepository>()
            aisleCountBefore = aisleRepository.getAll().count()
            val id = addLocationUseCase(newLocation)
            aisleCountAfter = aisleRepository.getAll().count()
            defaultAisle = aisleRepository.getDefaultAisleFor(id)
        }
        assertNotNull(defaultAisle)
        assertEquals(aisleCountBefore + 1, aisleCountAfter)
    }

    @Test
    fun addLocation_LocationInserted_AddsAisleProducts() {
        val newLocation = getNewLocation(true)
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val productCount: Int
        val aisleProducts: List<AisleProduct>
        val defaultAisle: Aisle?
        runBlocking {
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            productCount = testData.getRepository<ProductRepository>().getAll().count()
            aisleProductCountBefore = aisleProductRepository.getAll().count()
            val id = addLocationUseCase(newLocation)
            aisleProductCountAfter = aisleProductRepository.getAll().count()
            defaultAisle = testData.getRepository<AisleRepository>().getDefaultAisleFor(id)
            aisleProducts =
                aisleProductRepository.getAll().filter { it.aisleId == defaultAisle?.id }
        }
        assertEquals(productCount, aisleProducts.count())
        assertEquals(aisleProductCountBefore + productCount, aisleProductCountAfter)
    }

    private companion object {
        @JvmStatic
        fun showDefaultAisleArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(true),
            Arguments.of(false)
        )
    }
}