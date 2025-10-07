package com.simats.financetrack

/**
 * Data class representing an expense item
 */
data class Expense(
    val category: String,
    val amount: Double,
    val date: String,
    val iconResId: Int
)