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

import android.text.InputFilter
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.aisleron.databinding.FragmentProductListItemBinding
import com.aisleron.domain.FilterType
import com.aisleron.domain.preferences.NoteHint
import com.aisleron.domain.preferences.TrackingMode
import com.aisleron.ui.productlist.aisle.AisleProductListItemRecyclerViewAdapter.AisleProductListItemListener
import java.text.DecimalFormat

class ProductListItemViewHolder(
    binding: FragmentProductListItemBinding,
    private val listener: AisleProductListItemListener,
    private val defaultTrackingMode: TrackingMode,
    private val defaultUnitOfMeasure: String,
    private val noteHint: NoteHint,
    private val listFilter: FilterType
) :
    ViewHolder(binding.root) {
    private val contentView: TextView = binding.txtProductName
    private val inStockView: CheckBox = binding.chkInStock
    private val qtySelector: LinearLayout = binding.stpQtySelector
    private val decQtyButton: ImageButton = binding.btnQtyDec
    private val incQtyButton: ImageButton = binding.btnQtyInc
    private val qtyEdit: EditText = binding.edtQty
    private val noteButton: ImageButton = binding.btnNote
    private val noteLayout: LinearLayout = binding.llProductNotePreview
    private val noteTextView: TextView = binding.txtProductNotePreview
    private val rootView = binding.root

    private var qtyWatcher: TextWatcher? = null

    fun bind(item: ProductShoppingListItem) {
        val trackingMode = when (item.trackingMode) {
            TrackingMode.DEFAULT -> defaultTrackingMode
            else -> item.trackingMode
        }

        rootView.isSelected = item.selected

        inStockView.isChecked = item.inStock
        inStockView.isVisible = trackingMode in setOf(
            TrackingMode.CHECKBOX,
            TrackingMode.CHECKBOX_QUANTITY
        ) || (listFilter == FilterType.ALL)

        inStockView.setOnClickListener { _ ->
            listener.onProductStatusChange(item, inStockView.isChecked)
        }

        inStockView.setOnLongClickListener { _ -> itemView.performLongClick() }

        // Remove any old watcher
        qtyWatcher?.let {
            qtyEdit.removeTextChangedListener(it)
            qtyWatcher = null
        }

        qtyEdit.setText(formatQty(item.qtyNeeded))
        qtyEdit.setSelection(qtyEdit.text?.length ?: 0)
        qtyEdit.hint = item.unitOfMeasure.ifEmpty { defaultUnitOfMeasure }

        val qtyIncrement = if (item.qtyIncrement > 0) item.qtyIncrement else 1.0

        // Add a new watcher and keep reference
        qtyWatcher = qtyEdit.doAfterTextChanged { editable ->
            val newQty = editable?.toString()?.toDoubleOrNull() ?: 0.0
            listener.onProductQuantityChange(item, newQty)
        }

        qtyEdit.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
            }

            false
        }

        qtySelector.isVisible = trackingMode in setOf(
            TrackingMode.QUANTITY,
            TrackingMode.CHECKBOX_QUANTITY
        )

        decQtyButton.setOnClickListener {
            val currentQty = (qtyEdit.text.toString().toDoubleOrNull() ?: 0.0)
            val newQty = maxOf(currentQty - qtyIncrement, 0.0)
            qtyEdit.setText(formatQty(newQty))
        }

        incQtyButton.setOnClickListener {
            val maxLength = qtyEdit.filters
                .filterIsInstance<InputFilter.LengthFilter>()
                .firstOrNull()
                ?.max ?: Int.MAX_VALUE

            val currentQty = (qtyEdit.text.toString().toDoubleOrNull() ?: 0.0)
            val newQty = currentQty + qtyIncrement
            val formattedQty = formatQty(newQty)

            if (formattedQty.length <= maxLength) {
                qtyEdit.setText(formattedQty)
            }
        }

        val hasNote = item.noteId != null && (item.noteText ?: "").isNotBlank()
        noteLayout.isVisible =
            hasNote && noteHint == NoteHint.SUMMARY

        val itemName = item.name +
                if (noteHint == NoteHint.INDICATOR && hasNote)
                    " *"
                else
                    ""

        contentView.text = itemName

        noteTextView.text = item.noteText
        noteTextView.setOnClickListener {
            listener.onShowNoteClick(item)
        }

        noteButton.isVisible =
            hasNote && noteHint == NoteHint.BUTTON

        noteButton.setOnClickListener {
            listener.onShowNoteClick(item)
        }
    }

    private fun formatQty(qty: Double): String =
        if (qty > 0.0)
            DecimalFormat("0.###").format(qty)
        else
            ""
}