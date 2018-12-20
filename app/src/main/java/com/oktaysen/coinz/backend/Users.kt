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
                    callback(user)
                }
    }

    fun getOrCreateCurrentUser(callback: (user: User?, isNewUser: Boolean) -> Unit) {
        if (auth.currentUser == null) {
            callback(null, false)
            return
        }

        val currentUserId = auth.currentUser!!.uid

        store.collection("users")
                .document(currentUserId)
                .get()
                .addOnCompleteListener {result ->
                    if (!result.isSuccessful) {
                        Timber.e(result.exception)
                        callback(null, false)
                        return@addOnCompleteListener
                    }
                    if (!result.result!!.exists()) {
                        Timber.v("Current user not found in the database. Creating new.")
                        val user = User(currentUserId, makeRandomUsername())
                        result.result!!.reference.set(user)
                                .addOnCompleteListener { result ->
                                    if (!result.isSuccessful) {
                                        Timber.e(result.exception)
                                        callback(null, false)
                                        return@addOnCompleteListener
                                    }
                                    callback(user, true)
                                }
                        return@addOnCompleteListener
                    }
                    val user = result.result!!.toObject(User::class.java)
                    if (user == null) {
                        Timber.e("Parse to current user failed.")
                        callback(null, false)
                        return@addOnCompleteListener
                    }
                    user.id = result.result!!.id
                    callback(user, false)
                }
    }

    fun updateUsername(username: String, callback: ((success: Boolean, errorMessage: String?) -> Unit)?) {
        if (auth.currentUser == null) {
            callback?.invoke(false, "User isn't logged in.")
            return
        }

        store.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener {result ->
                    if (!result.isSuccessful) {
                        Timber.e(result.exception)
                        callback?.invoke(false, result.exception?.message ?: "An error occured.")
                        return@addOnCompleteListener
                    }
                    if (!result.result!!.isEmpty) {
                        Timber.e("User $username already exists.")
                        callback?.invoke(false, "\"$username\" is taken.")
                        return@addOnCompleteListener
                    }
                    store.collection("users")
                            .document(auth.currentUser!!.uid)
                            .update("username", username)
                            .addOnCompleteListener { result ->
                                if (!result.isSuccessful) {
                                    Timber.e(result.exception)
                                    callback?.invoke(false, result.exception?.message ?: "An error occured.")
                                    return@addOnCompleteListener
                                }
                                Timber.v("Updated username to $username")
                                callback?.invoke(true, null)
                            }
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