package com.oktaysen.coinz.layout.login

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.oktaysen.coinz.R
import kotlinx.android.synthetic.main.fragment_login_choice.*

class ChoiceFragment: Fragment() {
    var onEmailClickListener:(() -> Unit)? = null
    var onGoogleClickListener:(() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_choice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login_email.setOnClickListener { onEmailClickListener?.invoke() }
        login_google.setOnClickListener { onGoogleClickListener?.invoke() }
    }
}