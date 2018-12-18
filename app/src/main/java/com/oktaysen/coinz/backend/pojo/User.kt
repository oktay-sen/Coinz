package com.oktaysen.coinz.backend.pojo

class User (
        var id: String?,
        val username: String?) {
    constructor(): this(null, null)
}