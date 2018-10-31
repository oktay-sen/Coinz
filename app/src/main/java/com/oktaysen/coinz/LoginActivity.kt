package com.oktaysen.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*
import android.support.v4.content.ContextCompat
import android.view.WindowManager
import android.widget.Toast
import com.oktaysen.coinz.backend.Auth


class LoginActivity : AppCompatActivity() {
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

        login_email.setOnClickListener { this.onEmailClick() }
        login_google.setOnClickListener { this.onGoogleClick() }
    }

    private fun onEmailClick() {

    }

    private fun onGoogleClick() {
        Auth().attemptLoginWithGoogle(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Auth().onGoogleLoginActivityResult(this, requestCode, resultCode, data) { success ->
            if (success) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Google Login failed.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
