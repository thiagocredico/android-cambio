package com.betrybe.currencyview.ui.views.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.betrybe.currencyview.data.api.ApiService
import com.betrybe.currencyview.data.models.CurrencySymbolResponse
import com.betrybe.currencyview.ui.adapters.CurrencyRatesAdapter
import com.betrye.currencyview.R
import com.google.android.material.textview.MaterialTextView
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val API_KEY = "gKSrABlHD03DgJxz5bn3CKCC0XK4gY01"
const val BASE_URL = "https://api.apilayer.com/exchangerates_data/"
const val ERROR_FETCHING_CURRENCY_NAMES = "Error fetching currency names"
const val ERROR_FETCHING_LATEST_RATES = "Error fetching latest rates"

object RetrofitClient {
    private val client = OkHttpClient.Builder().apply {
        addInterceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .header("apikey", API_KEY)
                .build()
            chain.proceed(newRequest)
        }
    }.build()

    val apiService: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        retrofit.create(ApiService::class.java)
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var autoComplete: AutoCompleteTextView
    private lateinit var loadingElement: FrameLayout
    private lateinit var selectCurrencyText: MaterialTextView
    private lateinit var currencyRatesRecyclerView: RecyclerView
    private var currencyNames: Map<String, String> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        currencyNames = emptyMap()

        initViews()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val symbolsResponse = fetchSymbols()
                val symbols = symbolsResponse.symbols.keys.toList()
                setupAutoComplete(symbols)
                currencyNames = fetchCurrencyNames() ?: emptyMap()

                hideLoading()
            } catch (e: HttpException) {
                handleException(e)
            } catch (e: IOException) {
                handleException(e)
            }
        }
    }

    private suspend fun fetchCurrencyNames(): Map<String, String>? {
        return try {
            RetrofitClient.apiService.getCurrencySymbols().symbols
        } catch (e: IOException) {
            Log.e("FETCH_CURRENCY_ERROR", "$ERROR_FETCHING_CURRENCY_NAMES: IO error - $e")
            null
        } catch (e: HttpException) {
            Log.e("FETCH_CURRENCY_ERROR", "$ERROR_FETCHING_CURRENCY_NAMES: HTTP error - $e")
            null
        }
    }

    private fun initViews() {
        autoComplete = findViewById(R.id.currency_selection_input_layout)
        loadingElement = findViewById(R.id.waiting_response_state)
        selectCurrencyText = findViewById(R.id.select_currency_state)
        currencyRatesRecyclerView = findViewById(R.id.currency_rates_state)
    }

    private suspend fun fetchSymbols(): CurrencySymbolResponse {
        return try {
            RetrofitClient.apiService.getCurrencySymbols()
        } catch (e: IOException) {
            Log.e("FETCH_SYMBOLS_ERROR", "$ERROR_FETCHING_LATEST_RATES: IO error - $e")
            throw e
        } catch (e: HttpException) {
            Log.e("FETCH_SYMBOLS_ERROR", "$ERROR_FETCHING_LATEST_RATES: HTTP error - $e")
            throw e
        }
    }

    private fun setupAutoComplete(symbols: List<String>) {
        val adapter = ArrayAdapter(
            this@MainActivity,
            android.R.layout.simple_dropdown_item_1line,
            symbols
        )
        autoComplete.setAdapter(adapter)
        autoComplete.setOnClickListener {
            autoComplete.showDropDown()
        }
        selectCurrencyText.visibility = View.VISIBLE
        autoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedCurrency = symbols[position]
            CoroutineScope(Dispatchers.Main).launch {
                getLatestRates(selectedCurrency)
            }
        }
    }

    private suspend fun getLatestRates(baseCurrency: String) {
        try {
            showLoading()

            val latestRatesResponse =
                RetrofitClient.apiService.getLatestRates(baseCurrency)

            val ratesList = latestRatesResponse.rates.toList()
            currencyNames = fetchCurrencyNames() ?: emptyMap()

            val currencyRatesAdapter = CurrencyRatesAdapter(ratesList, currencyNames)
            currencyRatesRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            currencyRatesRecyclerView.adapter = currencyRatesAdapter

            currencyRatesRecyclerView.visibility = View.VISIBLE
            selectCurrencyText.visibility = View.GONE
        } catch (e: HttpException) {
            handleException(e)
        } catch (e: IOException) {
            handleException(e)
        } finally {
            hideLoading()
        }
    }

    private fun showLoading() {
        loadingElement.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        loadingElement.visibility = View.GONE
    }

    private fun handleException(e: Exception) {
        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        Log.e("MAIN_ACTIVITY_ERROR", "Error: ${e.message}", e)
        hideLoading()
    }
}
