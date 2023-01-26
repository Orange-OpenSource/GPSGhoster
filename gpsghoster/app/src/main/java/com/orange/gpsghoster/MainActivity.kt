/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.orange.common.app.IApp
import com.orange.common.permission.PermissionCallback
import com.orange.common.permission.PermissionHelper
import com.orange.common.preference.AppPreferenceDataStore
import com.orange.gpsghoster.databinding.ActivityMainBinding
import com.orange.gpsghoster.db.entity.SensitiveAreaEntity
import com.orange.gpsghoster.db.model.SensitiveAreaViewModel
import com.orange.gpsghoster.db.model.SensitiveAreaViewModelFactory
import com.orange.gpsghoster.db.parse.ParseSensitiveArea
import com.orange.gpsghoster.db.parse.ParseUtils
import com.orange.gpsghoster.ui.parse.SampleDispatchActivity
import com.orange.gpsghoster.ui.settings.SettingsActivity
import com.orange.gpsghoster.util.CommonUtils
import com.orange.gpsghoster.util.Constant
import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseQuery
import com.parse.ParseUser


class MainActivity : AppCompatActivity(), PermissionCallback {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var permissionHelper: PermissionHelper
    private lateinit var preferenceDataStore: AppPreferenceDataStore

    private val mSensitiveAreaViewModel : SensitiveAreaViewModel by viewModels {
        SensitiveAreaViewModelFactory((application as App).sensitiveAreaRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionHelper = PermissionHelper(this)
        preferenceDataStore = AppPreferenceDataStore.getInstance(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            // TODO : remplacer string en dur
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
                R.id.nav_home,  /* R.id.nav_gallery, R.id.nav_slideshow */
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Ref:
        // - https://stackoverflow.com/questions/57721586/how-to-implement-sign-out-function-in-the-new-navigation-drawer-in-android-studi
        navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener { MenuItem ->
            logout()
            true
        }

        // update header view
        val title : TextView =  navView.getHeaderView(0).findViewById(R.id.title)
        val subtitle : TextView =  navView.getHeaderView(0).findViewById(R.id.subtitle)
        title.text = resources.getString(R.string.app_name)
        subtitle.text = ParseUser.getCurrentUser().email

        // check permissions
        if (!permissionHelper.checkPermission(Constant.DANGEROUS_PERMISSIONS)) {
            permissionHelper.requestPermissions(Constant.DANGEROUS_PERMISSIONS, true, this)
        } else {
            onRequestPermissionsGranted()
        }

        // insert Parse Sensitive Area of the current user
        val query : ParseQuery<ParseSensitiveArea> = ParseQuery.getQuery("SensitiveArea")
        query.whereEqualTo("createdBy", ParseUser.getCurrentUser())
        query.findInBackground(object : FindCallback<ParseSensitiveArea> {
            override fun done(objects: MutableList<ParseSensitiveArea>?, e: ParseException?) {
                if (e == null) {
                    if (objects != null) {
                        for(item in objects) {
                            with(mSensitiveAreaViewModel.contains(item.objectId)) {
                                observeForever(object : Observer<List<SensitiveAreaEntity>> {
                                    override fun onChanged(list: List<SensitiveAreaEntity>) {
                                        if (list.isEmpty()) {
                                            val sensitiveArea = ParseUtils.convertParseSensitiveAreaToSensitiveAreaEntity(item)
                                            if (sensitiveArea != null) {
                                                mSensitiveAreaViewModel.insert(sensitiveArea)
                                            }
                                        }
                                    }
                                })
                            }
                        }
                    }
                } else {
                    Log.wtf("Error", e.localizedMessage)
                }
            }

        })
    }

    private fun logout() {
        // User clicked to log out.
        ParseUser.logOut()

        // empty user data
        (applicationContext as IApp).deleteUserData();

        // FLAG_ACTIVITY_CLEAR_TASK only works on API 11, so if the user
        // logs out on older devices, we'll just exit.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            val intent: Intent = Intent(
                this@MainActivity,
                SampleDispatchActivity::class.java
            )
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK
                        or Intent.FLAG_ACTIVITY_NEW_TASK
            )
            startActivity(intent)
        } else {
            finish()
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

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        if (!permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } else {
            permissionHelper.onRequestPermissionsGranted()
        }
    }

    override fun exitApp() {
        finish()
        Process.killProcess(Process.myPid())
    }

    override fun onRequestPermissionsGranted() {
        // TODO : remplacer string en dur
        CommonUtils.displayToast(this, "permissions are granted")
    }
}
