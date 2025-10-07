package com.simats.financetrack.ui.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.simats.financetrack.databinding.FragmentDashboardBinding
import com.simats.financetrack.models.DashboardData
import com.simats.financetrack.repository.ExpenseRepository
import com.simats.financetrack.ui.transactions.AddTransactionActivity
import java.text.NumberFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: ExpenseRepository
    private lateinit var recentTransactionsAdapter: RecentTransactionsAdapter
    private lateinit var categoryExpensesAdapter: CategoryExpensesAdapter
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = ExpenseRepository(requireContext())
        
        setupRecyclerViews()
        setupClickListeners()
        loadDashboardData()
    }

    private fun setupRecyclerViews() {
        // Recent Transactions
        recentTransactionsAdapter = RecentTransactionsAdapter { transaction ->
            // Handle transaction click - maybe show edit dialog
        }
        binding.rvRecentTransactions.apply {
            adapter = recentTransactionsAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Category Expenses
        categoryExpensesAdapter = CategoryExpensesAdapter()
        binding.rvCategoryExpenses.apply {
            adapter = categoryExpensesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTransaction.setOnClickListener {
            startActivity(Intent(context, AddTransactionActivity::class.java))
        }

        binding.btnAddIncome.setOnClickListener {
            val intent = Intent(context, AddTransactionActivity::class.java)
            intent.putExtra("transaction_type", "income")
            startActivity(intent)
        }

        binding.btnAddExpense.setOnClickListener {
            val intent = Intent(context, AddTransactionActivity::class.java)
            intent.putExtra("transaction_type", "expense")
            startActivity(intent)
        }
    }

    private fun loadDashboardData() {
        val dashboardData = repository.getDashboardData()
        updateUI(dashboardData)
    }

    private fun updateUI(dashboardData: DashboardData) {
        // Update balance cards
        binding.tvCurrentBalance.text = currencyFormat.format(dashboardData.currentBalance)
        binding.tvMonthlyIncome.text = currencyFormat.format(dashboardData.monthlyIncome)
        binding.tvMonthlyExpense.text = currencyFormat.format(dashboardData.monthlyExpense)
        
        // Set balance color based on positive/negative
        val balanceColor = if (dashboardData.currentBalance >= 0) {
            Color.parseColor("#4CAF50") // Green for positive
        } else {
            Color.parseColor("#F44336") // Red for negative
        }
        binding.tvCurrentBalance.setTextColor(balanceColor)

        // Update recent transactions
        if (dashboardData.recentTransactions.isNotEmpty()) {
            binding.tvNoTransactions.visibility = View.GONE
            binding.rvRecentTransactions.visibility = View.VISIBLE
            recentTransactionsAdapter.updateTransactions(dashboardData.recentTransactions)
        } else {
            binding.tvNoTransactions.visibility = View.VISIBLE
            binding.rvRecentTransactions.visibility = View.GONE
        }

        // Update category expenses
        if (dashboardData.categoryExpenses.isNotEmpty()) {
            binding.rvCategoryExpenses.visibility = View.VISIBLE
            binding.tvNoCategoryData.visibility = View.GONE
            categoryExpensesAdapter.updateExpenses(dashboardData.categoryExpenses)
            setupPieChart(dashboardData.categoryExpenses)
        } else {
            binding.rvCategoryExpenses.visibility = View.GONE
            binding.tvNoCategoryData.visibility = View.VISIBLE
            binding.pieChartExpenses.visibility = View.GONE
        }
    }

    private fun setupPieChart(categoryExpenses: List<com.simats.financetrack.models.CategoryExpense>) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        categoryExpenses.forEach { expense ->
            entries.add(PieEntry(expense.totalAmount.toFloat(), expense.categoryName))
            colors.add(Color.parseColor(expense.categoryColor))
        }

        val dataSet = PieDataSet(entries, "Expenses by Category").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        val pieData = PieData(dataSet)
        binding.pieChartExpenses.apply {
            data = pieData
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 40f
            transparentCircleRadius = 45f
            setDrawEntryLabels(false)
            legend.isEnabled = false
            invalidate() // refresh the chart
        }
        
        binding.pieChartExpenses.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData() // Refresh data when returning to dashboard
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}