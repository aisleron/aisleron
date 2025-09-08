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

package com.aisleron

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.aisleron.databinding.ActivityMainBinding
import com.aisleron.ui.FabHandlerImpl
import com.aisleron.ui.settings.DisplayPreferences
import com.aisleron.ui.settings.DisplayPreferencesImpl
import com.aisleron.ui.settings.WelcomePreferencesImpl
import com.google.android.material.navigation.NavigationView
import org.koin.androidx.fragment.android.setupKoinFragmentFactory


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        setupKoinFragmentFactory()

        super.onCreate(savedInstanceState)

        // Needs to be a standalone variable so it is not garbage collected
        prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { p, s ->
            when (s) {
                "application_theme" -> recreate()
                "restore_database" -> softRestartApp()
                "display_lockscreen" -> setShowOnLockScreen(p.getBoolean(s, false))
            }
        }

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)

        val displayPreferences = DisplayPreferencesImpl()

        setShowOnLockScreen(displayPreferences.showOnLockScreen(this))

        when (displayPreferences.applicationTheme(this)) {
            DisplayPreferences.ApplicationTheme.LIGHT_THEME ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            DisplayPreferences.ApplicationTheme.DARK_THEME ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        setWindowInsetListeners()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_in_stock,
                R.id.nav_needed,
                R.id.nav_all_items,
                R.id.nav_all_shops,
                R.id.nav_shopping_list
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, _, _ ->
            val appBarLayout = binding.appBarMain.appBarLayout
            appBarLayout.setExpanded(true, true)

            drawerLayout.closeDrawers()
            FabHandlerImpl().setFabItems(this)
        }

        if (!WelcomePreferencesImpl().isInitialized(this)) {
            navController.navigate(R.id.nav_welcome)
        }
    }

    private fun setShowOnLockScreen(show: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(show)
        }
    }

    private fun setWindowInsetListeners() {
        //TODO: Change system bar style to auto to allow for black status bar text
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(getStatusBarColor()))

        // Fab margins
        val fab = binding.appBarMain.fab
        ViewCompat.setOnApplyWindowInsetsListener(fab) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.navigationBars()
                        or WindowInsetsCompat.Type.ime()
            )

            view.updateLayoutParams<MarginLayoutParams> {
                val fabMargins = resources.getDimensionPixelSize(R.dimen.fab_margin_bottom)
                bottomMargin = fabMargins + insets.bottom
            }

            windowInsets
        }

        // AppBar
        val appBar = binding.appBarMain.appBarLayout
        ViewCompat.setOnApplyWindowInsetsListener(appBar) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.statusBars()
                        or WindowInsetsCompat.Type.navigationBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )

            view.updatePadding(top = insets.top, right = insets.right, left = insets.left)

            /*view.updateLayoutParams<MarginLayoutParams> {
                topMargin = insets.top
                leftMargin = insets.left
                rightMargin = insets.right
            }
*/
            /*val actionBarHeight = resources.getDimensionPixelSize(R.dimen.toolbar_height)

            val params = view.layoutParams
            params.height = actionBarHeight + insets.top
            view.layoutParams = params

            view.updatePadding(top = insets.top, right = insets.right, left = insets.left)*/

            windowInsets
        }

        //Navigation Drawer
        val drawer = binding.navView
        ViewCompat.setOnApplyWindowInsetsListener(drawer) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.navigationBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )

            val header = view.findViewById<FrameLayout>(R.id.nav_header_frame)
            header.updatePadding(left = insets.left)

            val menu = view.findViewById<LinearLayout>(R.id.navigation_menu_items)
            menu.updatePadding(left = insets.left, bottom = insets.bottom)

            windowInsets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        if (binding.drawerLayout.isOpen) {
            binding.drawerLayout.closeDrawers()
        }

        super.onResume()
    }

    private fun getStatusBarColor(): Int {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            resolveThemeColor(android.R.attr.statusBarColor)
        } else {
            Color.TRANSPARENT
        }
    }

    private fun resolveThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()

        val wasResolved = theme.resolveAttribute(attrResId, typedValue, true)
        if (!wasResolved) {
            throw IllegalArgumentException("Attribute 0x${attrResId.toString(16)} not defined in theme")
        }

        return if (typedValue.resourceId != 0) {
            ContextCompat.getColor(this, typedValue.resourceId)
        } else {
            typedValue.data
        }
    }

    private fun softRestartApp() {
        viewModelStore.clear()
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.popBackStack(navController.graph.startDestinationId, false)
        navController.navigate(navController.graph.startDestinationId)
        recreate()
    }

    override fun onDestroy() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        super.onDestroy()
    }
}