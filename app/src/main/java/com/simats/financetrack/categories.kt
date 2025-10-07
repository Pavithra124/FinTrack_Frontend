package com.simats.financetrack

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Categories : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        enableEdgeToEdge()
        setContentView(R.layout.activity_categories)

        // Handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // RecyclerView setup
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Expense list (sample data)
        val expenseList = listOf(
            Expense("Food and Dining", 400.0, "12 Sep 2025", R.drawable.food),
            Expense("Transportation", 125.0, "11 Sep 2025", R.drawable.transportation),
            Expense("Shopping", 230.0, "10 Sep 2025", R.drawable.shopping),
            Expense("Utilities", 90.0, "09 Sep 2025", R.drawable.utilities),
            Expense("Healthcare", 300.0, "08 Sep 2025", R.drawable.healthcare),
            Expense("Entertainment", 220.0, "07 Sep 2025", R.drawable.entertainment),
            Expense("Travel", 1500.0, "06 Sep 2025", R.drawable.travel),
            Expense("Business", 700.0, "05 Sep 2025", R.drawable.business),
            Expense("Others", 80.0, "04 Sep 2025", R.drawable.others)
        )

        recyclerView.adapter = ExpensesAdapter(expenseList)
    }
}
