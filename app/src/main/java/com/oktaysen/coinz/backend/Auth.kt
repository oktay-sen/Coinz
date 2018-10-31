package com.oktaysen.coinz.backend

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.oktaysen.coinz.R

class AuthInstance(private val auth: FirebaseAuth) {
    companion object {
        const val GOOGLE_SIGN_IN: Int = 19475
    }

    fun isLoggedIn():Boolean {
        return auth.currentUser != null
    }

    fun logOut() {
        auth.signOut()
    }

    //TODO: Write tests for this.
    fun attemptLoginWithGoogle(activity:Activity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.google_web_client_id))
                .requestEmail()
                .build()
        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        activity.startActivityForResult(googleSignInClient.signInIntent, GOOGLE_SIGN_IN)
    }

    //TODO: Write tests for this.
    fun onGoogleLoginActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?, onComplete:((Boolean) -> Unit)?) {
        if (requestCode == GOOGLE_SIGN_IN) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                auth.signInWithCredential(credential)
                        .addOnCompleteListener(activity) { task -> onComplete?.invoke(task.isSuccessful) }
                        .addOnCanceledListener(activity) { onComplete?.invoke(false) }
            } catch (e: ApiException) { onComplete?.invoke(false) }
        }
    }
}

val instance:AuthInstance = AuthInstance(FirebaseAuth.getInstance())

fun Auth():AuthInstance {
    return instance
}