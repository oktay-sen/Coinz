package com.oktaysen.coinz

import android.app.Application
import android.util.Log
import com.mapbox.mapboxsdk.Mapbox
import timber.log.Timber

class Application:Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("Application", "Initializing application.")

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String? {
                return super.createStackElementTag(element) + ':' + element.lineNumber
            }
        })
    }
}