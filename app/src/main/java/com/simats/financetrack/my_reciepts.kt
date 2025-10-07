package com.simats.financetrack

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import java.io.IOException
import java.util.*

class MyReciepts : AppCompatActivity() {

    private lateinit var etDate: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var etTags: EditText
    private lateinit var rgCategories: RadioGroup
    private lateinit var btnSaveReceipt: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge to edge layout
        enableEdgeToEdge()

        // Set the layout
        setContentView(R.layout.activity_my_reciepts)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollViewMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        etDate = findViewById(R.id.etDate)
        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        etTags = findViewById(R.id.etTags)
        rgCategories = findViewById(R.id.rgCategories)
        btnSaveReceipt = findViewById(R.id.btnSaveReceipt)

        // Setup DatePicker on etDate click
        etDate.setOnClickListener {
            showDatePicker()
        }

        // Save receipt action
        btnSaveReceipt.setOnClickListener {
            saveReceipt()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                etDate.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun saveReceipt() {
        val date = etDate.text.toString().trim()
        val amount = etAmount.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val tags = etTags.text.toString().trim()

        val selectedCategoryId = rgCategories.checkedRadioButtonId
        val category = when (selectedCategoryId) {
            R.id.rbFoodDining -> "Food and Dining"
            R.id.rbTravel -> "Travel"
            R.id.rbFuture -> "Future"
            R.id.rbEntertainment -> "Entertainment"
            R.id.rbShopping -> "Shopping"
            R.id.rbBills -> "Bills"
            R.id.rbHealth -> "Health"
            R.id.rbEducation -> "Education"
            else -> ""
        }

        if (date.isEmpty() || amount.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill in Date, Amount and select a Category", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("date", date)
            .add("amount", amount)
            .add("description", description)
            .add("tags", tags)
            .add("category", category)
            .build()

        val request = Request.Builder()
            .url("https://8jkb7c9c-80.inc1.devtunnels.ms/FinanceTrack/save_receipt.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MyReciepts, "Failed to save receipt: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MyReciepts, "Receipt saved successfully", Toast.LENGTH_SHORT).show()
                        clearInputs()
                    } else {
                        Toast.makeText(this@MyReciepts, "Error saving receipt: $responseText", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun clearInputs() {
        etDate.text.clear()
        etAmount.text.clear()
        etDescription.text.clear()
        etTags.text.clear()
        rgCategories.check(R.id.rbFoodDining) // Reset to default category
    }
}
