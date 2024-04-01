package com.aisleron

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.databinding.ActivityMainBinding
import com.aisleron.domain.model.Location
import com.aisleron.domain.model.LocationType
import com.aisleron.placeholder.LocationData
import com.aisleron.ui.navshoplist.NavShopListRecyclerViewAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_in_stock, R.id.nav_needed_list, R.id.nav_all_items, R.id.nav_all_shops
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //Add Additional nav items
        val navShopAdapter = NavShopListRecyclerViewAdapter(
            LocationData.locations.filter { s -> s.type == LocationType.SHOP && s.pinned } ,
            object :
            NavShopListRecyclerViewAdapter.NavListShopItemListener {
            override fun onItemClick(item: Location) {
                navigateToShoppingList(item, navController, drawerLayout)
            }
        })

        val shopMenuItem: MenuItem? = navView.menu.findItem(R.id.nav_list_shop_list)
        val recyclerView: RecyclerView? = shopMenuItem?.actionView as RecyclerView?
        recyclerView?.adapter = navShopAdapter
    }

    private fun navigateToShoppingList(
        item: Location,
        navController: NavController,
        drawerLayout: DrawerLayout
    ) {
        val bundle = Bundle()
        bundle.putInt("locationId", item.id.toInt())
        bundle.putSerializable("filterType", item.defaultFilter)
        navController.navigate(R.id.nav_shopping_list, bundle)
        drawerLayout.closeDrawers()
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
}