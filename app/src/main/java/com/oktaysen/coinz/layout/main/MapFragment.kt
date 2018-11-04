package com.oktaysen.coinz.layout.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.oktaysen.coinz.R
import timber.log.Timber

class MapFragment: Fragment(), PermissionsListener, LocationEngineListener {
    private val PERMISSION_REQUEST = 1957
    private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    )

    private var mapView: MapView? = null
    private var map: MapboxMap? = null
    private var locationEngine: LocationEngine? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_main_map, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.map_view)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync {mapboxMap: MapboxMap? ->
            map = mapboxMap
            if (hasLocationPermissions())
                onLocationPermissionsGranted()
            else {
                requestLocationPermissions()
            }
        }
    }

    private fun hasLocationPermissions():Boolean =
            LOCATION_PERMISSIONS.fold(true) { acc, perm ->
                acc && context!!.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED }

    private fun requestLocationPermissions() {
        if (LOCATION_PERMISSIONS.fold(false) { acc, perm ->
                    acc || ActivityCompat.shouldShowRequestPermissionRationale(activity!!, perm) }) {
            AlertDialog.Builder(context!!)
                    .setTitle(R.string.location_permission_explanation_title)
                    .setMessage(R.string.location_permission_explanation)
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissions(LOCATION_PERMISSIONS, PERMISSION_REQUEST)
                    }
                    .create()
                    .show()
        } else {
            requestPermissions(LOCATION_PERMISSIONS, PERMISSION_REQUEST)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST) {
            if (hasLocationPermissions()) {
                onLocationPermissionsGranted()
            } else {
                Toast.makeText(context, R.string.location_permission_not_granted, Toast.LENGTH_LONG).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    @SuppressLint("MissingPermission")
    private fun onLocationPermissionsGranted() {
        Timber.v("Location permissions granted.")
        locationEngine = LocationEngineProvider(context).obtainBestLocationEngineAvailable();
        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY;
        locationEngine?.fastestInterval = 1000;
        locationEngine?.addLocationEngineListener(this);
        locationEngine?.activate();

        val locationComponent = map?.locationComponent
        locationComponent?.activateLocationComponent(context!!, locationEngine!!)

        locationComponent?.isLocationComponentEnabled = true
        locationComponent?.renderMode = RenderMode.NORMAL
        locationComponent?.cameraMode = CameraMode.TRACKING
        locationComponent?.forceLocationUpdate(locationEngine?.lastLocation)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLocationChanged(location: Location?) { /* No implementation */ }
    @SuppressLint("MissingPermission")
    override fun onConnected() { locationEngine?.requestLocationUpdates() }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(context, R.string.location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        Timber.v("Location permission granted?: $granted")
        if (granted) onLocationPermissionsGranted()
        else Toast.makeText(context, R.string.location_permission_not_granted, Toast.LENGTH_LONG).show()
    }
}