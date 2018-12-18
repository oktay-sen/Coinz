package com.oktaysen.coinz.backend

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.oktaysen.coinz.backend.pojo.Coin
import com.oktaysen.coinz.backend.pojo.Trade
import com.oktaysen.coinz.backend.pojo.User
import timber.log.Timber

class TradingInstance(val users: UsersInstance, val auth: FirebaseAuth, val store: FirebaseFirestore) {
    fun getUserInventory(userId: String, callback: (List<Coin>?) -> Unit) {
        if (auth.currentUser == null) {
            callback(null)
            return
        }
        store.collection("users")
                .document(userId)
                .collection("account")
                .get()
                .addOnCompleteListener {result ->
                    if (!result.isSuccessful) {
                        Timber.e(result.exception)
                        callback(null)
                        return@addOnCompleteListener
                    }
                    val items = result.result!!.toObjects(Coin::class.java)
                    callback(items) //TODO: Combine coins and items
                }
    }

    fun getCurrentUserInventory(callback: (List<Coin>?) -> Unit) {
        if (auth.currentUser == null) {
            callback(null)
            return
        }
        getUserInventory(auth.currentUser!!.uid, callback)
    }

    fun getUserInventory(user: User, callback: (List<Coin>?) -> Unit) {
        if (user.id == null) {
            callback(null)
            return
        }
        getUserInventory(user.id!!, callback)
    }

    fun newTradeWithUser(userId: String, fromItems: List<Coin>, toItems: List<Coin>, callback: ((Boolean) -> Unit)?) {
        if (auth.currentUser == null) {
            callback?.invoke(false)
            return
        }
        val currentUserId = auth.currentUser!!.uid

        users.getOrCreateCurrentUser { currentUser ->
            if (currentUser == null) {
                callback?.invoke(false)
                return@getOrCreateCurrentUser
            }
            store.runTransaction { transaction ->
                val toUserSnapshot = transaction.get(store.collection("users").document(userId))
                val toUser = toUserSnapshot.toObject(User::class.java)
                if (!toUserSnapshot.exists() || toUser == null) {
                    Timber.e("Target user with id $userId not found.")
                    callback?.invoke(false)
                    return@runTransaction
                }
                val fromInventoryRef = store.collection("users").document(currentUserId).collection("account")
                val toInventoryRef = store.collection("users").document(userId).collection("account")
                val fromItemsSnapshots = fromItems.map { transaction.get(fromInventoryRef.document(it.id!!)) }
                val fromItemsReal = fromItemsSnapshots.map { it.toObject(Coin::class.java) } //TODO: Also implement cosmetic
                val fromLegit = fromItemsSnapshots.all { it.exists() } && !fromItemsReal.contains(null)
                val toLegit = toItems.map { transaction.get(toInventoryRef.document(it.id!!)) }.all { it.exists() }
                if (!fromLegit || !toLegit) {
                    Timber.e("Some trade items were invalid. From: $fromLegit, To: $toLegit")
                    callback?.invoke(false)
                    return@runTransaction
                }

                // Ready to trade.
                val tradeRef = store.collection("trades").document()
                val trade = Trade(
                        tradeRef.id,
                        auth.currentUser!!.uid,
                        userId,
                        currentUser.username,
                        toUser.username,
                        fromItems.map { it.id!! },
                        toItems.map { it.id!! },
                        Timestamp.now(),
                        Trade.State.PENDING
                )
                transaction.set(tradeRef, trade)
                fromItemsReal.map { transaction.set(tradeRef.collection("fromItems").document(it!!.id!!), it) }
                fromItems.map { transaction.delete(fromInventoryRef.document(it.id!!)) }
                callback?.invoke(true)
            }.addOnFailureListener { e ->
                Timber.e(e)
                callback?.invoke(false)
            }
        }
    }

    fun getTrades(callback: (List<String>?, Map<String, List<Trade>>?) -> Unit) {
        if (auth.currentUser == null) {
            callback(null, null)
            return
        }
        val currentUserId = auth.currentUser!!.uid
        store.collection("trades")
                .whereEqualTo("fromId", currentUserId)
                .orderBy("date")
                .get()
                .addOnCompleteListener { result ->
                    if (!result.isSuccessful) {
                        Timber.e(result.exception)
                        callback(null, null)
                        return@addOnCompleteListener
                    }
                    val fromTrades = result.result!!.toObjects(Trade::class.java)
                    store.collection("trades")
                            .whereEqualTo("toId", currentUserId)
                            .orderBy("date")
                            .get()
                            .addOnCompleteListener { result ->
                                if (!result.isSuccessful) {
                                    Timber.e(result.exception)
                                    callback(null, null)
                                    return@addOnCompleteListener
                                }
                                val toTrades = result.result!!.toObjects(Trade::class.java)
                                val allTrades: List<Trade> = listOf(fromTrades, toTrades)
                                        .flatten()
                                        .sortedBy { it.date?.seconds }
                                val contacts: MutableList<String> = mutableListOf()
                                val trades: MutableMap<String, MutableList<Trade>> = mutableMapOf()
                                allTrades.forEach { trade ->
                                    val username =
                                            if (trade.fromId == currentUserId) trade.toUsername!!
                                            else trade.fromUsername!!
                                    if (!contacts.contains(username)) {
                                        contacts.add(username)
                                        trades.put(username, mutableListOf())
                                    }
                                    trades[username]!!.add(trade)
                                }
                                callback(contacts, trades)
                            }
                }
    }
}

private val tradingInstance:TradingInstance = TradingInstance(Users(), FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

fun Trading():TradingInstance {
    return tradingInstance
}