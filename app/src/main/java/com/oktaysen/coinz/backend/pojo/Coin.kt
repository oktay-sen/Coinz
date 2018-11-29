package com.oktaysen.coinz.backend.pojo

import com.google.firebase.firestore.GeoPoint
import java.util.*

class Coin(val id: String, val value: Double, val currency: Currency, val position: GeoPoint, val date: Date?) {
    enum class Currency { DOLR, SHIL, PENY, QUID }

    override fun toString(): String {
        return "{Coin: $value $currency}"
    }
}