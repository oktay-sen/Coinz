package com.oktaysen.coinz.backend

import com.google.firebase.auth.FirebaseAuth

class AuthInstance(private val auth: FirebaseAuth) {
    fun isLoggedIn():Boolean {
        return auth.currentUser != null
    }

    fun logOut() {
        auth.signOut()
    }
}

val instance:AuthInstance = AuthInstance(FirebaseAuth.getInstance())

fun Auth():AuthInstance {
    return instance
}