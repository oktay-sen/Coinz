package com.oktaysen.coinz.backend.pojo

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import com.mapbox.mapboxsdk.geometry.LatLng

class Coin(val id: String?, val value: Double?, val currency: Currency?, val position: GeoPoint?, val date: Timestamp?, val ownerId: String?) {
    //Empty constructor for JSON parsing.
    constructor() : this(null, null, null, null, null, null)

    enum class Currency { DOLR, SHIL, PENY, QUID }

    override fun toString() = "{Coin: ${getTitle()}}"

    @Exclude
    fun getTitle() = "%s %.2f".format(currency, value)

    @Exclude
    fun getLatLng() = if (position != null) LatLng(position.longitude, position.latitude) else null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Coin

        if (id != other.id) return false
        if (value != other.value) return false
        if (currency != other.currency) return false
        if (position != other.position) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (value?.hashCode() ?: 0)
        result = 31 * result + (currency?.hashCode() ?: 0)
        result = 31 * result + (position?.hashCode() ?: 0)
        return result
    }
}