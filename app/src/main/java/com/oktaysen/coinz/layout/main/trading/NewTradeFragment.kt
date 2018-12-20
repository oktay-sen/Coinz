package com.oktaysen.coinz.layout.main.trading

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.oktaysen.coinz.R
import com.oktaysen.coinz.backend.Trading
import com.oktaysen.coinz.backend.Users
import com.oktaysen.coinz.backend.pojo.Coin
import com.oktaysen.coinz.backend.pojo.User
import com.oktaysen.coinz.layout.main.MainActivity
import com.oktaysen.coinz.layout.util.ItemAdapter
import kotlinx.android.synthetic.main.fragment_trading_new_trade.*
import timber.log.Timber

class NewTradeFragment: Fragment() {

    var tradingWith: User? = null
    var toInventory: List<Coin>? = null
    var fromInventory: List<Coin>? = null

    var fromSelected: List<Coin> = listOf()
    var toSelected: List<Coin> = listOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_trading_new_trade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        to_inventory.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        from_inventory.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        updateSecondHalf()

        username.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                username_button.isEnabled = !s.isNullOrEmpty()
                tradingWith = null
                updateSecondHalf()
            }
        })

        username_button.setOnClickListener {
            Users().getUserFromUsername(username.text.toString()) { user ->
                if (user == null) {
                    username.error = "Player not found"
                    return@getUserFromUsername
                }
                tradingWith = user

                Trading().getCurrentUserInventory { ownItems ->
                    if (ownItems == null) {
                        Timber.e("Failed to retrieve the current user's inventory.")
                        return@getCurrentUserInventory
                    }
                    fromInventory = ownItems
                    Trading().getUserInventory(user) { theirItems ->
                        if (theirItems == null) {
                            Timber.e("Failed to retrieve the target user's inventory.")
                            return@getUserInventory
                        }
                        toInventory = theirItems
                        updateSecondHalf()
                    }
                }
            }
        }

        submit_button.setOnClickListener {
            if (tradingWith == null || (toSelected.isEmpty() && fromSelected.isEmpty())) {
                return@setOnClickListener
            }
            Trading().newTradeWithUser(tradingWith!!.id!!, fromSelected, toSelected) { success ->
                if (success) {
                    if (activity is MainActivity) {
                        (activity as MainActivity).tradingFragmentInstance.refreshTrades() //TODO: Try to find a better design for this.
                        (activity as MainActivity).tradingFragmentInstance.openDefault()
                    }
                } else {
                    Toast.makeText(context, "An error has occured. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun updateSecondHalf() {
        activity?.runOnUiThread {
            if (tradingWith != null && toInventory != null && fromInventory != null && second_half_container.visibility != View.VISIBLE) {
                second_half_container.visibility = View.VISIBLE
                to_title.text = "${tradingWith!!.username}'s Inventory"
                to_inventory.adapter = ItemAdapter(toInventory!!, toSelected, context!!) {list, item ->
                    toSelected = list
                    updateSubmitButton()
                }
                from_inventory.adapter = ItemAdapter(fromInventory!!, fromSelected, context!!) {list, item ->
                    fromSelected = list
                    updateSubmitButton()
                }
            }
            if ((tradingWith == null || toInventory == null || fromInventory == null) && second_half_container.visibility != View.GONE){
                toSelected = listOf()
                fromSelected = listOf()
                second_half_container.visibility = View.GONE
                to_title.text = ""
                to_inventory.adapter = null
                from_inventory.adapter = null
                updateSubmitButton()
            }
        }
    }

    fun updateSubmitButton() {
        submit_button.isEnabled = !(toSelected.isEmpty() && fromSelected.isEmpty())
    }
}