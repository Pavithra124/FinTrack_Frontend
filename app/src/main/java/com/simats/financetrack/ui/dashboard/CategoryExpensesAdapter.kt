package com.simats.financetrack.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simats.financetrack.databinding.ItemCategoryExpenseBinding
import com.simats.financetrack.models.CategoryExpense
import java.text.NumberFormat
import java.util.*

class CategoryExpensesAdapter : RecyclerView.Adapter<CategoryExpensesAdapter.CategoryExpenseViewHolder>() {

    private var categoryExpenses = listOf<CategoryExpense>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private var maxAmount = 0.0

    fun updateExpenses(newExpenses: List<CategoryExpense>) {
        categoryExpenses = newExpenses
        maxAmount = newExpenses.maxOfOrNull { it.totalAmount } ?: 0.0
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryExpenseViewHolder {
        val binding = ItemCategoryExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryExpenseViewHolder, position: Int) {
        holder.bind(categoryExpenses[position])
    }

    override fun getItemCount() = categoryExpenses.size

    inner class CategoryExpenseViewHolder(
        private val binding: ItemCategoryExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(categoryExpense: CategoryExpense) {
            binding.apply {
                // Set category details
                tvCategoryIcon.text = categoryExpense.categoryIcon
                tvCategoryName.text = categoryExpense.categoryName
                tvCategoryAmount.text = currencyFormat.format(categoryExpense.totalAmount)
                
                // Set category color
                try {
                    val color = Color.parseColor(categoryExpense.categoryColor)
                    viewCategoryColor.setBackgroundColor(color)
                    
                    // Set progress bar color and progress
                    progressBar.progressTintList = android.content.res.ColorStateList.valueOf(color)
                    val progress = if (maxAmount > 0) {
                        ((categoryExpense.totalAmount / maxAmount) * 100).toInt()
                    } else 0
                    progressBar.progress = progress
                    
                } catch (e: Exception) {
                    val defaultColor = Color.parseColor("#9E9E9E")
                    viewCategoryColor.setBackgroundColor(defaultColor)
                    progressBar.progressTintList = android.content.res.ColorStateList.valueOf(defaultColor)
                    progressBar.progress = 0
                }
                
                // Calculate and show percentage
                val totalExpenses = categoryExpenses.sumOf { it.totalAmount }
                val percentage = if (totalExpenses > 0) {
                    ((categoryExpense.totalAmount / totalExpenses) * 100).toInt()
                } else 0
                
                tvCategoryPercentage.text = "$percentage%"
            }
        }
    }
}