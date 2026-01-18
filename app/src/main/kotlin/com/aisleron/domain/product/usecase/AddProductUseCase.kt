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

import com.aisleron.domain.TransactionRunner
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.aisleproduct.usecase.GetAisleProductMaxRankUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.base.usecase.AddUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

interface AddProductUseCase : AddUseCase<Product> {
    override suspend operator fun invoke(item: Product): Int
}

class AddProductUseCaseImpl(
    private val productRepository: ProductRepository,
    private val getDefaultAislesUseCase: GetDefaultAislesUseCase,
    private val addAisleProductsUseCase: AddAisleProductsUseCase,
    private val isProductNameUniqueUseCase: IsProductNameUniqueUseCase,
    private val getAisleProductMaxRankUseCase: GetAisleProductMaxRankUseCase,
    private val transactionRunner: TransactionRunner

) : AddProductUseCase {
    override suspend operator fun invoke(item: Product): Int {

        if (!isProductNameUniqueUseCase(item)) {
            throw AisleronException.DuplicateProductNameException("Product Name must be unique")
        }

        if (productRepository.get(item.id) != null) {
            throw AisleronException.DuplicateProductException("Cannot add a duplicate of an existing Product")
        }

        return transactionRunner.run {
            val newProduct = item.copy(
                id = productRepository.add(item)
            )

            // TODO: Remove aisle allocation when getting rid of default aisle concept
            val defaultAisles = getDefaultAislesUseCase().toMutableList()
            addAisleProductsUseCase(defaultAisles.map {
                AisleProduct(
                    aisleId = it.id,
                    product = newProduct,
                    rank = getAisleProductMaxRankUseCase(it) + 1,
                    id = 0
                )
            })

            newProduct.id
        }
    }
}