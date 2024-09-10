package com.example.currencyconverter

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.currecnyconverter.R
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var fromCurrencySpinner: Spinner
    private lateinit var toCurrencySpinner: Spinner
    private lateinit var amountEditText: EditText
    private lateinit var convertButton: Button
    private lateinit var resultTextView: TextView

    private val apiKey = "358c72d1398dac310324118909411c55f42b4f38"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fromCurrencySpinner = findViewById(R.id.fromCurrencySpinner)
        toCurrencySpinner = findViewById(R.id.toCurrencySpinner)
        amountEditText = findViewById(R.id.amountEditText)
        convertButton = findViewById(R.id.convertButton)
        resultTextView = findViewById(R.id.resultTextView)

        fetchCurrencies()

        convertButton.setOnClickListener {
            val amount = amountEditText.text.toString().toDouble()
            val fromCurrency = fromCurrencySpinner.selectedItem.toString()
            val toCurrency = toCurrencySpinner.selectedItem.toString()
            convertCurrency(fromCurrency, toCurrency, amount)
        }
    }

    private fun fetchCurrencies() {
        val url = "https://currency.getgeoapi.com/api/v2/currency/list?api_key=$apiKey"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val currencies = JSONObject(body).getJSONObject("currencies")
                val currencyList = ArrayList<String>()
                currencies.keys().forEach { key -> currencyList.add(key) }

                runOnUiThread {
                    val adapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_spinner_item,
                        currencyList
                    )
                    fromCurrencySpinner.adapter = adapter
                    toCurrencySpinner.adapter = adapter
                }
            }
            override fun onFailure(call: Call, e: IOException) {}
        })
    }

    private fun convertCurrency(fromCurrency: String, toCurrency: String, amount: Double) {
        val url = "https://currency.getgeoapi.com/api/v2/currency/convert?api_key=$apiKey&from=$fromCurrency&to=$toCurrency&amount=$amount&format=json"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val rate = JSONObject(body).getJSONObject("rates").getJSONObject(toCurrency).getDouble("rate_for_amount")

                runOnUiThread {
                    resultTextView.text = "$amount $fromCurrency = $rate $toCurrency"
                }
            }
            override fun onFailure(call: Call, e: IOException) {}
        })
    }
}
