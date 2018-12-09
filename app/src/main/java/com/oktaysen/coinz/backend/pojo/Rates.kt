package com.oktaysen.coinz.backend.pojo

import com.google.firebase.Timestamp
import java.io.Serializable

class Rates(val dolr: Double?, val shil: Double?, val peny: Double?, val quid: Double?, val date: Timestamp?): Serializable {
    constructor():this(null, null, null, null, null)

    override fun toString(): String {
        return "Rates(DOLR=$dolr, SHIL=$shil, PENY=$peny, QUID=$quid, date=$date)"
    }
}