package com.simats.financetrack.ui.reports

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.simats.financetrack.auth.AuthManager
import com.simats.financetrack.models.MonthlySummary
import com.simats.financetrack.models.CategoryExpense
import com.simats.financetrack.repository.ExpenseRepository
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PDFGenerator(private val context: Context) {
    
    private val authManager = AuthManager(context)
    private val repository = ExpenseRepository(context)
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    
    fun generateFinancialReport(outputUri: Uri? = null): String? {
        return try {
            val user = authManager.getCurrentUser()
            if (user == null) {
                return null
            }
            
            val currentMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            val monthlySummary = repository.getMonthlySummary(currentMonthYear)
            val categoryExpenses = repository.getCurrentMonthCategoryExpenses()
            val currentBalance = repository.getCurrentBalance()
            
            val fileName = "FinanceReport_${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.pdf"
            
            val outputStream = if (outputUri != null) {
                context.contentResolver.openOutputStream(outputUri)
            } else {
                val file = getOutputFile(fileName)
                if (file == null) return null
                FileOutputStream(file)
            }
            
            if (outputStream == null) {
                return null
            }
            
            // Generate proper PDF using iText
            generatePDFDocument(outputStream, user.displayName, monthlySummary, categoryExpenses, currentBalance)
            
            outputStream.close()
            
            if (outputUri != null) {
                outputUri.toString()
            } else {
                getOutputFile(fileName)?.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun getOutputFile(fileName: String): File? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+ (API 29+), use app-specific external storage
                val downloadsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "")
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                File(downloadsDir, fileName)
            } else {
                // For older versions, use public downloads directory
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                File(downloadsDir, fileName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun generatePDFDocument(
        outputStream: OutputStream,
        userName: String,
        monthlySummary: MonthlySummary,
        categoryExpenses: List<CategoryExpense>,
        currentBalance: Double
    ) {
        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)
        
        // Set up fonts
        val headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
        val titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
        val normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
        val smallFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
        
        // Add header
        val header = Paragraph("FINANCE TRACK")
            .setFont(headerFont)
            .setFontSize(24f)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20f)
        document.add(header)
        
        // Add report title
        val title = Paragraph("Financial Report")
            .setFont(titleFont)
            .setFontSize(18f)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(30f)
        document.add(title)
        
        // Add user info and date
        val reportDate = dateFormat.format(Date())
        val userInfo = Paragraph("User: $userName\nReport Date: $reportDate")
            .setFont(normalFont)
            .setFontSize(12f)
            .setMarginBottom(20f)
        document.add(userInfo)
        
        // Add financial summary section
        document.add(createSectionHeader("Financial Summary"))
        
        val summaryTable = Table(2)
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(20f)
        
        summaryTable.addCell(createTableCell("Total Income", true))
        summaryTable.addCell(createTableCell(currencyFormat.format(monthlySummary.totalIncome), false))
        
        summaryTable.addCell(createTableCell("Total Expenses", true))
        summaryTable.addCell(createTableCell(currencyFormat.format(monthlySummary.totalExpense), false))
        
        summaryTable.addCell(createTableCell("Monthly Balance", true))
        val balanceColor = if (monthlySummary.balance >= 0) ColorConstants.GREEN else ColorConstants.RED
        summaryTable.addCell(createTableCell(currencyFormat.format(monthlySummary.balance), false, balanceColor))
        
        summaryTable.addCell(createTableCell("Total Balance", true))
        val totalBalanceColor = if (currentBalance >= 0) ColorConstants.GREEN else ColorConstants.RED
        summaryTable.addCell(createTableCell(currencyFormat.format(currentBalance), false, totalBalanceColor))
        
        document.add(summaryTable)
        
        // Add expense categories section
        if (categoryExpenses.isNotEmpty()) {
            document.add(createSectionHeader("Expense Categories"))
            
            val categoryTable = Table(3)
                .setWidth(UnitValue.createPercentValue(100f))
                .setMarginBottom(20f)
            
            categoryTable.addCell(createTableCell("Category", true))
            categoryTable.addCell(createTableCell("Amount", true))
            categoryTable.addCell(createTableCell("Percentage", true))
            
            val totalExpenses = monthlySummary.totalExpense
            categoryExpenses.take(10).forEach { category ->
                val percentage = if (totalExpenses > 0) {
                    String.format("%.1f%%", (category.totalAmount / totalExpenses) * 100)
                } else "0.0%"
                
                categoryTable.addCell(createTableCell("${category.categoryIcon} ${category.categoryName}", false))
                categoryTable.addCell(createTableCell(currencyFormat.format(category.totalAmount), false))
                categoryTable.addCell(createTableCell(percentage, false))
            }
            
            document.add(categoryTable)
        }
        
        // Add footer
        document.add(Paragraph("\n\n"))
        val footer = Paragraph("Generated by Finance Track App\nReport generated on: $reportDate")
            .setFont(smallFont)
            .setFontSize(10f)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(30f)
        document.add(footer)
        
        document.close()
    }
    
    private fun createSectionHeader(text: String): Paragraph {
        return Paragraph(text)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            .setFontSize(14f)
            .setMarginTop(20f)
            .setMarginBottom(10f)
    }
    
    private fun createTableCell(text: String, isHeader: Boolean, color: com.itextpdf.kernel.colors.Color? = null): Cell {
        val cell = Cell().add(Paragraph(text)
            .setFont(PdfFontFactory.createFont(if (isHeader) StandardFonts.HELVETICA_BOLD else StandardFonts.HELVETICA))
            .setFontSize(if (isHeader) 12f else 10f))
        
        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY)
        }
        
        if (color != null) {
            cell.setFontColor(color)
        }
        
        cell.setPadding(8f)
        return cell
    }
}