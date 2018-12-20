package com.oktaysen.coinz.layout.login

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.oktaysen.coinz.R
import kotlinx.android.synthetic.main.fragment_login_login.*

class LoginFragment: Fragment() {
    var onLoginListener:((String, String) -> Unit)? = null
    var onBackListener:(() -> Unit)? = null
    var onRegisterListener:(() -> Unit)? = null
    var onForgotListener:(() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        back_button.setOnClickListener { onBackListener?.invoke() }
        register_button.setOnClickListener { onRegisterListener?.invoke() }
        login_forgot.setOnClickListener { onForgotListener?.invoke() }

        login_input_email.setOnEditorActionListener { _, _, _ ->
            login_input_email.error = null
            return@setOnEditorActionListener false
        }

        login_input_password.setOnEditorActionListener { _, _, _ ->
            login_input_password.error = null
            return@setOnEditorActionListener false
        }

        login_button.setOnClickListener {
            var canLogin = true
            if (login_input_email.text.length <=3 || !login_input_email.text.contains("@")) {
                canLogin = false
                login_input_email.error = "Enter a valid email"
            } else {
                login_input_email.error = null
            }
            if (login_input_password.text.isEmpty()) {
                canLogin = false
                login_input_password.error = "Enter a password"
            } else {
                login_input_password.error = null
            }
            if (canLogin)
                onLoginListener?.invoke(login_input_email.text.toString(), login_input_password.text.toString())
        }
    }
}