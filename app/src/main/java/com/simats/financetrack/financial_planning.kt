package com.simats.financetrack

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FinancialPlanning : AppCompatActivity() {

    private lateinit var etPrimarySalary: EditText
    private lateinit var etAdditionalIncome: EditText
    private lateinit var etBudget: EditText
    private lateinit var tvTotalIncome: TextView
    private lateinit var btnSave: Button

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_financial_planning)

        // Bind views
        etPrimarySalary = findViewById(R.id.etPrimarySalary)
        etAdditionalIncome = findViewById(R.id.etAdditionalIncome)
        etBudget = findViewById(R.id.etBudget)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        btnSave = findViewById(R.id.btnSavee)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTotalIncome()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etPrimarySalary.addTextChangedListener(textWatcher)
        etAdditionalIncome.addTextChangedListener(textWatcher)

        btnSave.setOnClickListener {
            saveIncomeData()
        }
    }

    private fun updateTotalIncome() {
        val primary = etPrimarySalary.text.toString().toDoubleOrNull() ?: 0.0
        val additional = etAdditionalIncome.text.toString().toDoubleOrNull() ?: 0.0
        val total = primary + additional

        tvTotalIncome.text = String.format("₹%.2f", total)
    }

    private fun saveIncomeData() {
        val primary = etPrimarySalary.text.toString().trim()
        val additional = etAdditionalIncome.text.toString().trim()
        val budget = etBudget.text.toString().trim()
        val total = tvTotalIncome.text.toString().replace("₹", "").trim()

        if (primary.isEmpty()) {
            Toast.makeText(this, "Please enter primary salary", Toast.LENGTH_SHORT).show()
            return
        }
        if (budget.isEmpty()) {
            Toast.makeText(this, "Please enter your monthly budget", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = 1 // Replace with actual logged-in user id

        val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        val json = """
            {
              "user_id": $userId,
              "primary_salary": $primary,
              "additional_income": $additional,
              "amount": $total,
              "month_year": "$monthYear",
              "budget": $budget
            }
        """.trimIndent()

        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("https://8jkb7c9c-80.inc1.devtunnels.ms/FinanceTrack/save_financial_planning.php")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@FinancialPlanning,
                        "Failed to save data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@FinancialPlanning,
                            "Financial data saved successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@FinancialPlanning, FinancialOverview::class.java)
                        intent.putExtra("total_income", total)
                        intent.putExtra("budget", budget)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@FinancialPlanning,
                            "Server error: ${response.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }
}
