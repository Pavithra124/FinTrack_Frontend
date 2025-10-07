package com.simats.financetrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ExpenseAlert : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout
        enableEdgeToEdge()

        setContentView(R.layout.activity_expense_alert)

        // Handle system window insets
        val rootView = findViewById<android.view.View>(R.id.layout_analytics)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Navigate to BudgetOverview when button is clicked
        val btnViewDetailedAnalysis: Button = findViewById(R.id.btnViewDetailedAnalysis)
        btnViewDetailedAnalysis.setOnClickListener {
            val intent = Intent(this, BudgetOverview::class.java)
            startActivity(intent)
        }
    }
}
