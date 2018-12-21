package com.oktaysen.coinz.backend

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.oktaysen.coinz.backend.pojo.Coin
import com.oktaysen.coinz.backend.pojo.Trade
import com.oktaysen.coinz.backend.pojo.User
import kotlinx.android.synthetic.main.fragment_trading_trade_pending.view.*
import timber.log.Timber

class TradingInstance(val users: UsersInstance, val auth: FirebaseAuth, val store: FirebaseFirestore) {
    fun getUserInventory(userId: String, callback: (List<Coin>?) -> Unit) {
        if (auth.currentUser == null) {
            callback(null)
            return
        }
        Timber.v("Getting inventory of $userId")
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
                    Timber.v("Inventory of $userId is: $items")
                    callback(items)
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

    fun newTradeWithUser(userId: String, fromItems: List<Coin>, toItems: List<Coin>, callback: ((success: Boolean) -> Unit)?) {
        if (auth.currentUser == null) {
            callback?.invoke(false)
            return
        }
        val currentUserId = auth.currentUser!!.uid
        if (userId == currentUserId) {
            callback?.invoke(false)
            return
        }

        users.getOrCreateCurrentUser { currentUser, _ ->
            if (currentUser == null) {
                callback?.invoke(false)
                return@getOrCreateCurrentUser
            }
            store.collection("users")
                    .document(userId)
                    .get()
                    .addOnCompleteListener { result ->
                        if (!result.isSuccessful) {
                            Timber.e(result.exception)
                            callback?.invoke(false)
                            return@addOnCompleteListener
                        }
                        val toUser = result.result!!.toObject(User::class.java)
                        if (toUser == null) {
                            Timber.e("Target user $userId isn't a valid user.")
                            callback?.invoke(false)
                            return@addOnCompleteListener
                        }
                        getUserInventory(userId) { toInventory ->
                            if (toInventory == null) {
                                callback?.invoke(false)
                                return@getUserInventory
                            }

                            if (!toItems.all { toInventory.contains(it) }) {
                                Timber.e("Target inventory doesn't contain all items in the trade.")
                                callback?.invoke(false)
                                return@getUserInventory
                            }

                            getCurrentUserInventory { fromInventory ->
                                if (fromInventory == null) {
                                    callback?.invoke(false)
                                    return@getCurrentUserInventory
                                }

                                if (!fromItems.all { fromInventory.contains(it) }) {
                                    Timber.e("Current user's inventory doesn't contain all items in the trade.")
                                    callback?.invoke(false)
                                    return@getCurrentUserInventory
                                }

                                val tradeRef = store.collection("trades").document()
                                val trade = Trade(
                                        tradeRef.id,
                                        auth.currentUser!!.uid,
                                        userId,
                                        currentUser.username,
                                        toUser.username,
                                        Timestamp.now(),
                                        Trade.State.PENDING
                                )
                                val batch = store.batch()

                                batch.set(tradeRef, trade)
                                fromItems.forEach {fromCoin ->
                                    batch.set(tradeRef.collection("fromItems").document(fromCoin.id!!), fromCoin)
                                    batch.delete(
                                            store
                                                    .collection("users")
                                                    .document(currentUserId)
                                                    .collection("account")
                                                    .document(fromCoin.id))
                                }
                                toItems.forEach {toCoin ->
                                    batch.set(tradeRef.collection("toItems").document(toCoin.id!!), toCoin)
                                    batch.delete(
                                            store
                                                    .collection("users")
                                                    .document(userId)
                                                    .collection("account")
                                                    .document(toCoin.id))
                                }

                                batch.commit()
                                        .addOnCompleteListener { result ->
                                            if (!result.isSuccessful) {
                                                Timber.e(result.exception)
                                                callback?.invoke(false)
                                                return@addOnCompleteListener
                                            }
                                            Timber.v("Trade request sent!")
                                            callback?.invoke(true)
                                        }
                            }
                        }
                    }
        }
    }

    fun getTrades(callback: (usernames: List<String>?, tradesByUsername: Map<String, List<Trade>>?) -> Unit) {
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

    fun getTrade(tradeId: String, callback: (trade: Trade?, fromItems: List<Coin>, toItems: List<Coin>) -> Unit) {
        if (auth.currentUser == null) {
            callback(null, listOf(), listOf())
            return
        }

        store.collection("trades")
                .document(tradeId)
                .get()
                .addOnCompleteListener { result ->
                    if (!result.isSuccessful) {
                        Timber.e(result.exception)
                        callback(null, listOf(), listOf())
                        return@addOnCompleteListener
                    }
                    if (!result.result!!.exists()) {
                        Timber.e("Trade $tradeId doesn't exist.")
                        callback(null, listOf(), listOf())
                        return@addOnCompleteListener
                    }
                    val trade = result.result!!.toObject(Trade::class.java)
                    if (trade == null) {
                        Timber.e("Trade $tradeId is not a proper trade.")
                        callback(null, listOf(), listOf())
                        return@addOnCompleteListener
                    }
                    store.collection("trades")
                            .document(tradeId)
                            .collection("fromItems")
                            .get()
                            .addOnCompleteListener { result ->
                                if (!result.isSuccessful) {
                                    Timber.e(result.exception)
                                    callback(null, listOf(), listOf())
                                    return@addOnCompleteListener
                                }
                                val fromItems: List<Coin> = result.result!!.toObjects(Coin::class.java)
                                store.collection("trades")
                                        .document(tradeId)
                                        .collection("toItems")
                                        .get()
                                        .addOnCompleteListener { result ->
                                            if (!result.isSuccessful) {
                                                Timber.e(result.exception)
                                                callback(null, listOf(), listOf())
                                                return@addOnCompleteListener
                                            }
                                            val toItems: List<Coin> = result.result!!.toObjects(Coin::class.java)
                                            callback(trade, fromItems, toItems)
                                        }
                            }
                }
    }

    fun acceptTrade(tradeId: String, callback: ((success: Boolean) -> Unit)?) {
        if (auth.currentUser == null) {
            callback?.invoke(false)
            return
        }
        val currentUserId = auth.currentUser!!.uid

        getTrade(tradeId) { trade, fromItems, toItems ->
            if (trade == null) {
                callback?.invoke(false)
                return@getTrade
            }
            if (trade.toId != currentUserId) {
                Timber.e("Trade $tradeId isn't made to the current user.")
                callback?.invoke(false)
                return@getTrade
            }
            if (trade.state != Trade.State.PENDING) {
                Timber.e("Trade $tradeId is not pending.")
                callback?.invoke(false)
                return@getTrade
            }
            val batch = store.batch()
            val tradeRef = store.collection("trades").document(tradeId)
            batch.update(tradeRef, "state", Trade.State.ACCEPTED.toString())
            fromItems.forEach {coin ->
                val ref = store.collection("users")
                        .document(currentUserId)
                        .collection("account")
                        .document(coin.id!!)
                batch.set(ref, coin)
                batch.delete(tradeRef.collection("fromItems").document(coin.id))
            }
            toItems.forEach {coin ->
                val ref = store.collection("users")
                        .document(trade.fromId!!)
                        .collection("account")
                        .document(coin.id!!)
                batch.set(ref, coin)
                batch.delete(tradeRef.collection("toItems").document(coin.id))
            }
            batch.commit()
                    .addOnCompleteListener { result ->
                        if (!result.isSuccessful) {
                            Timber.e(result.exception)
                            callback?.invoke(false)
                            return@addOnCompleteListener
                        }
                        Timber.v("Trade $tradeId completed.")
                        callback?.invoke(true)
                    }
        }
    }

    fun rejectTrade(tradeId: String, callback: ((success: Boolean) -> Unit)?) {
        if (auth.currentUser == null) {
            callback?.invoke(false)
            return
        }
        val currentUserId = auth.currentUser!!.uid

        getTrade(tradeId) { trade, fromItems, toItems ->
            if (trade == null) {
                callback?.invoke(false)
                return@getTrade
            }
            if (trade.toId != currentUserId && trade.fromId != currentUserId) {
                Timber.e("Trade $tradeId doesn't involve the current user.")
                callback?.invoke(false)
                return@getTrade
            }
            if (trade.state != Trade.State.PENDING) {
                Timber.e("Trade $tradeId is not pending.")
                callback?.invoke(false)
                return@getTrade
            }
            val batch = store.batch()
            val tradeRef = store.collection("trades").document(tradeId)
            batch.update(tradeRef, "state", Trade.State.CANCELED.toString())
            fromItems.forEach {coin ->
                val ref = store.collection("users")
                        .document(trade.fromId!!)
                        .collection("account")
                        .document(coin.id!!)
                batch.set(ref, coin)
                batch.delete(tradeRef.collection("fromItems").document(coin.id))
            }
            toItems.forEach {coin ->
                val ref = store.collection("users")
                        .document(trade.toId!!)
                        .collection("account")
                        .document(coin.id!!)
                batch.set(ref, coin)
                batch.delete(tradeRef.collection("toItems").document(coin.id))
            }
            batch.commit()
                    .addOnCompleteListener { result ->
                        if (!result.isSuccessful) {
                            Timber.e(result.exception)
                            callback?.invoke(false)
                            return@addOnCompleteListener
                        }
                        Timber.v("Trade $tradeId completed.")
                        callback?.invoke(true)
                    }
        }
    }
}

private val tradingInstance:TradingInstance = TradingInstance(Users(), FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

fun Trading():TradingInstance {
    return tradingInstance
}