package com.simats.financetrack

import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class savedexpensesActivity : AppCompatActivity() {

    private lateinit var llReceiptsContainer: LinearLayout
    private lateinit var requestQueue: RequestQueue

    // Replace with your actual PHP API URL
    private val API_URL = "http://YOUR_SERVER/get_receipts.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.savedexpenses)

        llReceiptsContainer = findViewById(R.id.llReceiptsContainer)
        requestQueue = Volley.newRequestQueue(this)

        fetchReceipts()
    }

    private fun fetchReceipts() {
        val jsonRequest = JsonObjectRequest(Request.Method.GET, API_URL, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val receipts = response.getJSONArray("receipts")
                        displayReceipts(receipts)
                    } else {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "JSON Parsing error", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonRequest)
    }

    private fun displayReceipts(receipts: org.json.JSONArray) {
        // Remove all child views except the title at index 0
        while (llReceiptsContainer.childCount > 1) {
            llReceiptsContainer.removeViewAt(1)
        }

        for (i in 0 until receipts.length()) {
            val receipt = receipts.getJSONObject(i)
            val category = receipt.getString("category")
            val amount = receipt.getString("amount")
            val date = receipt.getString("date")

            // Create a new LinearLayout for this receipt dynamically
            val receiptLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
                setBackgroundColor(resources.getColor(android.R.color.white))
            }

            val tvCategory = TextView(this).apply {
                text = category
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
            }

            val tvAmount = TextView(this).apply {
                text = "Amount: ₹$amount"
                textSize = 14f
                setPadding(0, 8, 0, 0)
            }

            val tvDate = TextView(this).apply {
                text = "Date: $date"
                textSize = 12f
                setPadding(0, 8, 0, 0)
            }

            receiptLayout.addView(tvCategory)
            receiptLayout.addView(tvAmount)
            receiptLayout.addView(tvDate)

            llReceiptsContainer.addView(receiptLayout)
        }
    }
}
