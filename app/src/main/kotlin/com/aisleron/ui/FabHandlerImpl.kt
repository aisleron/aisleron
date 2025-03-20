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

package com.aisleron.ui

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import com.aisleron.R
import com.aisleron.ui.FabHandler.FabClickedCallBack
import com.aisleron.ui.bundles.Bundler
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FabHandlerImpl : FabHandler {

    private fun fabMain(activity: Activity) = activity.findViewById<FloatingActionButton>(R.id.fab)

    private fun fabAddProduct(activity: Activity) =
        activity.findViewById<FloatingActionButton>(R.id.fab_add_product)

    private fun lblAddProduct(activity: Activity) =
        activity.findViewById<TextView>(R.id.add_product_fab_label)

    private fun fabAddAisle(activity: Activity) =
        activity.findViewById<FloatingActionButton>(R.id.fab_add_aisle)

    private fun lblAddAisle(activity: Activity) =
        activity.findViewById<TextView>(R.id.add_aisle_fab_label)

    private fun fabAddShop(activity: Activity) =
        activity.findViewById<FloatingActionButton>(R.id.fab_add_shop)

    private fun lblAddShop(activity: Activity) =
        activity.findViewById<TextView>(R.id.add_shop_fab_label)

    private var fabEntries = mutableListOf<FabHandler.FabOption>()

    private var allFabAreHidden: Boolean = true

    override fun getFabView(activity: Activity): View? = fabMain(activity)

    private var _fabClickedCallBack: FabClickedCallBack? = null
    override fun setFabOnClickedListener(fabClickedCallBack: FabClickedCallBack) {
        _fabClickedCallBack = fabClickedCallBack
    }

    private fun hideSingleFabViews(fab: FloatingActionButton, label: TextView) {
        fab.hide()
        label.visibility = View.GONE
    }

    private fun showSingleFabViews(fab: FloatingActionButton, label: TextView) {
        fab.show()
        label.visibility = View.VISIBLE
    }

    private fun hideAllFab(activity: Activity) {
        for (fabOption in FabHandler.FabOption.entries) {
            when (fabOption) {
                FabHandler.FabOption.ADD_PRODUCT -> hideSingleFabViews(
                    fabAddProduct(activity),
                    lblAddProduct(activity)
                )

                FabHandler.FabOption.ADD_AISLE -> hideSingleFabViews(
                    fabAddAisle(activity),
                    lblAddAisle(activity)
                )

                FabHandler.FabOption.ADD_SHOP -> hideSingleFabViews(
                    fabAddShop(activity),
                    lblAddShop(activity)
                )
            }
        }

        allFabAreHidden = true
    }

    private fun showAllFab(activity: Activity) {
        for (fabOption in fabEntries) {
            when (fabOption) {
                FabHandler.FabOption.ADD_PRODUCT -> showSingleFabViews(
                    fabAddProduct(activity),
                    lblAddProduct(activity)
                )

                FabHandler.FabOption.ADD_AISLE -> showSingleFabViews(
                    fabAddAisle(activity),
                    lblAddAisle(activity)
                )

                FabHandler.FabOption.ADD_SHOP -> showSingleFabViews(
                    fabAddShop(activity),
                    lblAddShop(activity)
                )
            }
        }

        allFabAreHidden = false
    }

    override fun setFabOnClickListener(
        activity: Activity,
        fabOption: FabHandler.FabOption,
        onClickListener: View.OnClickListener
    ) {
        val fab = getFabFromOption(fabOption, activity)

        fab.setOnClickListener {
            onClickListener.onClick(it)
            hideAllFab(activity)
            _fabClickedCallBack?.fabClicked(fabOption)
        }
    }

    private fun getFabFromOption(
        fabOption: FabHandler.FabOption, activity: Activity
    ): FloatingActionButton =
        when (fabOption) {
            FabHandler.FabOption.ADD_PRODUCT -> fabAddProduct(activity)
            FabHandler.FabOption.ADD_AISLE -> fabAddAisle(activity)
            FabHandler.FabOption.ADD_SHOP -> fabAddShop(activity)
        }

    private fun setMainFabToSingleOption(activity: Activity) {
        getFabFromOption(fabEntries.first(), activity).let {
            fabMain(activity).setImageDrawable(it.drawable)
            fabMain(activity).setOnClickListener { _ -> it.callOnClick() }
        }

        fabMain(activity).show()
    }

    private fun setMainFabToMultiOption(activity: Activity) {
        val fab = fabMain(activity)
        fab.setImageDrawable(
            ResourcesCompat.getDrawable(
                activity.resources, android.R.drawable.ic_input_add, activity.theme
            )
        )

        fab.setOnClickListener {
            if (allFabAreHidden) {
                showAllFab(activity)
            } else {
                hideAllFab(activity)
            }
        }

        fab.show()
    }

    override fun setFabItems(activity: Activity, vararg fabOptions: FabHandler.FabOption) {
        fabEntries = fabOptions.distinctBy { it.name }.toMutableList()
        when (fabEntries.count()) {
            0 -> fabMain(activity).hide()
            1 -> setMainFabToSingleOption(activity)
            else -> setMainFabToMultiOption(activity)
        }

        hideAllFab(activity)
        setFabOnClickListener(activity, FabHandler.FabOption.ADD_SHOP) {
            val bundle = Bundler().makeAddLocationBundle()
            activity.findNavController(R.id.nav_host_fragment_content_main)
                .navigate(R.id.nav_add_shop, bundle)
        }
    }


}