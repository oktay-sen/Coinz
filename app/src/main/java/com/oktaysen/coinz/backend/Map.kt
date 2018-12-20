package com.oktaysen.coinz.backend

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.oktaysen.coinz.backend.pojo.Coin
import timber.log.Timber
import com.oktaysen.coinz.backend.util.*

class MapInstance(val uni: UniInstance, val auth: FirebaseAuth, val store: FirebaseFirestore, val registry: ListenerRegistryInstance) {
    fun addTodaysCoinsIfMissing() {
        store
                .collection("coins")
                .whereGreaterThan("date", yesterday())
                .whereLessThanOrEqualTo("date", today())
                .get()
                .addOnCompleteListener { result ->
                    if (!result.isSuccessful) {
                        Timber.e(result.exception)
                        return@addOnCompleteListener
                    }
                    if (result.result!!.isEmpty) {
                        uni.getDataFromUni(Timestamp.now().toDate()) { uniMap ->
                            if (uniMap == null) return@getDataFromUni
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

    fun listenToMap(callback: (List<Coin>, List<Coin>, List<Coin>, List<Coin>) -> Unit): Int {
        Timber.v("Getting map started.")
        return registry.register(
            store
                .collection("coins")
                .whereGreaterThan("date", yesterday())
                .whereLessThanOrEqualTo("date", today())
                .whereEqualTo("ownerId", null)
                .addSnapshotListener(ProcessedSnapshotListener<Coin> { current, added, modified, removed ->
                    Timber.v("Coins in Firestore: ${current.size}")
                    callback(current, added, modified, removed)
                    if (current.isEmpty()) addTodaysCoinsIfMissing()
                })
        )
    }

    fun collectCoin(id: String, callback: ((success: Boolean) -> Unit)?) {
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

private val mapInstance:MapInstance = MapInstance(Uni(), FirebaseAuth.getInstance(), FirebaseFirestore.getInstance(), ListenerRegistry())

fun Map():MapInstance {
    return mapInstance
}