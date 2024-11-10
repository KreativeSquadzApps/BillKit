package com.kreativesquadz.billkit

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.kreativesquadz.billkit.databinding.ActivityMainBinding
import com.kreativesquadz.billkit.ui.home.HomeFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity  : AppCompatActivity(), HomeFragment.DrawerToggleListener {
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
        viewModel.getSesssion()
        observers()
        navigationSetup()
    }

    private fun observers(){
        viewModel.companyDetails.observe(this){

        }
        viewModel.invoicePrefixNumberList.observe(this){

        }
        viewModel.customer.observe(this){

        }
        viewModel.gstTax.observe(this){

        }
    }
    private fun navigationSetup(){
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        manageDrawerItemVisibility(navView)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.billHistoryFrag, R.id.creditNoteFrag,
                R.id.inventoryFrag, R.id.customerManagementFrag, R.id.staffManagementFrag,
                R.id.creditDetailsFrag, R.id.dayBookFrag, R.id.settingsFrag,
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        hideActionBar(navController)
    }

    private fun hideActionBar(navController: NavController){
        navController.addOnDestinationChangedListener { controller: NavController?, destination: NavDestination, arguments: Bundle? ->
            when (destination.id) {
                R.id.receiptFrag -> {
                    if (supportActionBar != null) {
                        supportActionBar!!.hide()
                    }
                }
                R.id.nav_home ->{
                    if (supportActionBar != null) {
                        supportActionBar!!.hide()
                    }
                }
                else -> {
                    if (supportActionBar != null) {
                        supportActionBar!!.show()

                    }

                }
            }
        }
    }

    override fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }
    private fun manageDrawerItemVisibility(nav : NavigationView) {
        val menu = nav.menu
        val home = menu.findItem(R.id.nav_home)
        val billHistory = menu.findItem(R.id.billHistoryFrag)
        val creditNote = menu.findItem(R.id.creditNoteFrag)
        val inventory = menu.findItem(R.id.inventoryFrag)
        val customerManagement = menu.findItem(R.id.customerManagementFrag)
        val staffManagement = menu.findItem(R.id.staffManagementFrag)
        val creditDetails = menu.findItem(R.id.creditDetailsFrag)
        val dayBook = menu.findItem(R.id.dayBookFrag)
        val settings = menu.findItem(R.id.settingsFrag)

        viewModel.loginResponse.observe(this) {
            it?.staff?.let {
                settings.isVisible = true
                home.isVisible = true
                val permissions  =  it.permissions.split(",")
                permissions.forEach {permission ->
                    when (permission){
                        billHistory.title -> {
                            billHistory.isVisible = true
                        }
                        creditNote.title -> {
                            creditNote.isVisible = true
                        }
                        inventory.title -> {
                            inventory.isVisible = true
                        }
                        customerManagement.title -> {
                            customerManagement.isVisible = true
                        }
                        staffManagement.title -> {
                            staffManagement.isVisible = true
                        }
                        creditDetails.title -> {
                            creditDetails.isVisible = true
                        }
                        dayBook.title -> {
                            dayBook.isVisible = true
                        }
                        else -> {
                            Toast.makeText(this, "No Permission", Toast.LENGTH_SHORT).show()
                        }

                    }

                }
            }
            it?.user?.let {
                home.isVisible = true
                billHistory.isVisible = true
                creditNote.isVisible = true
                inventory.isVisible = true
                customerManagement.isVisible = true
                staffManagement.isVisible = true
                creditDetails.isVisible = true
                dayBook.isVisible = true
                settings.isVisible = true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }




//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }


}