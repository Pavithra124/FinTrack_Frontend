package com.simats.financetrack

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class FinancialOverview : AppCompatActivity() {

    data class Receipt(
        val id: String,
        val category: String,
        val date: String,
        val amount: String,
        val description: String,
        val tags: String
    )

    inner class ReceiptAdapter(private val receipts: List<Receipt>) :
        RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder>() {

        inner class ReceiptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val desc: TextView = itemView.findViewById(R.id.tvDescription)
            val date: TextView = itemView.findViewById(R.id.tvDate)
            val amount: TextView = itemView.findViewById(R.id.tvAmount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_receipt, parent, false)
            return ReceiptViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
            val receipt = receipts[position]
            holder.desc.text = receipt.description
            holder.date.text = receipt.date
            holder.amount.text = "₹${receipt.amount}"
        }

        override fun getItemCount() = receipts.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_financial_overview)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.topBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // PieChart
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        setupPieChart(pieChart)

        // Income from Intent
        displayIncomeFromIntent()

        // RecyclerView for Receipts
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewReceipts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        fetchRecentReceipts(recyclerView)

        // Navigation Listeners
        findViewById<LinearLayout>(R.id.Addexp).setOnClickListener {
            startActivity(Intent(this, MyReciepts::class.java))
        }
        findViewById<LinearLayout>(R.id.layout_analytics).setOnClickListener {
            startActivity(Intent(this, ExpenseAlert::class.java))
        }
        findViewById<TextView>(R.id.tvExpenseLabel).setOnClickListener {
            startActivity(Intent(this, BudgetAllocation::class.java))
        }
        findViewById<LinearLayout>(R.id.moreLayout).setOnClickListener {
            startActivity(Intent(this, BudgetAllocation::class.java))
        }
        findViewById<CardView>(R.id.cardBalance).setOnClickListener {
            startActivity(Intent(this, FinancialPlanning::class.java))
        }
        findViewById<LinearLayout>(R.id.layout_saved_receipts).setOnClickListener {
            startActivity(Intent(this, savedexpensesActivity::class.java))
        }
    }

    private fun fetchRecentReceipts(recyclerView: RecyclerView) {
        val url = "http://YOUR_SERVER_IP_OR_DOMAIN/get_receipts.php"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val receiptsJson = response.getJSONArray("receipts")
                val receiptList = mutableListOf<Receipt>()
                for (i in 0 until receiptsJson.length()) {
                    val obj = receiptsJson.getJSONObject(i)
                    receiptList.add(
                        Receipt(
                            id = obj.getString("id"),
                            category = obj.getString("category"),
                            date = obj.getString("date"),
                            amount = obj.getString("amount"),
                            description = obj.getString("description"),
                            tags = obj.getString("tags")
                        )
                    )
                }
                recyclerView.adapter = ReceiptAdapter(receiptList)
            },
            { error ->
                error.printStackTrace()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun displayIncomeFromIntent() {
        val totalIncome = intent.getStringExtra("total_income") ?: "0.00"
        findViewById<TextView>(R.id.tvIncome).text = "₹$totalIncome"
    }

    private fun setupPieChart(pieChart: PieChart) {
        val entries = arrayListOf(
            PieEntry(30f, "Food & Dining"),
            PieEntry(20f, "Transportation"),
            PieEntry(15f, "Shopping"),
            PieEntry(25f, "Bills"),
            PieEntry(10f, "Others")
        )

        val dataSet = PieDataSet(entries, "Expenses")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList() + ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = android.graphics.Color.BLACK

        val data = PieData(dataSet)
        pieChart.data = data

        pieChart.description.isEnabled = false
        pieChart.centerText = "Expenses"
        pieChart.setEntryLabelColor(android.graphics.Color.BLACK)
        pieChart.setUsePercentValues(true)
        pieChart.animateY(1200)

        val legend: Legend = pieChart.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.topbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, PersonalDetails::class.java))
                true
            }
            R.id.action_notifications -> {
                startActivity(Intent(this, Notifications::class.java))
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
