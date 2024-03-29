package com.oktaysen.coinz.layout.login

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.oktaysen.coinz.R
import kotlinx.android.synthetic.main.fragment_login_forgot.*

class ForgotFragment: Fragment() {
    var onResetListener:((String) -> Unit)? = null
    var onBackListener:(() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_forgot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        back_button.setOnClickListener { onBackListener?.invoke() }

        login_input_email.setOnEditorActionListener { _, _, _ ->
            login_input_email.error = null
            return@setOnEditorActionListener false
        }

        login_reset_password_button.setOnClickListener {
            var canRegister = true
            if (login_input_email.text.length <=3 || !login_input_email.text.contains("@")) {
                canRegister = false
                login_input_email.error = "Enter a valid email"
            } else {
                login_input_email.error = null
            }
            if (canRegister)
                onResetListener?.invoke(login_input_email.text.toString())
        }
    }
}