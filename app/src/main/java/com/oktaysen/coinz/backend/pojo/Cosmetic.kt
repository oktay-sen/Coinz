package com.oktaysen.coinz.backend.pojo

class Cosmetic(val id: String? = null, val title: String?=null, val subtitle: String?=null, val imageUrl: String?=null): Item {
    override fun getType(): Item.Type = Item.Type.COSMETIC
    override fun getID(): String = id!!
}