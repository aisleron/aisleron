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

package com.aisleron.domain.product.usecase

import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

interface CopyProductUseCase {
    suspend operator fun invoke(source: Product, newProductName: String): Int
}

class CopyProductUseCaseImpl(
    private val productRepository: ProductRepository,
    private val aisleProductRepository: AisleProductRepository,
    private val isProductNameUniqueUseCase: IsProductNameUniqueUseCase
) : CopyProductUseCase {
    override suspend fun invoke(source: Product, newProductName: String): Int {
        val newProduct = source.copy(
            id = 0,
            name = newProductName
        )

        if (!isProductNameUniqueUseCase(newProduct)) {
            throw AisleronException.DuplicateProductNameException("Product Name must be unique")
        }

        return productRepository.add(newProduct).let { newProductId ->
            productRepository.get(newProductId)?.let { addedProduct ->
                aisleProductRepository.getProductAisles(source.id).forEach { ap ->
                    aisleProductRepository.add(
                        ap.copy(
                            id = 0,
                            product = addedProduct,
                            rank = aisleProductRepository.getAisleMaxRank(ap.aisleId) + 1
                        )
                    )
                }
            }
            newProductId
        }
    }
}