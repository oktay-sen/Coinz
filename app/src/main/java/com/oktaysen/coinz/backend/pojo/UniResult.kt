package com.oktaysen.coinz.backend.pojo

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class UniResult (
        val type: String,
        val `date-generated`: String,
        val `time-generated`: String,
        val `approximate-time-remaining`: String,
        val rates: Rates,
        val features: List<Feature>
){
    inner class Feature(
            val type: String,
            val properties: Properties,
            val geometry: Geometry
    ){
        inner class Properties (
                val id: String,
                val value: String,
                val currency: String,
                val `marker-symbol`: String,
                val `marker-color`: String
        )
        inner class Geometry(
                val type: String,
                val coordinates: List<Double>
        )
    }

    companion object {
        @JvmStatic
        fun fromString(gson: Gson, str: String): UniResult {
            return gson.fromJson(str, UniResult::class.java)
        }
    }

    fun getDate(): Timestamp {
        val format = SimpleDateFormat("EEE MMM dd yyyy", Locale.UK)
        return Timestamp(format.parse(`date-generated`))
    }

    fun getCoins(): List<Coin> = features.map { feature -> Coin(
                feature.properties.id,
                feature.properties.value.toDouble(),
                Coin.Currency.valueOf(feature.properties.currency),
                GeoPoint(feature.geometry.coordinates[0], feature.geometry.coordinates[1]),
                getDate(),
                null
        )
    }
}