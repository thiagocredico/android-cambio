package com.betrybe.currencyview.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.betrye.currencyview.R
import java.util.Locale

class CurrencyRatesAdapter(
    private val currencyRates: List<Pair<String, Double>>?,
    private val currencyNames: Map<String, String>?
) : RecyclerView.Adapter<CurrencyRatesAdapter.CurrencyRatesViewHolder>() {

    class CurrencyRatesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemCurrencyName: TextView = view.findViewById(R.id.item_currency_name)
        val itemCurrencyRate: TextView = view.findViewById(R.id.item_currency_rate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyRatesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_currency_rate, parent, false)
        return CurrencyRatesViewHolder(view)
    }

    override fun getItemCount(): Int = currencyRates?.size ?: 0
    override fun onBindViewHolder(holder: CurrencyRatesViewHolder, position: Int) {
        val currencyCode = currencyRates?.getOrNull(position)?.first ?: ""
        val currencyValue = currencyRates?.getOrNull(position)?.second ?: 0.0
        val currencyName = currencyNames?.getOrDefault(currencyCode, "Unknown") ?: "Unknown"

        val currencyInfo = "$currencyCode - $currencyName"

        holder.itemCurrencyName.text = currencyInfo
        holder.itemCurrencyRate.text = String.format(Locale.US, "%.2f", currencyValue)
    }
}
