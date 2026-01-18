/*
 * Copyright (C) 2025-2026 aisleron.com
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

package com.aisleron.domain.product.usecase

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAllProductsUseCaseTest {

    private lateinit var dm: TestDependencyManager
    private lateinit var getAllProductsUseCase: GetAllProductsUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        getAllProductsUseCase = dm.getUseCase()
    }

    @Test
    fun getAllProducts_ProductsReturned_MatchesRepoList() = runTest {
        val repoProductsList = dm.getRepository<ProductRepository>().getAll()

        val getProductsList = getAllProductsUseCase()

        assertEquals(repoProductsList, getProductsList)
    }
}