package com.aisleron.ui

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import com.aisleron.R
import com.aisleron.ui.bundles.Bundler
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FabHandler(private val activity: Activity) {

    private val fabMain = activity.findViewById<FloatingActionButton>(R.id.fab)

    private val fabAddProduct = activity.findViewById<FloatingActionButton>(R.id.fab_add_product)
    private val lblAddProduct = activity.findViewById<TextView>(R.id.add_product_fab_label)

    private val fabAddAisle = activity.findViewById<FloatingActionButton>(R.id.fab_add_aisle)
    private val lblAddAisle = activity.findViewById<TextView>(R.id.add_aisle_fab_label)

    private val fabAddShop = activity.findViewById<FloatingActionButton>(R.id.fab_add_shop)
    private val lblAddShop = activity.findViewById<TextView>(R.id.add_shop_fab_label)

    private var allFabAreHidden: Boolean = true

    private fun hideFab(fabOption: FabOption) {
        when (fabOption) {
            FabOption.ADD_PRODUCT -> hideSingleFabViews(fabAddProduct, lblAddProduct)
            FabOption.ADD_AISLE -> hideSingleFabViews(fabAddAisle, lblAddAisle)
            FabOption.ADD_SHOP -> hideSingleFabViews(fabAddShop, lblAddShop)
        }
    }

    private fun hideSingleFabViews(fab: FloatingActionButton, label: TextView) {
        fab.hide()
        label.visibility = View.GONE
    }

    private fun showFab(fabOption: FabOption) {
        when (fabOption) {
            FabOption.ADD_PRODUCT -> showSingleFabViews(fabAddProduct, lblAddProduct)
            FabOption.ADD_AISLE -> showSingleFabViews(fabAddAisle, lblAddAisle)
            FabOption.ADD_SHOP -> showSingleFabViews(fabAddShop, lblAddShop)
        }

        allFabAreHidden = false
    }

    private fun showSingleFabViews(fab: FloatingActionButton, label: TextView) {
        fab.show()
        label.visibility = View.VISIBLE
    }

    private fun hideAllFab() {
        for (fabOption in FabOption.entries) {
            hideFab(fabOption)
        }
        allFabAreHidden = true
    }

    private fun showAllFab() {
        for (fabOption in FabOption.entries) {
            showFab(fabOption)
        }
    }

    fun initializeFab() {
        hideAllFab()

        fabAddShop.setOnClickListener {
            val bundle = Bundler().makeAddLocationBundle()
            activity.findNavController(R.id.nav_host_fragment_content_main)
                .navigate(R.id.nav_add_shop, bundle)
            hideAllFab()
        }

        fabMain.setImageDrawable(
            ResourcesCompat.getDrawable(
                activity.resources, android.R.drawable.ic_input_add, activity.theme
            )
        )
        fabMain.setOnClickListener {
            if (allFabAreHidden) {
                showFab(FabOption.ADD_SHOP)
            } else {
                hideAllFab()
            }
        }
    }

    fun setFabOnClickListener(fabOption: FabOption, onClickListener: View.OnClickListener) {
        val fab = when (fabOption) {
            FabOption.ADD_PRODUCT -> fabAddProduct
            FabOption.ADD_AISLE -> fabAddAisle
            FabOption.ADD_SHOP -> fabAddShop
        }

        fab.setOnClickListener {
            onClickListener.onClick(it)
            hideAllFab()
        }
    }

    fun setModeShowAllFab() {
        fabMain.setOnClickListener {
            if (allFabAreHidden) {
                showAllFab()
            } else {
                hideAllFab()
            }
        }
    }

    enum class FabOption {
        ADD_PRODUCT, ADD_AISLE, ADD_SHOP
    }

}