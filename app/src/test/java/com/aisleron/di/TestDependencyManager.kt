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

package com.aisleron.di

import com.aisleron.testdata.data.AisleronTestDb
import kotlinx.coroutines.runBlocking

class TestDependencyManager(private val addData: Boolean = true) {
    private val db = AisleronTestDb()
    val testRepositoryFactory = TestRepositoryFactory(db)
    val testUseCaseFactory = TestUseCaseFactory(testRepositoryFactory)

    inline fun <reified T> getRepository(): T = testRepositoryFactory.get<T>()

    inline fun <reified T> getUseCase(): T = testUseCaseFactory.get<T>()

    init {
        initializeTestData()
    }

    private fun initializeTestData() {
        if (addData) {
            runBlocking { testUseCaseFactory.createSampleDataUseCase() }
        }
    }
}