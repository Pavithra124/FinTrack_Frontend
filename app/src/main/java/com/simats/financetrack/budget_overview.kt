package com.simats.financetrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BudgetOverview : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layouts
        enableEdgeToEdge()

        setContentView(R.layout.activity_budget_overview)

        // Handle system window insets safely
        val rootView = findViewById<android.view.View>(R.id.main)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Back to ExpenseAlert Activity
        val btnViewDetailedAnalysis: Button? = findViewById(R.id.btnViewDetailedAnalysis)
        btnViewDetailedAnalysis?.setOnClickListener {
            val intent = Intent(this, ExpenseAlert::class.java)
            startActivity(intent)
        }
    }
}
