package com.oktaysen.coinz.backend.pojo

interface Item {
    enum class Type { COIN, COSMETIC }

    fun getType():Type
    fun getID():String
}