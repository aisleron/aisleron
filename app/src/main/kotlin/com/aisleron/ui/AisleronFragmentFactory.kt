package com.aisleron.ui

import androidx.fragment.app.FragmentFactory
import com.aisleron.ui.shop.ShopFragment

class AisleronFragmentFactory(private val appTitleUpdateListener: AppTitleUpdateListener) : FragmentFactory() {
    override fun instantiate(
        classLoader: ClassLoader,
        className: String
    ) = when (className) {
        ShopFragment::class.java.name -> ShopFragment(appTitleUpdateListener)
        else -> super.instantiate(classLoader, className)
    }
}