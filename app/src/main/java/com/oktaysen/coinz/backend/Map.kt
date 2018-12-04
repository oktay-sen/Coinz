package com.oktaysen.coinz.backend

import android.os.AsyncTask
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
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

    val today = Timestamp.now()

    val yesterday = {
        val cal = Calendar.getInstance()
        cal.time = today.toDate()
        cal.add(Calendar.DAY_OF_MONTH, -1)
        Timestamp(cal.time)
    }()

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

    fun addTodaysCoinsIfMissing() {
        store
                .collection("coins")
                .whereGreaterThan("date", yesterday)
                .whereLessThanOrEqualTo("date", today)
                .get()
                .addOnCompleteListener { result ->
                    if (!result.isSuccessful) {
                        Timber.e(result.exception)
                        return@addOnCompleteListener
                    }
                    if (result.result!!.isEmpty) {
                        getMapFromUni(Timestamp.now().toDate()) { uniMap ->
                            if (uniMap == null) return@getMapFromUni
                            val newCoins = uniMap.getCoins()
                            val batch = store.batch()
                            val ref = store.collection("coins")
                            for (coin in newCoins) {
                                batch.set(ref.document(coin.id!!), coin)
                            }
                            batch
                                    .commit()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful)
                                            Timber.v("Added ${newCoins.size} coins to Firestore.")
                                        else
                                            Timber.e(task.exception)
                                    }
                        }
                    }
                }

    }

    fun listenToMap(callback: (List<Coin>, List<Coin>, List<Coin>, List<Coin>) -> Unit) {
        Timber.v("Getting map started.")
        store
                .collection("coins")
                .whereGreaterThan("date", yesterday)
                .whereLessThanOrEqualTo("date", today)
                .whereEqualTo("ownerId", null)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null || snapshot == null) {
                        Timber.e(exception)
                        return@addSnapshotListener
                    }
                    val currentMap = snapshot.toObjects(Coin::class.java)
                    Timber.v("Coins in Firestore: ${currentMap.size}")
                    val addedCoins: MutableList<Coin> = mutableListOf()
                    val modifiedCoins: MutableList<Coin> = mutableListOf()
                    val removedCoins: MutableList<Coin> = mutableListOf()
                    snapshot.documentChanges.map {change ->
                        when (change.type) {
                            DocumentChange.Type.ADDED -> addedCoins.add(change.document.toObject(Coin::class.java))
                            DocumentChange.Type.MODIFIED -> modifiedCoins.add(change.document.toObject(Coin::class.java))
                            DocumentChange.Type.REMOVED -> removedCoins.add(change.document.toObject(Coin::class.java))
                        }
                    }
                    callback(currentMap, addedCoins, modifiedCoins, removedCoins)
                    if (currentMap.size == 0) {
                        addTodaysCoinsIfMissing()
                    }
                }
    }

    fun collectCoin(id: String, callback: ((Boolean) -> Unit)?) {
        if (auth.currentUser == null) {
            callback?.invoke(false)
            return
        }
        store.collection("coins").document(id).get()
                .addOnCompleteListener {  result ->
                    if (!result.isSuccessful || !result.result!!.exists() || auth.currentUser == null) {
                        callback?.invoke(false)
                        return@addOnCompleteListener
                    }
                    val coin = result.result!!.toObject(Coin::class.java)
                    if (coin == null || coin.ownerId != null) {
                        callback?.invoke(false)
                        return@addOnCompleteListener
                    }
                    result.result!!.reference.update("ownerId", auth.currentUser!!.uid)
                            .addOnCompleteListener { result ->
                                if (result.isSuccessful)
                                    callback?.invoke(true)
                                else
                                    callback?.invoke(false)
                            }
                }
    }

    fun collectCoin(coin: Coin, callback: ((Boolean) -> Unit)?) {
        collectCoin(coin.id!!, callback)
    }
}

val instance:MapInstance = MapInstance(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance(), OkHttpClient(), Gson())

fun Map():MapInstance {
    return instance
}