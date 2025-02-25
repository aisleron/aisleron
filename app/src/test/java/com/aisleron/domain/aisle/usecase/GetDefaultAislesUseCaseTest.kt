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

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.data.TestDataManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetDefaultAislesUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var getDefaultAislesUseCase: GetDefaultAislesUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        getDefaultAislesUseCase = GetDefaultAislesUseCase(testData.getRepository<AisleRepository>())
    }

    @Test
    fun getDefaultAisles_AislesReturned_MatchesRepoList() {
        val getAisleList: List<Aisle>
        val repoAisleList: List<Aisle>

        runBlocking {
            repoAisleList =
                testData.getRepository<AisleRepository>().getAll().filter { it.isDefault }
            getAisleList = getDefaultAislesUseCase()
        }

        Assertions.assertEquals(repoAisleList, getAisleList)
    }
}