package com.oktaysen.coinz.backend.pojo

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties
import com.mapbox.mapboxsdk.geometry.LatLng
import timber.log.Timber
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.NullPointerException

class Coin(val id: String?, val value: Double?, val currency: Currency?, val position: GeoPoint?, val date: Timestamp?, val ownerId: String?) {
    //Empty constructor for JSON parsing.
    constructor() : this(null, null, null, null, null, null)

    enum class Currency { DOLR, SHIL, PENY, QUID }

    override fun toString() = "{Coin: ${getTitle()}}"

    @Exclude
    fun getTitle() = "%s %.2f".format(currency, value)

    @Exclude
    fun getLatLng() = if (position != null) LatLng(position.longitude, position.latitude) else null
}