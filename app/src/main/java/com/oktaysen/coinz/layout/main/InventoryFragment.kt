package com.oktaysen.coinz.layout.main

import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.oktaysen.coinz.R
import com.oktaysen.coinz.backend.Inventory
import com.oktaysen.coinz.backend.ListenerRegistry
import com.oktaysen.coinz.backend.pojo.Coin
import com.oktaysen.coinz.backend.pojo.Coin.Currency.*
import com.oktaysen.coinz.backend.pojo.Rates
import com.oktaysen.coinz.layout.util.ItemAdapter
import kotlinx.android.synthetic.main.fragment_main_inventory.*
import timber.log.Timber

class InventoryFragment:Fragment() {

    var walletSelected: List<Coin> = listOf()

    var walletDone = false
    var bankDone = false
    var ratesDone = false

    var walletListenerId: Int? = null
    var bankAccountListenerId: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_main_inventory, container, false)
    }

    override fun onStart() {
        super.onStart()
        wallet_items.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        bank_items.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        updateButtonVisibility()
        deposit_button.setOnClickListener { Inventory().depositCoins(walletSelected, null) }

        updateRefresh()
        walletListenerId = Inventory().listenToWallet { current, _, modified, removed ->
            activity?.runOnUiThread {
                walletDone = true
                updateRefresh()
                if (current.isEmpty())
                    wallet_empty_message.visibility = View.VISIBLE
                else
                    wallet_empty_message.visibility = View.GONE
                walletSelected = walletSelected
                        .filter { !removed.contains(it) }
                        .map {
                            val index = modified.indexOf(it)
                            if (index > -1) modified[index]
                            else it
                        }
                updateButtonVisibility()
                wallet_items.adapter = ItemAdapter(current, walletSelected, context!!) { items, item ->
                    walletSelected = items
                    updateButtonVisibility()
                }
            }
        }

        bankAccountListenerId = Inventory().listenToBankAccount { current, _, modified, removed ->
            activity?.runOnUiThread {
                bankDone = true
                updateRefresh()
                if (current.isEmpty())
                    bank_empty_message.visibility = View.VISIBLE
                else
                    bank_empty_message.visibility = View.GONE
                wallet_subtitle.text = "${10 - (Inventory().getDepositedTodayCount()?:0)} deposits left"
                bank_items.adapter = ItemAdapter(current, null, context!!, null)
            }
        }

        Inventory().getTodaysRates { rates ->
            ratesDone = true
            updateRefresh()
            if (rates == null) return@getTodaysRates
            activity?.runOnUiThread {
                rates_text.text = getRatesText(rates)
                rates_text.isSelected = true
            }
        }
    }

    private fun updateRefresh() {
        activity?.runOnUiThread {
            if (walletDone && bankDone && ratesDone && refresh.isRefreshing) {
                refresh.isRefreshing = false
                refresh.isEnabled = false
            }
            if (!(walletDone && bankDone && ratesDone) && !refresh.isRefreshing){
                refresh.isEnabled = true
                refresh.isRefreshing = true
            }
        }
    }

    private fun updateButtonVisibility() {
        activity?.runOnUiThread {
            if (walletSelected.isNotEmpty() && 10 - (Inventory().coinsDepositedToday
                            ?: 0) >= walletSelected.size)
                if (deposit_button.isOrWillBeHidden)
                    deposit_button.show()
            if (walletSelected.isEmpty() || 10 - (Inventory().coinsDepositedToday
                            ?: 0) < walletSelected.size)
                if (deposit_button.isOrWillBeShown)
                    deposit_button.hide()
        }
    }

    fun getRatesText(rates: Rates): CharSequence {
        val currencies: Set<Coin.Currency> = setOf(DOLR, PENY, SHIL, QUID)
        val result = SpannableStringBuilder()
        currencies.forEach { currency ->
            val color = context!!.getColor(when(currency){
                DOLR -> R.color.dolrPrimary
                SHIL -> R.color.shilPrimary
                PENY -> R.color.penyPrimary
                QUID -> R.color.quidPrimary
            })
            val currencyText = "$currency "
            val value = "%.3f    ".format(when (currency) {
                DOLR -> rates.dolr
                SHIL -> rates.shil
                PENY -> rates.peny
                QUID -> rates.quid
            })

            val start = result.length
            val str = "$currencyText$value"
            result.append(str)
            result.setSpan(ForegroundColorSpan(color), start, start + str.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            result.setSpan(StyleSpan(Typeface.BOLD), start, start + currencyText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return result
    }

    override fun onStop() {
        super.onStop()
        Timber.v("onStop")
        if (walletListenerId != null) {
            val result = ListenerRegistry().unregister(walletListenerId!!)
            Timber.v("Wallet listener removed: $result")
        }
        if (bankAccountListenerId != null) {
            val result = ListenerRegistry().unregister(bankAccountListenerId!!)
            Timber.v("Bank account listener removed: $result")
        }
    }
}