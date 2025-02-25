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

package com.aisleron.domain.aisle.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.AisleRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAisleUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var getAisleUseCase: GetAisleUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        getAisleUseCase = GetAisleUseCaseImpl(testData.getRepository<AisleRepository>())
    }

    @Test
    fun getAisle_NonExistentId_ReturnNull() {
        Assertions.assertNull(runBlocking { getAisleUseCase(2001) })
    }

    @Test
    fun getAisle_ExistingId_ReturnAisle() {
        val aisle = runBlocking { getAisleUseCase(1) }
        Assertions.assertNotNull(aisle)
        Assertions.assertEquals(1, aisle!!.id)
    }
}