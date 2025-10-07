package com.simats.financetrack.repository

import android.content.Context
import com.simats.financetrack.auth.AuthManager
import com.simats.financetrack.database.DatabaseHelper
import com.simats.financetrack.models.*
import java.text.SimpleDateFormat
import java.util.*

class ExpenseRepository(private val context: Context) {
    
    private val dbHelper = DatabaseHelper(context)
    private val authManager = AuthManager(context)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    
    // Get current user ID, return 0 if no user is logged in
    private fun getCurrentUserId(): Long = authManager.getCurrentUserId()

    // User operations
    fun addUser(user: User): Long = dbHelper.addUser(user)
    fun getUser(id: Long): User? = dbHelper.getUser(id)

    // Category operations
    fun addCategory(category: Category): Long = dbHelper.addCategory(category)
    fun getAllCategories(): List<Category> = dbHelper.getAllCategories()
    fun getExpenseCategories(): List<Category> = dbHelper.getCategoriesByType("expense")
    fun getIncomeCategories(): List<Category> = dbHelper.getCategoriesByType("income")

    // Transaction operations
    fun addTransaction(transaction: Transaction): Long = dbHelper.addTransaction(transaction)
    
    fun getAllTransactions(): List<TransactionWithCategory> {
        val userId = getCurrentUserId()
        return if (userId > 0) {
            dbHelper.getAllTransactionsForUser(userId)
        } else {
            emptyList()
        }
    }
    
    fun getTransactionsByDateRange(startDate: String, endDate: String): List<TransactionWithCategory> {
        val userId = getCurrentUserId()
        return if (userId > 0) {
            dbHelper.getTransactionsByDateRangeForUser(userId, startDate, endDate)
        } else {
            emptyList()
        }
    }
    
    fun deleteTransaction(id: Long): Int = dbHelper.deleteTransaction(id)

    // Get transactions for current month
    fun getCurrentMonthTransactions(): List<TransactionWithCategory> {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val monthStr = String.format("%02d", month)
        val startDate = "$year-$monthStr-01"
        val endDate = "$year-$monthStr-31"
        return getTransactionsByDateRange(startDate, endDate)
    }

    // Analytics operations
    fun getCurrentBalance(): Double {
        val userId = getCurrentUserId()
        return if (userId > 0) {
            dbHelper.getCurrentBalanceForUser(userId)
        } else {
            0.0
        }
    }
    
    fun getMonthlySummary(monthYear: String): MonthlySummary {
        val userId = getCurrentUserId()
        return if (userId > 0) {
            dbHelper.getMonthlySummaryForUser(userId, monthYear)
        } else {
            MonthlySummary(monthYear, 0.0, 0.0, 0.0)
        }
    }
    
    fun getCurrentMonthSummary(): MonthlySummary {
        val currentMonthYear = monthYearFormat.format(Date())
        return getMonthlySummary(currentMonthYear)
    }
    
    fun getCategoryWiseExpense(monthYear: String): List<CategoryExpense> {
        val userId = getCurrentUserId()
        return if (userId > 0) {
            dbHelper.getCategoryWiseExpenseForUser(userId, monthYear)
        } else {
            emptyList()
        }
    }
    
    fun getCurrentMonthCategoryExpenses(): List<CategoryExpense> {
        val currentMonthYear = monthYearFormat.format(Date())
        return getCategoryWiseExpense(currentMonthYear)
    }

    // Budget operations
    fun addBudget(budget: Budget): Long = dbHelper.addBudget(budget)
    
    fun getBudgetForMonth(monthYear: String): List<BudgetWithCategory> {
        val userId = getCurrentUserId()
        return if (userId > 0) {
            dbHelper.getBudgetForMonthForUser(userId, monthYear)
        } else {
            emptyList()
        }
    }
    
    fun getCurrentMonthBudgets(): List<BudgetWithCategory> {
        val currentMonthYear = monthYearFormat.format(Date())
        return getBudgetForMonth(currentMonthYear)
    }

    // Dashboard data
    fun getDashboardData(): DashboardData {
        val currentBalance = getCurrentBalance()
        val monthlySummary = getCurrentMonthSummary()
        val recentTransactions = getAllTransactions().take(10) // Get last 10 transactions
        val categoryExpenses = getCurrentMonthCategoryExpenses()

        return DashboardData(
            currentBalance = currentBalance,
            monthlyIncome = monthlySummary.totalIncome,
            monthlyExpense = monthlySummary.totalExpense,
            recentTransactions = recentTransactions,
            categoryExpenses = categoryExpenses
        )
    }

    // Utility functions
    fun formatDate(date: Date): String = dateFormat.format(date)
    fun formatMonthYear(date: Date): String = monthYearFormat.format(date)
    fun getCurrentDate(): String = formatDate(Date())
    fun getCurrentMonthYear(): String = formatMonthYear(Date())

    // Search and filter functions
    fun searchTransactions(query: String): List<TransactionWithCategory> {
        return getAllTransactions().filter { transaction ->
            transaction.description?.contains(query, ignoreCase = true) == true ||
            transaction.categoryName.contains(query, ignoreCase = true)
        }
    }

    fun getTransactionsByCategory(categoryName: String): List<TransactionWithCategory> {
        return getAllTransactions().filter { transaction ->
            transaction.categoryName.equals(categoryName, ignoreCase = true)
        }
    }

    fun getTransactionsByType(type: String): List<TransactionWithCategory> {
        return getAllTransactions().filter { transaction ->
            transaction.type.equals(type, ignoreCase = true)
        }
    }

    // Statistical functions
    fun getAverageMonthlyExpense(): Double {
        val calendar = Calendar.getInstance()
        var totalExpense = 0.0
        var monthCount = 0

        // Get last 6 months data
        repeat(6) { i ->
            calendar.add(Calendar.MONTH, -i)
            val monthYear = monthYearFormat.format(calendar.time)
            val summary = getMonthlySummary(monthYear)
            totalExpense += summary.totalExpense
            monthCount++
            calendar.add(Calendar.MONTH, i) // Reset calendar
        }

        return if (monthCount > 0) totalExpense / monthCount else 0.0
    }

    fun getTopExpenseCategories(limit: Int = 5): List<CategoryExpense> {
        return getCurrentMonthCategoryExpenses()
            .sortedByDescending { it.totalAmount }
            .take(limit)
    }

    // Data validation
    fun isValidTransaction(transaction: Transaction): Boolean {
        return transaction.amount > 0 && 
               transaction.userId > 0 &&
               transaction.categoryId > 0 && 
               transaction.type in listOf("income", "expense") &&
               transaction.date.isNotEmpty()
    }

    fun isValidCategory(category: Category): Boolean {
        return category.name.isNotEmpty() && 
               category.type in listOf("income", "expense")
    }
}