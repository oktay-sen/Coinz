package com.oktaysen.coinz.layout.login

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.oktaysen.coinz.R

class LoginForgotFragment: Fragment() {
    var onResetListener:((String) -> Unit)? = null
    var onBackListener:(() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login_forgot, container, false)
        view.findViewById<Button>(R.id.back_button).setOnClickListener { onBackListener?.invoke() }

        val emailInput = view.findViewById<EditText>(R.id.login_input_email)
        emailInput.setOnEditorActionListener { _, _, _ ->
            emailInput.error = null
            return@setOnEditorActionListener false
        }

        view.findViewById<Button>(R.id.login_reset_password_button).setOnClickListener {
            var canRegister = true
            if (emailInput.text.length <=3 || !emailInput.text.contains("@")) {
                canRegister = false
                emailInput.error = "Enter a valid email"
            } else {
                emailInput.error = null
            }
            if (canRegister)
                onResetListener?.invoke(emailInput.text.toString())
        }
        return view
    }
}