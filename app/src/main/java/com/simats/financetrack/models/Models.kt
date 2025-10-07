package com.simats.financetrack.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val profileImage: String? = null,
    val createdAt: String = "",
    val lastLoginAt: String? = null,
    val isActive: Boolean = true
) : Parcelable {
    val fullName: String get() = "$firstName $lastName".trim().ifEmpty { username }
    val displayName: String get() = fullName.ifEmpty { username }
}

@Parcelize
data class UserSession(
    val userId: Long,
    val username: String,
    val loginTime: Long,
    val lastActivityTime: Long,
    val isActive: Boolean = true
) : Parcelable

@Parcelize
data class UserCredentials(
    val username: String,
    val password: String
) : Parcelable

@Parcelize
data class Category(
    val id: Long = 0,
    val name: String,
    val type: String, // "income" or "expense"
    val color: String,
    val icon: String
) : Parcelable

@Parcelize
data class Transaction(
    val id: Long = 0,
    val userId: Long,
    val categoryId: Long,
    val amount: Double,
    val type: String, // "income" or "expense"
    val description: String?,
    val date: String // Format: yyyy-MM-dd
) : Parcelable

@Parcelize
data class TransactionWithCategory(
    val id: Long,
    val amount: Double,
    val type: String,
    val description: String?,
    val date: String,
    val categoryName: String,
    val categoryColor: String,
    val categoryIcon: String
) : Parcelable

@Parcelize
data class Budget(
    val id: Long = 0,
    val userId: Long,
    val categoryId: Long?,
    val amount: Double,
    val monthYear: String // Format: yyyy-MM
) : Parcelable

@Parcelize
data class BudgetWithCategory(
    val id: Long,
    val amount: Double,
    val monthYear: String,
    val categoryName: String,
    val categoryColor: String,
    val categoryIcon: String
) : Parcelable

@Parcelize
data class MonthlySummary(
    val monthYear: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double
) : Parcelable

@Parcelize
data class CategoryExpense(
    val categoryName: String,
    val categoryColor: String,
    val categoryIcon: String,
    val totalAmount: Double
) : Parcelable

@Parcelize
data class DashboardData(
    val currentBalance: Double,
    val monthlyIncome: Double,
    val monthlyExpense: Double,
    val recentTransactions: List<TransactionWithCategory>,
    val categoryExpenses: List<CategoryExpense>
) : Parcelable