package com.oktaysen.coinz.backend.pojo

import com.google.firebase.Timestamp
import java.io.Serializable

class Trade(
        var tradeId: String?,
        val fromId: String?,
        val toId: String?,
        val fromUsername: String?,
        val toUsername: String?,
        val date: Timestamp?,
        val state: State): Serializable {
    constructor(): this(null, null, null, null, null, null, State.PENDING)
    enum class State { PENDING, ACCEPTED, CANCELED }
}