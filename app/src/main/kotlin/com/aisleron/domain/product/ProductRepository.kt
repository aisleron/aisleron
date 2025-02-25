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

package com.aisleron.domain.product

import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.base.BaseRepository

interface ProductRepository : BaseRepository<Product> {
    suspend fun getInStock(): List<Product>
    suspend fun getNeeded(): List<Product>
    suspend fun getByFilter(filter: FilterType): List<Product>
    suspend fun getByAisle(aisle: Aisle): List<Product>
    suspend fun getByAisle(aisleId: Int): List<Product>
    suspend fun getByName(name: String): Product?
}