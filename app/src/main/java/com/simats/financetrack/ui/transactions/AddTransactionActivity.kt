package com.simats.financetrack.ui.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.financetrack.auth.AuthManager
import com.simats.financetrack.databinding.ActivityAddTransactionBinding
import com.simats.financetrack.models.Category
import com.simats.financetrack.models.Transaction
import com.simats.financetrack.repository.ExpenseRepository
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var repository: ExpenseRepository
    private lateinit var authManager: AuthManager
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    private var selectedDate = Date()
    private var selectedTransactionType = "expense" // Default
    private var categories = listOf<Category>()
    private var selectedCategory: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ExpenseRepository(this)
        authManager = AuthManager(this)

        // Get transaction type from intent
        selectedTransactionType = intent.getStringExtra("transaction_type") ?: "expense"

        setupUI()
        loadCategories()
        setupClickListeners()
    }

    private fun setupUI() {
        // Set title based on transaction type
        binding.tvTitle.text = if (selectedTransactionType == "income") "Add Income" else "Add Expense"
        
        // Set default date to today
        binding.etDate.setText(displayDateFormat.format(selectedDate))
        
        // Setup transaction type toggle
        updateTransactionTypeUI()
    }

    private fun loadCategories() {
        categories = if (selectedTransactionType == "income") {
            repository.getIncomeCategories()
        } else {
            repository.getExpenseCategories()
        }
        
        setupCategorySpinner()
    }

    private fun setupCategorySpinner() {
        val categoryNames = categories.map { "${it.icon} ${it.name}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.spinnerCategory.adapter = adapter
        
        binding.spinnerCategory.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                selectedCategory = null
            }
        })
        
        // Select first category by default
        if (categories.isNotEmpty()) {
            selectedCategory = categories[0]
        }
    }

    private fun setupClickListeners() {
        binding.btnIncome.setOnClickListener {
            if (selectedTransactionType != "income") {
                selectedTransactionType = "income"
                updateTransactionTypeUI()
                loadCategories()
            }
        }

        binding.btnExpense.setOnClickListener {
            if (selectedTransactionType != "expense") {
                selectedTransactionType = "expense"
                updateTransactionTypeUI()
                loadCategories()
            }
        }

        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
        
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun updateTransactionTypeUI() {
        if (selectedTransactionType == "income") {
            binding.btnIncome.setBackgroundColor(getColor(android.R.color.holo_green_light))
            binding.btnExpense.setBackgroundColor(getColor(android.R.color.darker_gray))
            binding.tvTitle.text = "Add Income"
        } else {
            binding.btnExpense.setBackgroundColor(getColor(android.R.color.holo_red_light))
            binding.btnIncome.setBackgroundColor(getColor(android.R.color.darker_gray))
            binding.tvTitle.text = "Add Expense"
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.time
                binding.etDate.setText(displayDateFormat.format(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun saveTransaction() {
        val amountText = binding.etAmount.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        // Validation
        if (amountText.isEmpty()) {
            binding.etAmount.error = "Please enter amount"
            binding.etAmount.requestFocus()
            return
        }

        val amount = try {
            amountText.toDouble()
        } catch (e: NumberFormatException) {
            binding.etAmount.error = "Please enter valid amount"
            binding.etAmount.requestFocus()
            return
        }

        if (amount <= 0) {
            binding.etAmount.error = "Amount must be greater than 0"
            binding.etAmount.requestFocus()
            return
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        // Create transaction
        val transaction = Transaction(
            userId = authManager.getCurrentUserId(),
            categoryId = selectedCategory!!.id,
            amount = amount,
            type = selectedTransactionType,
            description = description.ifEmpty { null },
            date = dateFormat.format(selectedDate)
        )

        // Save transaction
        val transactionId = repository.addTransaction(transaction)
        
        if (transactionId > 0) {
            Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show()
        }
    }
}