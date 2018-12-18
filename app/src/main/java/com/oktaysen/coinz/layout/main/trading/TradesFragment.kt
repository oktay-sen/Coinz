package com.oktaysen.coinz.layout.main.trading

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.oktaysen.coinz.R
import com.oktaysen.coinz.backend.Users
import com.oktaysen.coinz.backend.pojo.Trade

class TradesFragment: Fragment() {

    companion object {
        fun newInstance(username: String, trades: List<Trade>): TradesFragment {
            val bundle = Bundle()
            bundle.putString("username", username)
            bundle.putSerializable("trades", ArrayList(trades))
            val fragment = TradesFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    fun getUsername(): String = arguments!!.getString("username")!!
    fun getTrades(): List<Trade> = arguments!!.getSerializable("trades") as ArrayList<Trade>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_trading_trades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO: List the trades in a RecyclerView
    }
}