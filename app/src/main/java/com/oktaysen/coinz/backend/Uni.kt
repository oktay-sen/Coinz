package com.oktaysen.coinz.backend

import com.google.gson.Gson
import com.oktaysen.coinz.backend.pojo.UniResult
import com.oktaysen.coinz.backend.util.GetRequest
import okhttp3.OkHttpClient
import timber.log.Timber
import java.util.*

class UniInstance(val http: OkHttpClient, val gson: Gson){
    fun getDataFromUni(year: Int, month:Int, day: Int, callback: (UniResult?) -> Unit) {
        Timber.v("Getting the data from university.")

        val monthStr = if (month < 10) "0$month" else "$month"
        val dayStr = if (day < 10) "0$day" else "$day"
        val url = "http://homepages.inf.ed.ac.uk/stg/coinz/$year/$monthStr/$dayStr/coinzmap.geojson"
        Timber.v("University data URL: $url")

        GetRequest(http) { result ->
            if (result == null) {
                Timber.v("Getting university data unsuccessful.")
                callback(null)
                return@GetRequest
            }
            Timber.v("Getting university data successful.")
            callback(UniResult.fromString(gson, result))
        }.execute(url)
    }

    fun getDataFromUni(date: Date, callback: (UniResult?) -> Unit) {
        val cal = Calendar.getInstance()
        cal.time = date
        //We add +1 to the month because unlike year and day, it is 0-indexed, where January is 0 and December is 11.
        getDataFromUni(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), callback)
    }
}

private val uniInstance:UniInstance = UniInstance(OkHttpClient(), Gson())

fun Uni():UniInstance {
    return uniInstance
}