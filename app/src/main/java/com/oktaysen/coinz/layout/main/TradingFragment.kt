package com.oktaysen.coinz.layout.main

import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.oktaysen.coinz.R
import com.oktaysen.coinz.backend.Trading
import com.oktaysen.coinz.backend.pojo.Trade
import com.oktaysen.coinz.layout.main.trading.DefaultFragment
import com.oktaysen.coinz.layout.main.trading.NewTradeFragment
import com.oktaysen.coinz.layout.main.trading.TradesFragment
import kotlinx.android.synthetic.main.fragment_main_trading.*

class TradingFragment:Fragment() {

    var selectedContact: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_main_trading, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        openDefault()

        new_trade_button.setOnClickListener { openNewTrade() }

        contacts.layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
        refreshTrades()
    }

    fun refreshTrades() {
        Trading().getTrades { usernames, trades ->
            if (usernames == null || trades == null) return@getTrades
            contacts.adapter = ContactAdapter(usernames) {pos ->
                openTrades(usernames[pos], trades[usernames[pos]]!!.map { it.tradeId!! })
            }
        }
    }

    fun openDefault() {
        fragmentManager!!.beginTransaction()
                .replace(R.id.trade_container, DefaultFragment())
                .commit()
    }

    fun openNewTrade() {
        fragmentManager!!.beginTransaction()
                .replace(R.id.trade_container, NewTradeFragment())
                .commit()
    }

    fun openTrades(username: String, trades: List<String>) {
        fragmentManager!!.beginTransaction()
                .replace(R.id.trade_container, TradesFragment.newInstance(username, trades))
        .commit()
    }

    inner class ContactAdapter(val contacts: List<String>, val onContactClick: (Int) -> Unit): RecyclerView.Adapter<ContactAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, itemType: Int): ContactAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_contact, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = contacts.size

        override fun onBindViewHolder(vh: ContactAdapter.ViewHolder, pos: Int) {
            vh.name.text = contacts[pos]
            if (pos == selectedContact) {
                vh.name.setTypeface(vh.name.typeface, Typeface.BOLD)
            } else {
                vh.name.setTypeface(vh.name.typeface, Typeface.NORMAL)
            }
            vh.name.setOnClickListener{
                selectedContact = pos
                notifyDataSetChanged()
                onContactClick(pos)
            }
        }

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.contact_name)
        }
    }
}