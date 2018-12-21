package com.oktaysen.coinz.layout.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
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
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.oktaysen.coinz.R
import com.oktaysen.coinz.backend.ListenerRegistry
import com.oktaysen.coinz.backend.Map
import com.oktaysen.coinz.backend.pojo.Coin
import kotlinx.android.synthetic.main.fragment_main_map.*
import timber.log.Timber

class MapFragment: Fragment(), PermissionsListener, LocationEngineListener {
    private val PERMISSION_REQUEST = 1957
    private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    )
    private var map: MapboxMap? = null
    private var locationEngine: LocationEngine? = null
    private val markers: MutableMap<Marker, Coin> = mutableMapOf()
    private val markersById: MutableMap<String, Marker> = mutableMapOf()

    private var listenerId: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_main_map, container, false)
        return view
    }

    override fun onStart() {
        super.onStart()
        map_view.onStart()

        //TODO: Remove the following line later.

        current_location_button.setOnClickListener {
            if (map == null || map?.locationComponent?.isLocationComponentEnabled != true) return@setOnClickListener
            map?.locationComponent?.cameraMode = CameraMode.TRACKING
        }

        playable_area_button.setOnClickListener {
            if (map == null) return@setOnClickListener
            map?.locationComponent?.cameraMode = CameraMode.NONE
            map?.animateCamera(CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                            .target(LatLng(55.944425, -3.188396))
                            .bearing(0.0)
                            .zoom(15.0)
                            .build()
            ), 750)
        }

        map_view.getMapAsync {mapboxMap: MapboxMap? ->
            map = mapboxMap
            setUpCoins()
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

    private fun setUpCoins() {
        if (listenerId == null) {
            Timber.v("listenToMap calling")
            listenerId = Map().listenToMap { coins, added, changed, removed ->
                activity?.runOnUiThread {
                    Timber.v("Today's coins are: $coins")

                    val dolrIcon = IconFactory.getInstance(context!!).fromBitmap(getBitmap(R.drawable.pin_dolr)!!)
                    val penyIcon = IconFactory.getInstance(context!!).fromBitmap(getBitmap(R.drawable.pin_peny)!!)
                    val quidIcon = IconFactory.getInstance(context!!).fromBitmap(getBitmap(R.drawable.pin_quid)!!)
                    val shilIcon = IconFactory.getInstance(context!!).fromBitmap(getBitmap(R.drawable.pin_shil)!!)

                    val getIcon = { currency: Coin.Currency? ->
                        when (currency) {
                            Coin.Currency.DOLR -> dolrIcon
                            Coin.Currency.SHIL -> shilIcon
                            Coin.Currency.PENY -> penyIcon
                            Coin.Currency.QUID -> quidIcon
                            null -> quidIcon
                        }
                    }

                    added.forEach { coin ->
                        val markerOptions = MarkerOptions()
                                .position(coin.getLatLng())
                                .title(coin.getTitle())
                                .icon(getIcon(coin.currency))
                        val marker = map?.addMarker(markerOptions)
                        if (marker == null) {
                            Timber.e("Marker is null!")
                        } else {
                            markers[marker] = coin
                            markersById[coin.id!!] = marker
                            Timber.d("Added marker for $coin")
                        }
                    }

                    changed.forEach { coin ->
                        val marker = markersById[coin.id!!] ?: return@forEach
                        markers[marker] = coin
                        marker.position = coin.getLatLng()
                        marker.title = coin.getTitle()
                        marker.icon = getIcon(coin.currency)
                        Timber.d("Modified marker for $coin")
                    }

                    removed.forEach { coin ->
                        val marker = markersById[coin.id!!]
                        markers.remove(marker)
                        markersById.remove(coin.id)
                        marker?.remove()
                        Timber.d("Removed marker for $coin")
                    }
                }
            }

            //TODO: Remove this listener before release.
            map!!.setOnMarkerClickListener { marker ->
                Timber.v("Clicked on ${marker.title}")
                val coin = markers[marker] ?: return@setOnMarkerClickListener false
                Timber.v("Clicked on $coin")
                Map().collectCoin(markers[marker]!!) { success ->
                    Timber.v("Collecting coin $coin with id ${coin.id} success: $success")
                    Snackbar.make(activity!!.findViewById(R.id.container), "Collected ${coin.getTitle()}", Snackbar.LENGTH_LONG)
                            .setAction("View") {
                                if (activity is MainActivity)
                                    (activity as MainActivity).navigateTo(R.id.navigation_inventory)
                            }
                            .show()
                }
                return@setOnMarkerClickListener true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        map_view.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        map_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        map_view.onPause()
    }

    override fun onStop() {
        super.onStop()
        Timber.v("onStop")
        map_view.onStop()
        if (listenerId != null) {
            val result = ListenerRegistry().unregister(listenerId!!)
            if (result) {
                listenerId = null
                markers.keys.forEach { it.remove() }
                markers.clear()
                markersById.clear()
            }
            Timber.v("Map listener removed: $result")
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map_view.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        map_view.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map_view.onSaveInstanceState(outState)
    }

    override fun onLocationChanged(location: Location?) {
        if (location == null) return
        val here = LatLng(location.latitude, location.longitude)
        markers.values.filter { here.distanceTo(it.getLatLng()) <= 25 }
                .forEach { coin -> Map().collectCoin(coin) { success ->
                    Timber.v("Collecting coin $coin with id ${coin.id} success: $success")
                    activity?.runOnUiThread {
                        Snackbar.make(activity!!.findViewById(R.id.container), "Collected ${coin.getTitle()}", Snackbar.LENGTH_LONG)
                                .setAction("View") {
                                    if (activity is MainActivity)
                                        (activity as MainActivity).navigateTo(R.id.navigation_inventory)
                                }
                                .show()
                    }
                } }
    }

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

    //From: https://stackoverflow.com/a/35574775
    private fun getBitmap(drawableRes: Int): Bitmap? {
        val drawable = context?.getDrawable(drawableRes) ?: return null
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable. intrinsicHeight)
        drawable.draw(canvas)

        return bitmap
    }
}