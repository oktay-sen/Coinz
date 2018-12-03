package com.oktaysen.coinz.backend.pojo

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

class Coin(val id: String?, val value: Double?, val currency: Currency?, val position: GeoPoint?, val date: Timestamp?) {
    //Empty constructor for JSON parsing.
    constructor() : this(null, null, null, null, null)

    enum class Currency { DOLR, SHIL, PENY, QUID }

    override fun toString(): String {
        return "{Coin: $value $currency}"
    }
}