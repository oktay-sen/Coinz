package com.oktaysen.coinz.backend

import android.os.AsyncTask
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import com.oktaysen.coinz.backend.pojo.Coin
import com.oktaysen.coinz.backend.pojo.UniversityMapResult
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.*

class MapInstance(val auth: FirebaseAuth, val store: FirebaseFirestore, val http: OkHttpClient, val gson: Gson) {
    inner class GetRequest(private val callback: (String?) -> Unit) : AsyncTask<String, Unit, String>() {
        override fun doInBackground(vararg params: String?): String? {
            if (params.isEmpty() || params[0] == null) return null;
            val request = Request.Builder().url(params[0]!!).build()
            val response = http.newCall(request).execute()
            if (!response.isSuccessful) {
                Timber.e("Get Request failed: (${response.code()}) ${response.body().toString()}")
                return null
            }
            return response.body()?.string()
        }

        override fun onPostExecute(result: String?) {
            callback(result)
        }
    }

    fun getMapFromUni(year: Int, month:Int, day: Int, callback: (UniversityMapResult?) -> Unit) {
        Timber.v("Getting the map from university.")

        val monthStr = if (month < 10) "0$month" else "$month"
        val dayStr = if (day < 10) "0$day" else "$day"
        val url = "http://homepages.inf.ed.ac.uk/stg/coinz/$year/$monthStr/$dayStr/coinzmap.geojson"
        Timber.v("University map URL: $url")

        GetRequest{result ->
            if (result == null) {
                Timber.v("Getting university map unsuccessful.")
                callback(null)
                return@GetRequest
            }
            Timber.v("Getting university map successful.")
            callback(UniversityMapResult.fromString(gson, result))
        }.execute(url)
    }

    fun getMapFromUni(date: Date, callback: (UniversityMapResult?) -> Unit) {
        val cal = Calendar.getInstance()
        cal.time = date
        //We add +1 to the month because unlike year and day, it is 0-indexed, where January is 0 and December is 11.
        getMapFromUni(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), callback)
    }

    fun getMap(callback: (List<Coin>?) -> Unit) {
        val cal = Calendar.getInstance()
        val today = Timestamp.now()
        cal.time = today.toDate()
        cal.add(Calendar.DAY_OF_MONTH, -1)
        val yesterday = Timestamp(cal.time)
        Timber.v("Getting map started.")
        store
                .collection("coins")
                .whereGreaterThan("date", yesterday)
                .whereLessThanOrEqualTo("date", today)
                .get()
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        callback(null)
                        Timber.e(task.exception)
                        return@addOnCompleteListener
                    }
                    val result = task.result!!
                    Timber.v("Coins in Firestore: ${result.size()}")
                    if (result.size() > 0) {
                        callback(result.toObjects(Coin::class.java))
                    } else {
                        getMapFromUni(today.toDate()) { uniMap ->
                            if (uniMap == null) {
                                callback(null)
                                return@getMapFromUni
                            }
                            val newCoins = uniMap.getCoins()
                            val batch = store.batch()
                            val ref = store.collection("coins")
                            for (coin in newCoins) {
                                batch.set(ref.document(coin.id!!), coin)
                            }
                            batch
                                    .commit()
                                    .addOnCompleteListener { task ->
                                        if (!task.isSuccessful) {
                                            callback(null)
                                            return@addOnCompleteListener
                                        }
                                        callback(newCoins)
                                    }
                        }
                    }
                }
    }
}

val instance:MapInstance = MapInstance(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance(), OkHttpClient(), Gson())

fun Map():MapInstance {
    return instance
}