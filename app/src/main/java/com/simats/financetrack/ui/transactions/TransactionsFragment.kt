package com.simats.financetrack.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.financetrack.databinding.FragmentTransactionsBinding
import com.simats.financetrack.models.TransactionWithCategory
import com.simats.financetrack.repository.ExpenseRepository

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: ExpenseRepository
    private lateinit var transactionsAdapter: TransactionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = ExpenseRepository(requireContext())
        
        setupRecyclerView()
        setupClickListeners()
        loadTransactions()
    }

    private fun setupRecyclerView() {
        transactionsAdapter = TransactionsAdapter(
            onTransactionClick = { transaction ->
                // Handle transaction click - could show details or edit
                showTransactionDetails(transaction)
            },
            onDeleteClick = { transaction ->
                // Handle delete transaction
                deleteTransaction(transaction)
            }
        )
        
        binding.rvTransactions.apply {
            adapter = transactionsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTransaction.setOnClickListener {
            startActivity(Intent(context, AddTransactionActivity::class.java))
        }
        
        binding.btnFilterAll.setOnClickListener {
            loadTransactions()
        }
        
        binding.btnFilterIncome.setOnClickListener {
            filterTransactions("income")
        }
        
        binding.btnFilterExpense.setOnClickListener {
            filterTransactions("expense")
        }
    }

    private fun loadTransactions() {
        val transactions = repository.getAllTransactions()
        updateUI(transactions)
        updateFilterButtons("all")
    }
    
    private fun filterTransactions(type: String) {
        val transactions = repository.getTransactionsByType(type)
        updateUI(transactions)
        updateFilterButtons(type)
    }
    
    private fun updateFilterButtons(activeFilter: String) {
        // Reset all buttons
        binding.btnFilterAll.setBackgroundColor(requireContext().getColor(android.R.color.transparent))
        binding.btnFilterIncome.setBackgroundColor(requireContext().getColor(android.R.color.transparent))
        binding.btnFilterExpense.setBackgroundColor(requireContext().getColor(android.R.color.transparent))
        
        // Highlight active button
        when (activeFilter) {
            "all" -> binding.btnFilterAll.setBackgroundColor(requireContext().getColor(android.R.color.holo_blue_light))
            "income" -> binding.btnFilterIncome.setBackgroundColor(requireContext().getColor(android.R.color.holo_green_light))
            "expense" -> binding.btnFilterExpense.setBackgroundColor(requireContext().getColor(android.R.color.holo_red_light))
        }
    }

    private fun updateUI(transactions: List<TransactionWithCategory>) {
        if (transactions.isNotEmpty()) {
            binding.rvTransactions.visibility = View.VISIBLE
            binding.tvNoTransactions.visibility = View.GONE
            transactionsAdapter.updateTransactions(transactions)
            
            // Update summary
            val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
            val balance = totalIncome - totalExpense
            
            binding.tvTotalIncome.text = "₹${String.format("%.2f", totalIncome)}"
            binding.tvTotalExpense.text = "₹${String.format("%.2f", totalExpense)}"
            binding.tvNetBalance.text = "₹${String.format("%.2f", balance)}"
            
        } else {
            binding.rvTransactions.visibility = View.GONE
            binding.tvNoTransactions.visibility = View.VISIBLE
        }
    }
    
    private fun showTransactionDetails(transaction: TransactionWithCategory) {
        // For now, just show a simple dialog with transaction details
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Transaction Details")
            .setMessage("""
                Category: ${transaction.categoryName}
                Amount: ₹${transaction.amount}
                Type: ${transaction.type.capitalize()}
                Date: ${transaction.date}
                Description: ${transaction.description ?: "No description"}
            """.trimIndent())
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Delete") { dialog, _ ->
                deleteTransaction(transaction)
                dialog.dismiss()
            }
            .create()
            
        dialog.show()
    }
    
    private fun deleteTransaction(transaction: TransactionWithCategory) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                val result = repository.deleteTransaction(transaction.id)
                if (result > 0) {
                    android.widget.Toast.makeText(context, "Transaction deleted", android.widget.Toast.LENGTH_SHORT).show()
                    loadTransactions() // Refresh the list
                } else {
                    android.widget.Toast.makeText(context, "Error deleting transaction", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions() // Refresh when returning from add transaction
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}