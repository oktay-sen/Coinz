package com.oktaysen.coinz.layout.login

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.oktaysen.coinz.R

class RegisterFragment: Fragment() {
    var onRegisterListener:((String, String) -> Unit)? = null
    var onBackListener:(() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login_register, container, false)
        view.findViewById<Button>(R.id.back_button).setOnClickListener { onBackListener?.invoke() }

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

        val passwordInput2 = view.findViewById<EditText>(R.id.login_input_password_again)
        passwordInput2.setOnEditorActionListener { _, _, _ ->
            passwordInput2.error = null
            return@setOnEditorActionListener false
        }

        view.findViewById<Button>(R.id.register_button).setOnClickListener {
            var canRegister = true
            if (emailInput.text.length <=3 || !emailInput.text.contains("@")) {
                canRegister = false
                emailInput.error = "Enter a valid email"
            } else {
                emailInput.error = null
            }
            if (passwordInput.text.isEmpty()) {
                canRegister = false
                passwordInput.error = "Enter a password"
            } else if (passwordInput.text.length < 8) {
                passwordInput.error = "Password must be at least 8 characters"
            } else {
                passwordInput.error = null
            }
            if (passwordInput.text.toString() != passwordInput2.text.toString()) {
                canRegister = false
                passwordInput2.error = "Passwords don't match"
            } else {
                passwordInput2.error = null
            }
            if (canRegister)
                onRegisterListener?.invoke(emailInput.text.toString(), passwordInput.text.toString())
        }
        return view
    }
}