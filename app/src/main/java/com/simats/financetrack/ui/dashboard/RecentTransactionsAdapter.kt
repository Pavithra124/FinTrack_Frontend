package com.simats.financetrack.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simats.financetrack.databinding.ItemRecentTransactionBinding
import com.simats.financetrack.models.TransactionWithCategory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RecentTransactionsAdapter(
    private val onTransactionClick: (TransactionWithCategory) -> Unit
) : RecyclerView.Adapter<RecentTransactionsAdapter.TransactionViewHolder>() {

    private var transactions = listOf<TransactionWithCategory>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun updateTransactions(newTransactions: List<TransactionWithCategory>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemRecentTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    inner class TransactionViewHolder(
        private val binding: ItemRecentTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: TransactionWithCategory) {
            binding.apply {
                // Set category icon and name
                tvCategoryIcon.text = transaction.categoryIcon
                tvCategoryName.text = transaction.categoryName
                
                // Set description or category name if no description
                tvTransactionDescription.text = transaction.description?.takeIf { it.isNotEmpty() } 
                    ?: transaction.categoryName
                
                // Format and set date
                try {
                    val date = inputDateFormat.parse(transaction.date)
                    tvTransactionDate.text = date?.let { dateFormat.format(it) } ?: transaction.date
                } catch (e: Exception) {
                    tvTransactionDate.text = transaction.date
                }
                
                // Set amount with proper color
                val amountText = currencyFormat.format(transaction.amount)
                tvTransactionAmount.text = if (transaction.type == "income") "+$amountText" else "-$amountText"
                
                val amountColor = if (transaction.type == "income") {
                    Color.parseColor("#4CAF50") // Green for income
                } else {
                    Color.parseColor("#F44336") // Red for expense
                }
                tvTransactionAmount.setTextColor(amountColor)
                
                // Set category color indicator
                try {
                    viewCategoryColor.setBackgroundColor(Color.parseColor(transaction.categoryColor))
                } catch (e: Exception) {
                    viewCategoryColor.setBackgroundColor(Color.parseColor("#9E9E9E")) // Default gray
                }
                
                // Set click listener
                root.setOnClickListener {
                    onTransactionClick(transaction)
                }
            }
        }
    }
}