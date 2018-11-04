package com.oktaysen.coinz.layout.login

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import kotlinx.android.synthetic.main.activity_login.*
import android.support.v4.content.ContextCompat
import android.view.WindowManager
import android.widget.Toast
import com.oktaysen.coinz.layout.main.MainActivity
import com.oktaysen.coinz.R
import com.oktaysen.coinz.backend.Auth
import android.support.v7.app.AlertDialog




class LoginActivity : AppCompatActivity() {
    enum class LoginState { CHOICE, EMAIL_LOGIN, EMAIL_REGISTER, EMAIL_FORGOT }
    private var loginState = LoginState.CHOICE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.login_background)
        // TODO: When an image/video is the background of LoginActivity, remove the above and uncomment the following:
        //window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    override fun onStart() {
        super.onStart()
        setLoginState(loginState)
        Auth().addAuthStateListener(::onAuthStateChange)
    }

    override fun onStop() {
        super.onStop()
        Auth().removeAuthStateListener(::onAuthStateChange)
    }

    private fun setLoginState(newState: LoginState) {
        val fragment: Fragment = when (newState) {
            LoginState.CHOICE -> {
                val fragment = ChoiceFragment()
                fragment.onEmailClickListener = { setLoginState(LoginState.EMAIL_LOGIN) }
                fragment.onGoogleClickListener = { Auth().attemptLoginWithGoogle(this) }
                fragment
            }
            LoginState.EMAIL_LOGIN -> {
                val fragment = LoginFragment()
                fragment.onBackListener = ::onBackPressed
                fragment.onRegisterListener = { setLoginState(LoginState.EMAIL_REGISTER) }
                fragment.onForgotListener = { setLoginState(LoginState.EMAIL_FORGOT) }
                fragment.onLoginListener =  { email, password ->
                    Auth().loginWithEmailPassword(email, password) { success, error ->
                        if (!success) Toast.makeText(this, "Email or password is wrong", Toast.LENGTH_LONG).show()
                    }
                }
                fragment
            }
            LoginState.EMAIL_REGISTER -> {
                val fragment = RegisterFragment()
                fragment.onBackListener = ::onBackPressed
                fragment.onRegisterListener =  { email, password ->
                    Auth().registerWithEmailPassword(email, password) { success, error ->
                        if (!success) Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                    }
                }
                fragment
            }
            LoginState.EMAIL_FORGOT -> {
                val fragment = ForgotFragment()
                fragment.onBackListener = ::onBackPressed
                fragment.onResetListener =  { email ->
                    Auth().sendPasswordResetEmail(email) { success, error ->
                        if (!success) Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                        else {
                            val alertDialog = AlertDialog.Builder(this).create()
                            alertDialog.setTitle("Password Reset")
                            alertDialog.setMessage("Follow the instructions we sent to $email to reset your password.")
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ -> dialog.dismiss() }
                            alertDialog.show()
                        }
                    }
                }
                fragment
            }
        }

        if (login_wrapper.childCount == 0) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.login_wrapper, fragment)
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out) //TODO: Replace with slide animations
                    .commit()
        }
        else {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.login_wrapper, fragment)
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out) //TODO: Replace with slide animations
                    .commit()
        }
        loginState = newState
    }

    override fun onBackPressed() {
        when (loginState) {
            LoginState.CHOICE -> super.onBackPressed()
            LoginState.EMAIL_LOGIN -> setLoginState(LoginState.CHOICE)
            LoginState.EMAIL_REGISTER -> setLoginState(LoginState.EMAIL_LOGIN)
            LoginState.EMAIL_FORGOT -> setLoginState(LoginState.EMAIL_LOGIN)
        }
    }

    private fun onAuthStateChange(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Auth().onGoogleLoginActivityResult(this, requestCode, resultCode, data) { success, error ->
            if (!success) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }
}
