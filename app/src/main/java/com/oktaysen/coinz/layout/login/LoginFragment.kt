package com.oktaysen.coinz.layout.login

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.oktaysen.coinz.R

class LoginFragment: Fragment() {
    var onLoginListener:((String, String) -> Unit)? = null
    var onBackListener:(() -> Unit)? = null
    var onRegisterListener:(() -> Unit)? = null
    var onForgotListener:(() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login_login, container, false)
        view.findViewById<Button>(R.id.back_button).setOnClickListener { onBackListener?.invoke() }
        view.findViewById<Button>(R.id.register_button).setOnClickListener { onRegisterListener?.invoke() }
        view.findViewById<Button>(R.id.login_forgot).setOnClickListener { onForgotListener?.invoke() }

        val emailInput = view.findViewById<EditText>(R.id.login_input_email)
        emailInput.setOnEditorActionListener { _, _, _ ->
            emailInput.error = null
            return@setOnEditorActionListener false
        }

        val passwordInput = view.findViewById<EditText>(R.id.login_input_password)
        passwordInput.setOnEditorActionListener { _, _, _ ->
            passwordInput.error = null
            return@setOnEditorActionListener false
        }

        view.findViewById<Button>(R.id.login_button).setOnClickListener {
            var canLogin = true
            if (emailInput.text.length <=3 || !emailInput.text.contains("@")) {
                canLogin = false
                emailInput.error = "Enter a valid email"
            } else {
                emailInput.error = null
            }
            if (passwordInput.text.isEmpty()) {
                canLogin = false
                passwordInput.error = "Enter a password"
            } else {
                passwordInput.error = null
            }
            if (canLogin)
                onLoginListener?.invoke(emailInput.text.toString(), passwordInput.text.toString())
        }
        return view
    }
}