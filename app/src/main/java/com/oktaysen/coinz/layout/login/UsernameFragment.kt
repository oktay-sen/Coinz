package com.oktaysen.coinz.layout.login

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.oktaysen.coinz.R
import kotlinx.android.synthetic.main.fragment_login_username.*

class UsernameFragment: Fragment() {
    var onUsernameSelectedListener:((String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_username, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login_username.setOnEditorActionListener { _, _, _ ->
            login_username.error = null
            return@setOnEditorActionListener false
        }

        login_username_button.setOnClickListener {
            var canRegister = true
            if (login_username.text.length <=3) {
                canRegister = false
                login_username.error = "Must be more than 3 characters."
            } else {
                login_username.error = null
            }
            if (canRegister)
                onUsernameSelectedListener?.invoke(login_username.text.toString())
        }
    }
}