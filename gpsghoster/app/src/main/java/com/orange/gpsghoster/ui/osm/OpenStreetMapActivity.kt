/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.ui.osm

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.orange.common.crypto.EncryptedFileUtil
import com.orange.common.preference.AppPreferenceDataStore
import com.orange.gpsghoster.App
import com.orange.gpsghoster.BuildConfig
import com.orange.gpsghoster.databinding.ActivityOpenStreetMapBinding
import com.orange.gpsghoster.db.entity.SensitiveAreaEntity
import com.orange.gpsghoster.db.model.SensitiveAreaViewModel
import com.orange.gpsghoster.db.model.SensitiveAreaViewModelFactory
import com.orange.gpsghoster.db.parse.ParseSensitiveArea
import com.orange.gpsghoster.db.parse.ParseUtils
import com.orange.gpsghoster.util.CommonUtils
import com.parse.DeleteCallback
import com.parse.ParseQuery
import org.jetbrains.annotations.NotNull
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class OpenStreetMapActivity : AppCompatActivity(), MapEventsReceiver,  MapListener {

    private lateinit var mContext : Context
    private lateinit var mPreferenceDataStore: AppPreferenceDataStore
    private lateinit var mSharedPreferences : SharedPreferences
    private lateinit var mEncryptedFileUtil : EncryptedFileUtil

    private lateinit var binding: ActivityOpenStreetMapBinding
    private lateinit var mMapView : MapView
    private lateinit var mMyLocationNewOverlay : MyLocationNewOverlay
    private lateinit var mCompassOverlay : CompassOverlay
//    private lateinit var mLatLonGridlineOverlay2 : LatLonGridlineOverlay2
    private lateinit var mScaleBarOverlay : ScaleBarOverlay
    private lateinit var mMinimapOverlay : MinimapOverlay
    private lateinit var mCopyrightOverlay : CopyrightOverlay
    private lateinit var mMapEventsOverlay : MapEventsOverlay
    private lateinit var mMapController : MapController

    private lateinit var mOpenStreetMapModel: OpenStreetMapModel
    private lateinit var mOpenStreetMapModelFactory: OpenStreetMapModelFactory
    private lateinit var mRequestQueue: RequestQueue

    private val mSensitiveAreaViewModel : SensitiveAreaViewModel by viewModels {
        SensitiveAreaViewModelFactory((application as App).sensitiveAreaRepository)
    }

    private var progressDialog: ProgressDialog? = null

    fun onLoadingStart(showSpinner: Boolean) {
        if (showSpinner) {
            progressDialog = ProgressDialog.show(
                this, null,
                getString(com.parse.ui.R.string.com_parse_ui_progress_dialog_text), true, false
            )
        }
    }

    fun onLoadingFinish() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mContext = applicationContext
        mPreferenceDataStore = AppPreferenceDataStore.getInstance(mContext)
        mSharedPreferences = mPreferenceDataStore.getSharedPreferences()
        mEncryptedFileUtil = EncryptedFileUtil.getInstance(this)

        // handle permissions first, before map is created. not depicted here

        // load/initialize the osmdroid configuration, this can be done
        Configuration.getInstance().load(mContext, mSharedPreferences)
        // setting this before the layout is inflated is a good idea
        // it 'should' ensure that the map has a writable location for the map cache, even without permissions
        // if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        // see also StorageUtils
        // note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        // inflate and create the map
        binding = ActivityOpenStreetMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mMapView = binding.map
        mMapView.setTileSource(TileSourceFactory.MAPNIK)
        mMapView.addMapListener(this)

        // How to add the My Location overlay
        mMyLocationNewOverlay = MyLocationNewOverlay(GpsMyLocationProvider(mContext), mMapView)
        mMyLocationNewOverlay.enableMyLocation()
        mMyLocationNewOverlay.enableFollowLocation()
        mMapView.overlays.add(mMyLocationNewOverlay)

        // How to add a compass overlay
        mCompassOverlay = CompassOverlay(mContext, InternalCompassOrientationProvider(mContext), mMapView)
        mCompassOverlay.enableCompass()
        mMapView.overlays.add(mCompassOverlay)

        // How to add the Grid line Overlay
//        mLatLonGridlineOverlay2 = LatLonGridlineOverlay2()
//        mMapView.overlays.add(mLatLonGridlineOverlay2)

        // How to enable rotation gestures
        val rotationGestureOverlay = RotationGestureOverlay(mMapView)
        rotationGestureOverlay.isEnabled
        mMapView.setMultiTouchControls(true)
        mMapView.overlays.add(rotationGestureOverlay)

        // How to add Map Scale bar overlay
        mScaleBarOverlay = ScaleBarOverlay(mMapView)
        mScaleBarOverlay.setAlignBottom(true)
        mScaleBarOverlay.setAlignRight(true)
        mMapView.overlays.add(mScaleBarOverlay)

        // How to add the built-in Minimap
        val dm : DisplayMetrics = mContext.resources.displayMetrics
        mMinimapOverlay = MinimapOverlay(mContext, mMapView.tileRequestCompleteHandler)
        mMinimapOverlay.width = dm.widthPixels / 5
        mMinimapOverlay.height = dm.heightPixels / 5
        mMinimapOverlay.padding = 50
        // optionally, you can set the minimap to a different tile source
        // minimapOverlay.setTileSource(....);
        mMapView.overlays.add(mMinimapOverlay)

        // How to add the Copyright overlay
        mCopyrightOverlay = CopyrightOverlay(mContext)
        mMapView.overlays.add(mCopyrightOverlay)

        // How to add MapEventsReceiver
        mMapEventsOverlay = MapEventsOverlay(this)
        mMapView.overlays.add(mMapEventsOverlay)

        // controler
        mMapController = mMapView.controller as MapController
        mMapController.setCenter(mMyLocationNewOverlay.myLocation)
        mMapController.setZoom(15.0)

        // model
        mRequestQueue = Volley.newRequestQueue(this)
        mOpenStreetMapModelFactory = OpenStreetMapModelFactory(mPreferenceDataStore, mEncryptedFileUtil, mRequestQueue)
        mOpenStreetMapModel = ViewModelProvider(this, mOpenStreetMapModelFactory).get(OpenStreetMapModel::class.java)
    }

    override fun onStop() {
        super.onStop()
        mRequestQueue.cancelAll("osm")
    }

    override fun onPause() {
        super.onPause()

        // this will refresh the osmdroid configuration on resuming.
        // if you make changes to the configuration, use
        Configuration.getInstance().save(mContext, mSharedPreferences)

        mMyLocationNewOverlay.disableFollowLocation()
        mMyLocationNewOverlay.disableMyLocation()
        mCompassOverlay.disableCompass()

        mMapView.onPause() // needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onResume() {
        super.onResume()

        // this will refresh the osmdroid configuration on resuming.
        // if you make changes to the configuration, use
        Configuration.getInstance().load(mContext, mSharedPreferences)

        mMapView.onResume() // needed for compass, my location overlays, v6.0.0 and up

        mMyLocationNewOverlay.enableMyLocation()
        mMyLocationNewOverlay.enableFollowLocation()
        mCompassOverlay.enableCompass()
    }

    override fun singleTapConfirmedHelper(position: GeoPoint?): Boolean {
        if (position != null) {
            onLoadingStart(true)
            with(mSensitiveAreaViewModel.contains(position)) {
                observeForever(object : Observer<List<SensitiveAreaEntity>> {
                    override fun onChanged(list: List<SensitiveAreaEntity>) {
                        if (list.isEmpty()) {
                            with(mOpenStreetMapModel.computeSensitiveArea(position)) {
                                observeForever(object : Observer<SensitiveAreaEntity> {
                                    override fun onChanged(sensitiveAreaEntity: SensitiveAreaEntity) {
                                        Log.d("!!!", "sensitiveAreaEntity : " + sensitiveAreaEntity.toString())

                                        // insert in Parse
                                        val parseSensitiveArea = ParseUtils.convertSensitiveAreaEntityToParseSensitiveArea(sensitiveAreaEntity)
//                                        parseSensitiveArea?.put("createdBy", ParseUser.getCurrentUser())
                                        parseSensitiveArea?.saveInBackground { e ->
                                            if (e == null) {
                                                // save parse object id
                                                sensitiveAreaEntity.objectId = parseSensitiveArea.objectId

                                                // insert in DB
                                                with(mSensitiveAreaViewModel.insert(sensitiveAreaEntity)) {
                                                    observeForever(object : Observer<Long> {
                                                        override fun onChanged(t: Long?) {
                                                            if (t != null && t > 0) {
                                                                // update UI
                                                                addMarkerAndPolygon(sensitiveAreaEntity)
                                                            } else {
                                                                CommonUtils.displayToast(mContext, "DdB error with item insertion")
                                                            }
                                                            onLoadingFinish()
                                                            removeObserver(this)
                                                        }
                                                    })
                                                }
                                                onLoadingFinish()
                                                removeObserver(this)
                                            } else {
                                                onLoadingFinish()
                                                Log.wtf("Error", e.localizedMessage)
                                            }
                                        }
                                    }
                                })
                            }
                        }
                        onLoadingFinish()
                        removeObserver(this)
                    }
                })
            }
            return true
        }
        return false
    }

    override fun longPressHelper(position: GeoPoint?): Boolean {
        return false
    }

    private fun addMarkerAndPolygon(@NotNull sensitiveAreaEntity : SensitiveAreaEntity) {
        val aMarker = Marker(mMapView)
        aMarker.position = sensitiveAreaEntity.marker
        aMarker.title = sensitiveAreaEntity.marker.toString().replace(",", "\n")
        aMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        val aPolygon = Polygon()
        aPolygon.outlinePaint.color = Color.BLACK
        aPolygon.fillPaint.color = Color.RED
        aPolygon.points = sensitiveAreaEntity.polygon
        aPolygon.setOnClickListener { _, _, _ ->
            with(mSensitiveAreaViewModel.get(aMarker.position)) {
                observeForever(object : Observer<List<SensitiveAreaEntity>> {
                    override fun onChanged(t: List<SensitiveAreaEntity>) {
                        if (t.isNotEmpty()) {
                            val query = ParseQuery.getQuery<ParseSensitiveArea>("SensitiveArea")
                            query.getInBackground(t.first().objectId) { o, e ->
                                if (e == null) {
                                    o.deleteInBackground(DeleteCallback { e ->
                                        if (e == null) {
                                            Log.d("msg", "deleted")
                                            with(mSensitiveAreaViewModel.delete(t.first())) {
                                                observeForever(object : Observer<Int> {
                                                    override fun onChanged(t: Int) {
                                                        if (t > 0) {
                                                            refreshUi()
                                                        } else {
                                                            CommonUtils.displayToast(mContext, "DdB error with item deletion")
                                                        }
                                                        removeObserver(this)
                                                    }
                                                })
                                            }
                                        } else {
                                            Log.d("msg", "not deleted")
                                            e.printStackTrace()
                                        }
                                    })
                                } else {
                                    Log.d("msg", "not found")
                                }
                            }
                        }
                        removeObserver(this)
                    }
                })
            }
            return@setOnClickListener true
        }

        mMapView.overlays.add(aPolygon)
        mMapView.overlays.add(aMarker)
        mMapView.invalidate()
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        refreshUi()
        return false
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        refreshUi()
        return false
    }

    fun refreshUi() {
        mMapView.overlays.forEach {
            if (it is Marker || it is Polygon) {
                mMapView.overlays.remove(it)
            }
        }
        mMapView.invalidate()

        with(mSensitiveAreaViewModel.get(mMapView.boundingBox)) {
            observeForever(object : Observer<List<SensitiveAreaEntity>> {
                override fun onChanged(list: List<SensitiveAreaEntity>) {
                    for (sensitiveAreaEntity in list) {
                        addMarkerAndPolygon(sensitiveAreaEntity)
                    }
                    removeObserver(this)
                }
            })
        }
    }

}
