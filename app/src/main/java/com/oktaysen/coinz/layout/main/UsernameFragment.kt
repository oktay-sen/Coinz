package com.oktaysen.coinz.layout.main

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.oktaysen.coinz.R
import com.oktaysen.coinz.backend.Users
import kotlinx.android.synthetic.main.fragment_main_username.*

class UsernameDialog: DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_username, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        main_username.setOnEditorActionListener { _, _, _ ->
            main_username.error = null
            return@setOnEditorActionListener false
        }

        main_username_button.setOnClickListener {
            var canRegister = true
            if (main_username.text.length <=3) {
                canRegister = false
                main_username.error = "Must be more than 3 characters."
            } else {
                main_username.error = null
            }
            if (canRegister) {
                val username = main_username.text.toString()
                Users().updateUsername(username) { success, errorMessage ->
                    if (!success) {
                        main_username.error = errorMessage
                        return@updateUsername
                    }
                    dismiss()
                }
            }
        }
    }
}