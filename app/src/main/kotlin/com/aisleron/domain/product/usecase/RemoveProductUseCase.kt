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

import com.aisleron.domain.TransactionRunner
import com.aisleron.domain.base.usecase.RemoveUseCase
import com.aisleron.domain.note.usecase.RemoveNoteUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

interface RemoveProductUseCase : RemoveUseCase<Product> {
    suspend operator fun invoke(productId: Int)
}

class RemoveProductUseCaseImpl(
    private val productRepository: ProductRepository,
    private val removeNoteUseCase: RemoveNoteUseCase,
    private val transactionRunner: TransactionRunner
) : RemoveProductUseCase {
    override suspend fun invoke(item: Product) {
        transactionRunner.run {
            item.noteId?.let { removeNoteUseCase(item, it) }
            productRepository.remove(item)
        }
    }

    override suspend fun invoke(productId: Int) {
        val product = productRepository.get(productId)
        product?.let { invoke(it) }
    }
}