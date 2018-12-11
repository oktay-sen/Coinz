package com.oktaysen.coinz.backend

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.oktaysen.coinz.backend.pojo.Coin
import com.oktaysen.coinz.backend.pojo.Rates
import com.oktaysen.coinz.backend.util.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class InventoryInstance(val uni: UniInstance, val auth: FirebaseAuth, val store: FirebaseFirestore) {
    var coinsDepositedToday: Int? = null
    fun listenToWallet(callback: (List<Coin>, List<Coin>, List<Coin>, List<Coin>) -> Unit) {
        if (auth.currentUser == null || auth.currentUser?.uid == null)
            return
        store.collection("coins")
                .whereEqualTo("ownerId", auth.currentUser!!.uid)
                .whereGreaterThan("date", yesterday())
                .whereLessThanOrEqualTo("date", today())
                .addSnapshotListener(ProcessedSnapshotListener(callback))
    }

    fun listenToBankAccount(callback: (List<Coin>, List<Coin>, List<Coin>, List<Coin>) -> Unit) {
        if (auth.currentUser == null || auth.currentUser?.uid == null)
            return
        store.collection("users")
                .document(auth.currentUser!!.uid)
                .collection("account")
                .addSnapshotListener(ProcessedSnapshotListener<Coin>{ current, added, modified, removed ->
                    coinsDepositedToday = current
                            .filter { coin ->
                                val today = today().toDate()
                                val collected = coin.date?.toDate() ?: return@filter false
                                TimeUnit.HOURS.convert(today.time - collected.time, TimeUnit.MILLISECONDS) < 24
                            }
                            .size
                    callback(current, added, modified, removed)
                })
    }

    fun getDepositedTodayCount(): Int? = coinsDepositedToday

    fun depositCoins(coins: List<Coin>, callback: ((Boolean) -> Unit)?) {
        if (coins.isEmpty()) {
            callback?.invoke(true)
            return
        }
        if (coinsDepositedToday == null) {
            Timber.e("The number of deposits user made today is unknown.")
            callback?.invoke(false)
            return
        }
        if (coins.size > (10-(coinsDepositedToday?:10))) {
            Timber.e("User tried to deposit more than daily limit.")
            callback?.invoke(false)
            return
        }

        val walletRef = store.collection("coins")
        val bankRef = store.collection("users")
                .document(auth.currentUser!!.uid)
                .collection("account")
        val batch = store.batch()

        coins.forEach{ coin ->
            batch.delete(walletRef.document(coin.id!!))
            batch.set(bankRef.document(coin.id), coin)
        }
        batch.commit().addOnCompleteListener { result ->
            if (!result.isSuccessful) {
                Timber.v(result.exception)
                callback?.invoke(false)
                return@addOnCompleteListener
            }
            Timber.v("Deposited $coins")
            callback?.invoke(true)
        }
    }

    fun getTodaysRates(callback: (Rates?) -> Unit) {
        getRates(Timestamp.now(), callback)
    }

    fun getRates(date: Date, callback: (Rates?) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.time = date

        //We add +1 to the month because unlike year and day, it is 0-indexed, where January is 0 and December is 11.
        val dateStr = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)+1}-${calendar.get(Calendar.DAY_OF_MONTH)}"

        store.collection("rates")
                .document(dateStr)
                .get()
                .addOnCompleteListener { result ->
                    if (!result.isSuccessful) {
                        Timber.e(result.exception)
                        callback(null)
                        return@addOnCompleteListener
                    }
                    if (result.result!!.exists()) {
                        val rates = result.result!!.toObject(Rates::class.java)
                        Timber.v("Todays rates are: $rates")
                        callback(rates)
                        return@addOnCompleteListener
                    }
                    uni.getDataFromUni(date) {result ->
                        if (result == null) {
                            callback(null)
                            return@getDataFromUni
                        }
                        val rates = result.getDBRates()
                        store.collection("rates")
                                .document(dateStr)
                                .set(rates)
                                .addOnCompleteListener {result ->
                                    if (!result.isSuccessful) {
                                        Timber.e(result.exception)
                                        callback(null)
                                        return@addOnCompleteListener
                                    }
                                    Timber.v("Todays rates are: $rates")
                                    callback(rates)
                                }
                    }
                }
    }

    fun getRates(date: Timestamp, callback: (Rates?) -> Unit) {
        getRates(date.toDate(), callback)
    }
}

private val inventoryInstance:InventoryInstance = InventoryInstance(Uni(), FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

fun Inventory():InventoryInstance {
    return inventoryInstance
}