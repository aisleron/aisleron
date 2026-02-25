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

package com.aisleron.ui

import android.app.Activity
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import com.aisleron.R
import com.aisleron.ui.FabHandler.FabClickedCallBack
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.resourceprovider.ResourceProvider
import com.google.android.material.color.MaterialColors
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.shape.ShapeAppearanceModel

class FabHandlerImpl(private val resourceProvider: ResourceProvider) : FabHandler {
    private var _fabMain: FloatingActionButton? = null
    private fun fabMain(activity: Activity): FloatingActionButton {
        return _fabMain ?: activity.findViewById<FloatingActionButton>(R.id.fab).also {
            if (menuClosedFabShape == null) menuClosedFabShape = it.shapeAppearanceModel
            if (menuOpenFabShape == null) {
                menuOpenFabShape = it.shapeAppearanceModel
                    .toBuilder()
                    .setAllCornerSizes(ShapeAppearanceModel.PILL)
                    .build()
            }

            _fabMain = it
        }
    }

    private var _fabAddProduct: ExtendedFloatingActionButton? = null
    private fun fabAddProduct(activity: Activity): ExtendedFloatingActionButton {
        return _fabAddProduct
            ?: activity.findViewById<ExtendedFloatingActionButton>(R.id.fab_add_product).also {
                _fabAddProduct = it
            }
    }

    private var _fabAddAisle: ExtendedFloatingActionButton? = null
    private fun fabAddAisle(activity: Activity): ExtendedFloatingActionButton {
        return _fabAddAisle
            ?: activity.findViewById<ExtendedFloatingActionButton>(R.id.fab_add_aisle).also {
                _fabAddAisle = it
            }
    }

    private var _fabAddShop: ExtendedFloatingActionButton? = null
    private fun fabAddShop(activity: Activity): ExtendedFloatingActionButton {
        return _fabAddShop ?: activity.findViewById<ExtendedFloatingActionButton>(R.id.fab_add_shop)
            .also {
                _fabAddShop = it
            }
    }

    private var fabEntries = mutableListOf<FabHandler.FabOption>()
    private var fabMenuExpanded: Boolean = false
    private var menuClosedFabShape: ShapeAppearanceModel? = null
    private var menuOpenFabShape: ShapeAppearanceModel? = null

    override fun getFabView(activity: Activity): View = fabMain(activity)

    private var _fabClickedCallBack: FabClickedCallBack? = null
    override fun setFabOnClickedListener(fabClickedCallBack: FabClickedCallBack) {
        _fabClickedCallBack = fabClickedCallBack
    }

    private fun toggleSingleFabView(fab: ExtendedFloatingActionButton, show: Boolean) {
        if (show)
            fab.show()
        else
            fab.hide()
    }

    private fun toggleFabMenu(activity: Activity, setMenuExpanded: Boolean) {
        val fabMenuItemGap = dpToPx(activity, 4f)

        // Fab menu entries are anchored center of the main fab. Calculate the offset to be
        // half the main fab height, + 2x the gap between menu entries. This makes toe fab menu
        // align to Material Design guidelines (4dp between entries, 8dp from main)
        var yTranslation = -(getActualViewHeight(fabMain(activity)) / 2) - (fabMenuItemGap * 2)

        fabMenuExpanded = setMenuExpanded
        for (fabOption in fabEntries) {
            val fab = when (fabOption) {
                FabHandler.FabOption.ADD_PRODUCT -> fabAddProduct(activity)
                FabHandler.FabOption.ADD_AISLE -> fabAddAisle(activity)
                FabHandler.FabOption.ADD_SHOP -> fabAddShop(activity)
            }

            toggleSingleFabView(fab, setMenuExpanded)

            fab.translationY = yTranslation
            yTranslation = yTranslation - getActualViewHeight(fab) - fabMenuItemGap
        }

        toggleFabMain(fabMain(activity), setMenuExpanded)
    }

    private fun getActualViewHeight(view: View): Float {
        val matchParent = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(matchParent, matchParent)
        return view.measuredHeight.toFloat()
    }

    private fun dpToPx(activity: Activity, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, activity.resources.displayMetrics
        )
    }

    override fun setFabOnClickListener(
        activity: Activity,
        fabOption: FabHandler.FabOption,
        onClickListener: View.OnClickListener
    ) {
        val fab = getFabFromOption(fabOption, activity)

        fab.setOnClickListener {
            onClickListener.onClick(it)
            toggleFabMenu(activity, false)
            _fabClickedCallBack?.fabClicked(fabOption)
        }
    }

    private fun getFabFromOption(
        fabOption: FabHandler.FabOption, activity: Activity
    ): ExtendedFloatingActionButton =
        when (fabOption) {
            FabHandler.FabOption.ADD_PRODUCT -> fabAddProduct(activity)
            FabHandler.FabOption.ADD_AISLE -> fabAddAisle(activity)
            FabHandler.FabOption.ADD_SHOP -> fabAddShop(activity)
        }

    private fun setFabMainToSingleOption(activity: Activity) {
        getFabFromOption(fabEntries.first(), activity).let {
            fabMain(activity).setImageDrawable(it.icon)
            fabMain(activity).setOnClickListener { _ -> it.callOnClick() }
        }

        fabMain(activity).show()
    }

    private fun setFabMainToMultiOption(activity: Activity) {
        val fab = fabMain(activity)
        fab.setImageDrawable(
            ResourcesCompat.getDrawable(
                activity.resources, android.R.drawable.ic_input_add, activity.theme
            )
        )

        fab.setOnClickListener {
            toggleFabMenu(activity, !fabMenuExpanded)
        }

        fab.show()
    }

    private fun rotateFab(fab: FloatingActionButton, rotation: Float) {
        val duration =
            resourceProvider.getInteger(fab.context, R.integer.fab_rotation_duration_ms).toLong()

        fab.animate()
            .rotation(rotation)
            .setDuration(duration)
            .start()
    }

    private fun getColor(
        @AttrRes colorAttributeResId: Int, fab: FloatingActionButton
    ): ColorStateList {
        val color = MaterialColors.getColor(fab, colorAttributeResId)
        return ColorStateList.valueOf(color)
    }

    private fun toggleFabMain(fab: FloatingActionButton, menuExpanded: Boolean) {
        if (menuExpanded) {
            setFabMainOpen(fab)
        } else {
            setFabMainClosed(fab)
        }
    }

    private fun setFabMainOpen(fab: FloatingActionButton) {
        rotateFab(fab, 45f)
        menuOpenFabShape?.let { fab.shapeAppearanceModel = it }
        fab.backgroundTintList =
            getColor(com.google.android.material.R.attr.colorSecondaryContainer, fab)

        fab.imageTintList =
            getColor(com.google.android.material.R.attr.colorOnSecondaryContainer, fab)
    }

    private fun setFabMainClosed(fab: FloatingActionButton) {
        rotateFab(fab, 0f)
        menuClosedFabShape?.let { fab.shapeAppearanceModel = it }
        fab.backgroundTintList =
            getColor(com.google.android.material.R.attr.colorPrimaryContainer, fab)

        fab.imageTintList =
            getColor(com.google.android.material.R.attr.colorOnPrimaryContainer, fab)
    }

    private fun hideFabViews() {
        _fabAddShop?.let { toggleSingleFabView(it, false) }
        _fabAddAisle?.let { toggleSingleFabView(it, false) }
        _fabAddProduct?.let { toggleSingleFabView(it, false) }
    }

    override fun setFabItems(activity: Activity, vararg fabOptions: FabHandler.FabOption) {
        setFabMainClosed(fabMain(activity))
        hideFabViews()

        fabEntries = fabOptions.distinctBy { it.name }.toMutableList()
        when (fabEntries.count()) {
            0 -> fabMain(activity).hide()
            1 -> setFabMainToSingleOption(activity)
            else -> setFabMainToMultiOption(activity)
        }

        toggleFabMenu(activity, false)
        setFabOnClickListener(activity, FabHandler.FabOption.ADD_SHOP) {
            val bundle = Bundler().makeAddLocationBundle()
            activity.findNavController(R.id.nav_host_fragment_content_main)
                .navigate(R.id.nav_add_shop, bundle)
        }
    }

    override fun reset() {
        _fabMain?.let { setFabMainClosed(it) }
        _fabMain = null
        _fabAddProduct = null
        _fabAddAisle = null
        _fabAddShop = null
        menuClosedFabShape = null
        menuOpenFabShape = null
    }
}