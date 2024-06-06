package com.aisleron.ui

import androidx.core.view.MenuHost
import androidx.core.view.MenuHostHelper
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

class TestMenuHost : MenuHost {
    private val menuHostHelper = MenuHostHelper(this::invalidateMenu)
    override fun addMenuProvider(provider: MenuProvider) {
        menuHostHelper.addMenuProvider(provider)
    }

    override fun addMenuProvider(provider: MenuProvider, owner: LifecycleOwner) {
        menuHostHelper.addMenuProvider(provider, owner)
    }

    override fun addMenuProvider(
        provider: MenuProvider,
        owner: LifecycleOwner,
        state: Lifecycle.State
    ) {
        menuHostHelper.addMenuProvider(provider, owner, state)
    }

    override fun removeMenuProvider(provider: MenuProvider) {
        menuHostHelper.removeMenuProvider(provider)
    }

    override fun invalidateMenu() {}
}