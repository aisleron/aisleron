/*
 * Copyright (C) 2026 aisleron.com
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

package com.aisleron.ui.productlist

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder

interface ProductListItemListener {
    fun onClick(item: ShoppingListItem, view: View)
    fun onProductStatusChange(item: ProductShoppingListItem, inStock: Boolean)
    fun onProductQuantityChange(item: ProductShoppingListItem, quantity: Double?)
    fun onListPositionChanged(item: ShoppingListItem, precedingItem: ShoppingListItem?)
    fun onLongClick(item: ShoppingListItem, view: View): Boolean
    fun onMoved(item: ShoppingListItem)
    fun onDragStart(viewHolder: ViewHolder)
    fun onMove(item: ShoppingListItem)
    fun hasSelectedItems(): Boolean
    fun onShowNoteClick(item: ShoppingListItem)
}