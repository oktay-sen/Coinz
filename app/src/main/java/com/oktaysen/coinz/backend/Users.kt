package com.oktaysen.coinz.backend

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.oktaysen.coinz.backend.pojo.User
import timber.log.Timber

class UsersInstance(val auth: FirebaseAuth, val store: FirebaseFirestore) {
    fun getUserFromUsername(username: String, callback: (User?) -> Unit) {
        if (auth.currentUser == null) {
            callback(null)
            return
        }
        store.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener {result ->
                    if (!result.isSuccessful) {
                        Timber.e(result.exception)
                        callback(null)
                        return@addOnCompleteListener
                    }
                    if (result.result!!.isEmpty) {
                        Timber.v("User $username not found.")
                        callback(null)
                        return@addOnCompleteListener
                    }
                    val snapshot = result.result!!.documents[0]
                    val user = snapshot.toObject(User::class.java)
                    if (user == null) {
                        Timber.e("Parse to user failed.")
                        callback(null)
                        return@addOnCompleteListener
                    }
                    user.id = snapshot.id
                    callback(user) //TODO: Combine coins and items
                }
    }

    fun getOrCreateCurrentUser(callback: (User?) -> Unit) {
        if (auth.currentUser == null) {
            callback(null)
            return
        }

        val currentUserId = auth.currentUser!!.uid

        store.collection("users")
                .document(currentUserId)
                .get()
                .addOnCompleteListener {result ->
                    if (!result.isSuccessful) {
                        Timber.e(result.exception)
                        callback(null)
                        return@addOnCompleteListener
                    }
                    if (!result.result!!.exists()) {
                        Timber.v("Current user not found in the database. Creating new.")
                        val user = User(currentUserId, makeRandomUsername())
                        result.result!!.reference.set(user)
                                .addOnCompleteListener { result ->
                                    if (!result.isSuccessful) {
                                        Timber.e(result.exception)
                                        callback(null)
                                        return@addOnCompleteListener
                                    }
                                    callback(user)
                                }
                        return@addOnCompleteListener
                    }
                    val user = result.result!!.toObject(User::class.java)
                    if (user == null) {
                        Timber.e("Parse to current user failed.")
                        callback(null)
                        return@addOnCompleteListener
                    }
                    user.id = result.result!!.id
                    callback(user) //TODO: Combine coins and items
                }
    }

    private fun makeRandomUsername(): String {
        val num = Math.floor(Math.random()*100000)
        return "user$num"
    }
}

private val usersInstance:UsersInstance = UsersInstance(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

fun Users():UsersInstance {
    return usersInstance
}