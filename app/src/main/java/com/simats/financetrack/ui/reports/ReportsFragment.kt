package com.simats.financetrack.ui.reports

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.simats.financetrack.databinding.FragmentReportsBinding
import com.simats.financetrack.repository.ExpenseRepository
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var repository: ExpenseRepository
    private lateinit var pdfGenerator: PDFGenerator
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    
    companion object {
        private const val WRITE_EXTERNAL_STORAGE_PERMISSION = 100
        private const val READ_MEDIA_PERMISSION = 101
    }
    
    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                generatePDFWithUri(uri)
            }
        } else {
            Toast.makeText(requireContext(), "File selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            repository = ExpenseRepository(requireContext())
            pdfGenerator = PDFGenerator(requireContext())
            loadReports()
            setupClickListeners()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error loading reports: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnDownloadReport.setOnClickListener {
            generatePDFReport()
        }
    }

    private fun loadReports() {
        try {
            val currentMonthYear = monthYearFormat.format(Date())
            val monthlySummary = repository.getMonthlySummary(currentMonthYear)
            val categoryExpenses = repository.getCurrentMonthCategoryExpenses()
            
            // Update current month summary
            binding.tvCurrentMonthIncome.text = currencyFormat.format(monthlySummary.totalIncome)
            binding.tvCurrentMonthExpense.text = currencyFormat.format(monthlySummary.totalExpense)
            binding.tvCurrentMonthBalance.text = currencyFormat.format(monthlySummary.balance)
            
            // Set balance color
            val balanceColor = if (monthlySummary.balance >= 0) {
                android.graphics.Color.parseColor("#4CAF50")
            } else {
                android.graphics.Color.parseColor("#F44336")
            }
            binding.tvCurrentMonthBalance.setTextColor(balanceColor)
            
            // Display top expense categories
            if (categoryExpenses.isNotEmpty()) {
                val topCategories = categoryExpenses.take(3)
                
                if (topCategories.isNotEmpty()) {
                    binding.tvTopCategory1.text = "${topCategories[0].categoryIcon} ${topCategories[0].categoryName}"
                    binding.tvTopCategoryAmount1.text = currencyFormat.format(topCategories[0].totalAmount)
                }
                
                if (topCategories.size > 1) {
                    binding.tvTopCategory2.text = "${topCategories[1].categoryIcon} ${topCategories[1].categoryName}"
                    binding.tvTopCategoryAmount2.text = currencyFormat.format(topCategories[1].totalAmount)
                    binding.layoutTopCategory2.visibility = View.VISIBLE
                }
                
                if (topCategories.size > 2) {
                    binding.tvTopCategory3.text = "${topCategories[2].categoryIcon} ${topCategories[2].categoryName}"
                    binding.tvTopCategoryAmount3.text = currencyFormat.format(topCategories[2].totalAmount)
                    binding.layoutTopCategory3.visibility = View.VISIBLE
                }
            }
            
            // Calculate and display statistics
            val avgMonthlyExpense = repository.getAverageMonthlyExpense()
            binding.tvAverageMonthlyExpense.text = currencyFormat.format(avgMonthlyExpense)
            
            val currentBalance = repository.getCurrentBalance()
            binding.tvTotalBalance.text = currencyFormat.format(currentBalance)
            
            // Set total balance color
            val totalBalanceColor = if (currentBalance >= 0) {
                android.graphics.Color.parseColor("#4CAF50")
            } else {
                android.graphics.Color.parseColor("#F44336")
            }
            binding.tvTotalBalance.setTextColor(totalBalanceColor)
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error loading financial data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun generatePDFReport() {
        try {
            // Show file picker dialog
            showFilePickerDialog()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error starting PDF generation: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showFilePickerDialog() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "FinanceReport_${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.pdf")
        }
        filePickerLauncher.launch(intent)
    }
    
    private fun generatePDFWithUri(uri: Uri) {
        try {
            // Show loading state
            Toast.makeText(requireContext(), "Generating report...", Toast.LENGTH_SHORT).show()
            
            // Generate PDF in background thread
            Thread {
                try {
                    val filePath = pdfGenerator.generateFinancialReport(uri)
                    
                    requireActivity().runOnUiThread {
                        if (filePath != null) {
                            Toast.makeText(requireContext(), "Report saved successfully!", Toast.LENGTH_LONG).show()
                            
                            // Optionally open the PDF
                            try {
                                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                startActivity(openIntent)
                            } catch (e: Exception) {
                                // PDF viewer not available, just show success message
                                Toast.makeText(requireContext(), "PDF saved. Open with any PDF viewer.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to generate report", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Error generating report: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error starting PDF generation: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}