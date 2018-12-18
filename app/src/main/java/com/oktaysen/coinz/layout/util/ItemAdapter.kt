package com.oktaysen.coinz.layout.util

import android.content.Context
import android.support.design.card.MaterialCardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.oktaysen.coinz.R
import com.oktaysen.coinz.backend.pojo.Coin
import com.oktaysen.coinz.backend.pojo.Item
import com.oktaysen.coinz.layout.main.InventoryFragment

class ItemAdapter(val items: List<Item>, selectedItems: List<Item>?, val context: Context, val onItemClick: ((List<Item>, Item) -> Unit)?): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val selected = selectedItems?.toMutableList()?: mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            0 -> CoinViewHolder(inflater.inflate(R.layout.fragment_item_coin, parent, false))
            else -> throw Error("Unknown item type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(pos: Int): Int = when(items[pos]) {
        is Coin -> 0
        else -> throw Error("Unknown item type")
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, pos: Int) {
        when (vh) {
            is CoinViewHolder -> {
                val coin = items[pos] as Coin
                vh.currency.text = coin.currency.toString()
                vh.amount.text = "%.3f".format(coin.value)
                vh.imageContainer.setCardBackgroundColor(context.getColor(when (coin.currency!!) {
                    Coin.Currency.DOLR -> R.color.dolrPrimary
                    Coin.Currency.SHIL -> R.color.shilPrimary
                    Coin.Currency.PENY -> R.color.penyPrimary
                    Coin.Currency.QUID -> R.color.quidPrimary
                }))
                vh.container.setCardBackgroundColor(context.getColor(when (coin.currency) {
                    Coin.Currency.DOLR -> R.color.dolrSecondary
                    Coin.Currency.SHIL -> R.color.shilSecondary
                    Coin.Currency.PENY -> R.color.penySecondary
                    Coin.Currency.QUID -> R.color.quidSecondary
                }))
                vh.selectedOverlay.visibility = if (selected.contains(coin)) View.VISIBLE else View.INVISIBLE
                if (onItemClick != null)
                    vh.itemView.setOnClickListener {
                        if (selected.contains(items[pos])) {
                            selected.remove(items[pos])
                            vh.selectedOverlay.visibility = View.INVISIBLE
                        } else {
                            selected.add(items[pos])
                            vh.selectedOverlay.visibility = View.VISIBLE
                        }
                        onItemClick.invoke(selected, items[pos])
                    }
            }
        }
    }

    inner class CoinViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: MaterialCardView = view.findViewById(R.id.container)
        val imageContainer: MaterialCardView = view.findViewById(R.id.coin_image_container)
        val currency: TextView = view.findViewById(R.id.currency)
        val amount: TextView = view.findViewById(R.id.amount)
        val selectedOverlay: FrameLayout = view.findViewById(R.id.selected_overlay)
    }
}