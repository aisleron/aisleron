package com.aisleron.ui

import androidx.core.view.MenuHost
import androidx.fragment.app.FragmentFactory
import com.aisleron.ui.shop.ShopFragment

class AisleronFragmentFactory(
    private val addEditFragmentListener: AddEditFragmentListener,
    private val menuHost: MenuHost
) : FragmentFactory() {
    override fun instantiate(
        classLoader: ClassLoader,
        className: String
    ) = when (className) {
        ShopFragment::class.java.name -> ShopFragment(addEditFragmentListener, menuHost)
        else -> super.instantiate(classLoader, className)
    }
}