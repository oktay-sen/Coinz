package com.oktaysen.coinz.layout.main.trading

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.oktaysen.coinz.R
import com.oktaysen.coinz.backend.Trading
import com.oktaysen.coinz.backend.pojo.Coin
import com.oktaysen.coinz.backend.pojo.Trade
import com.oktaysen.coinz.layout.main.MainActivity
import com.oktaysen.coinz.layout.util.ItemAdapter
import kotlinx.android.synthetic.main.fragment_trading_trades.*
import timber.log.Timber

class TradesFragment: Fragment() {
    private enum class TradeAction { ACCEPT, REJECT, CANCEL }

    companion object {
        fun newInstance(username: String, tradeIds: List<String>): TradesFragment {
            val bundle = Bundle()
            bundle.putString("username", username)
            bundle.putStringArrayList("tradeIds", ArrayList(tradeIds))
            val fragment = TradesFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    var trades: MutableMap<String, Triple<Trade, List<Coin>, List<Coin>>?> = mutableMapOf()

    fun getUsername(): String = arguments!!.getString("username")!!
    fun getTradeIds(): List<String> = arguments!!.getStringArrayList("tradeIds")!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_trading_trades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trades_list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    override fun onStart() {
        super.onStart()
        initialize()
    }

    fun initialize() {
        getTradeIds().forEach{ tradeId ->
            Trading().getTrade(tradeId) { trade, fromItems, toItems ->
                if (trade == null) {
                    trades[tradeId] = null
                    Timber.e("Could not get trade $tradeId.")
                } else {
                    trades[tradeId] = Triple(trade, fromItems, toItems)
                }
                if (trades.size == getTradeIds().size) {
                    updateTradesList()
                }
            }
        }
    }

    fun updateTradesList() {
        activity?.runOnUiThread {
            val trades = getTradeIds()
                    .map { trades[it] }
                    .filter { it != null }
                    .map { it as Triple<Trade, List<Coin>, List<Coin>> }
            trades_list.adapter = TradesAdapter(getUsername(), trades) { trade, action ->
                when (action) {
                    TradeAction.ACCEPT -> {
                        Trading().acceptTrade(trade.tradeId!!) { success ->
                            if (success) {
                                initialize()
                            } else {
                                Toast.makeText(context, "Something went wrong accepting the trade", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    TradeAction.REJECT, TradeAction.CANCEL -> {
                        Trading().rejectTrade(trade.tradeId!!) { success ->
                            if (success) {
                                initialize()
                            } else {
                                Toast.makeText(context, "Something went wrong canceling the trade", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private inner class TradesAdapter(val username: String, val trades: List<Triple<Trade, List<Coin>, List<Coin>>>, val onButtonClick: (trade: Trade, action: TradeAction) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == 0)
                return PendingViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.fragment_trading_trade_pending, parent, false))
            else
                return FinishedViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.fragment_trading_trade_finished, parent, false))
        }

        override fun onBindViewHolder(vh: RecyclerView.ViewHolder, pos: Int) {
            val (trade, fromItems, toItems) = trades[pos]
            when (vh) {
                is PendingViewHolder -> {
                    vh.fromInventory.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    vh.toInventory.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    vh.fromInventory.adapter = ItemAdapter(fromItems, null, context!!, null)
                    vh.toInventory.adapter = ItemAdapter(toItems, null, context!!, null)

                    vh.acceptButton.setOnClickListener { onButtonClick(trade, TradeAction.ACCEPT) }
                    vh.rejectButton.setOnClickListener { onButtonClick(trade, TradeAction.REJECT) }
                    vh.cancelButton.setOnClickListener { onButtonClick(trade, TradeAction.CANCEL) }

                    if (username == trade.toUsername) {
                        //We are the sender.
                        vh.title.text = "Offer to $username"
                        when (trade.state) {
                            Trade.State.PENDING -> {
                                vh.toTitle.text = "You will receive"
                                vh.fromTitle.text = "$username will receive"
                                vh.cancelContainer.visibility = View.VISIBLE
                                vh.acceptRejectContainer.visibility = View.GONE
                            }
                            Trade.State.ACCEPTED -> {
                                vh.toTitle.text = "You received"
                                vh.fromTitle.text = "$username received"
                                vh.cancelContainer.visibility = View.GONE
                                vh.acceptRejectContainer.visibility = View.GONE
                            }
                            Trade.State.CANCELED -> {
                                vh.toTitle.text = "You didn't receive"
                                vh.fromTitle.text = "$username didn't receive"
                                vh.cancelContainer.visibility = View.GONE
                                vh.acceptRejectContainer.visibility = View.GONE
                            }
                        }
                    } else {
                        //We are the receiver.
                        vh.title.text = "Offer from $username"
                        when (trade.state) {
                            Trade.State.PENDING -> {
                                vh.toTitle.text = "$username will receive"
                                vh.fromTitle.text = "You will receive"
                                vh.cancelContainer.visibility = View.GONE
                                vh.acceptRejectContainer.visibility = View.VISIBLE
                            }
                            Trade.State.ACCEPTED -> {
                                vh.toTitle.text = "$username received"
                                vh.fromTitle.text = "You received"
                                vh.cancelContainer.visibility = View.GONE
                                vh.acceptRejectContainer.visibility = View.GONE
                            }
                            Trade.State.CANCELED -> {
                                vh.toTitle.text = "$username didn't receive"
                                vh.fromTitle.text = "You didn't receive"
                                vh.cancelContainer.visibility = View.GONE
                                vh.acceptRejectContainer.visibility = View.GONE
                            }
                        }
                    }
                }
                is FinishedViewHolder -> {
                    if (trade.toUsername == username) {
                        //We are the sender
                        vh.title.text = "Offer to $username"
                        if (trade.state == Trade.State.ACCEPTED) {
                            vh.subtitle.text = "$username accepted your offer."
                        } else {
                            vh.subtitle.text = "This deal is canceled."
                        }
                    } else {
                        //We are the receiver
                        vh.title.text = "Offer from $username"
                        if (trade.state == Trade.State.ACCEPTED) {
                            vh.subtitle.text = "You accepted $username's offer."
                        } else {
                            vh.subtitle.text = "This deal is canceled."
                        }
                    }
                }
            }
        }

        override fun getItemCount() = trades.size

        override fun getItemViewType(pos: Int): Int {
            if (trades[pos].first.state == Trade.State.PENDING)
                return 0
            return 1
        }

        inner class PendingViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.title)
            val toTitle: TextView = view.findViewById(R.id.to_title)
            val fromTitle: TextView = view.findViewById(R.id.from_title)
            val toInventory: RecyclerView = view.findViewById(R.id.to_inventory)
            val fromInventory: RecyclerView = view.findViewById(R.id.from_inventory)
            val acceptRejectContainer: LinearLayout = view.findViewById(R.id.accept_reject_container)
            val cancelContainer: LinearLayout = view.findViewById(R.id.cancel_container)
            val acceptButton: Button = view.findViewById(R.id.accept_button)
            val rejectButton: Button = view.findViewById(R.id.reject_button)
            val cancelButton: Button = view.findViewById(R.id.cancel_button)
        }
        inner class FinishedViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.title)
            val subtitle: TextView = view.findViewById(R.id.subtitle)
        }
    }
}