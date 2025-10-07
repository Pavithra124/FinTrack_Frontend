package com.simats.financetrack.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.simats.financetrack.models.*
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "expense_tracker.db"
        private const val DATABASE_VERSION = 2 // Updated for multi-user support

        // Table Names
        private const val TABLE_USERS = "users"
        private const val TABLE_CATEGORIES = "categories"
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val TABLE_BUDGETS = "budgets"

        // Common Columns
        private const val KEY_ID = "id"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_CREATED_AT = "created_at"

        // Users Table Columns
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_PROFILE_IMAGE = "profile_image"
        private const val KEY_LAST_LOGIN_AT = "last_login_at"
        private const val KEY_IS_ACTIVE = "is_active"

        // Categories Table Columns
        private const val KEY_CATEGORY_NAME = "name"
        private const val KEY_CATEGORY_TYPE = "type"
        private const val KEY_CATEGORY_COLOR = "color"
        private const val KEY_CATEGORY_ICON = "icon"

        // Transactions Table Columns
        private const val KEY_CATEGORY_ID = "category_id"
        private const val KEY_AMOUNT = "amount"
        private const val KEY_TYPE = "type"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"

        // Budgets Table Columns
        private const val KEY_MONTH_YEAR = "month_year"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Users Table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_USERNAME TEXT UNIQUE NOT NULL,
                $KEY_EMAIL TEXT,
                $KEY_PASSWORD_HASH TEXT NOT NULL,
                $KEY_FIRST_NAME TEXT,
                $KEY_LAST_NAME TEXT,
                $KEY_PROFILE_IMAGE TEXT,
                $KEY_LAST_LOGIN_AT DATETIME,
                $KEY_IS_ACTIVE BOOLEAN DEFAULT 1,
                $KEY_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        // Create Categories Table
        val createCategoriesTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_CATEGORY_NAME TEXT NOT NULL,
                $KEY_CATEGORY_TYPE TEXT CHECK($KEY_CATEGORY_TYPE IN ('income', 'expense')) NOT NULL,
                $KEY_CATEGORY_COLOR TEXT,
                $KEY_CATEGORY_ICON TEXT,
                $KEY_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        // Create Transactions Table
        val createTransactionsTable = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_USER_ID INTEGER NOT NULL,
                $KEY_CATEGORY_ID INTEGER NOT NULL,
                $KEY_AMOUNT REAL NOT NULL,
                $KEY_TYPE TEXT CHECK($KEY_TYPE IN ('income', 'expense')) NOT NULL,
                $KEY_DESCRIPTION TEXT,
                $KEY_DATE DATE NOT NULL,
                $KEY_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($KEY_USER_ID) REFERENCES $TABLE_USERS($KEY_ID),
                FOREIGN KEY ($KEY_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($KEY_ID)
            )
        """.trimIndent()

        // Create Budgets Table
        val createBudgetsTable = """
            CREATE TABLE $TABLE_BUDGETS (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_USER_ID INTEGER NOT NULL,
                $KEY_CATEGORY_ID INTEGER,
                $KEY_AMOUNT REAL NOT NULL,
                $KEY_MONTH_YEAR TEXT NOT NULL,
                $KEY_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($KEY_USER_ID) REFERENCES $TABLE_USERS($KEY_ID),
                FOREIGN KEY ($KEY_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($KEY_ID)
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createCategoriesTable)
        db.execSQL(createTransactionsTable)
        db.execSQL(createBudgetsTable)

        // Insert default categories
        insertDefaultCategories(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGETS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    private fun insertDefaultCategories(db: SQLiteDatabase) {
        val defaultCategories = listOf(
            Category(0, "Food & Dining", "expense", "#FF5722", "🍽️"),
            Category(0, "Transportation", "expense", "#2196F3", "🚗"),
            Category(0, "Shopping", "expense", "#E91E63", "🛍️"),
            Category(0, "Entertainment", "expense", "#9C27B0", "🎬"),
            Category(0, "Healthcare", "expense", "#4CAF50", "🏥"),
            Category(0, "Bills & Utilities", "expense", "#FF9800", "💡"),
            Category(0, "Education", "expense", "#3F51B5", "📚"),
            Category(0, "Salary", "income", "#4CAF50", "💼"),
            Category(0, "Business", "income", "#00BCD4", "💸"),
            Category(0, "Freelance", "income", "#795548", "💻"),
            Category(0, "Other Income", "income", "#607D8B", "💰"),
            Category(0, "Other Expense", "expense", "#9E9E9E", "📝")
        )

        defaultCategories.forEach { category ->
            val values = ContentValues().apply {
                put(KEY_CATEGORY_NAME, category.name)
                put(KEY_CATEGORY_TYPE, category.type)
                put(KEY_CATEGORY_COLOR, category.color)
                put(KEY_CATEGORY_ICON, category.icon)
            }
            db.insert(TABLE_CATEGORIES, null, values)
        }
    }

    // User operations
    fun addUser(user: User): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_USERNAME, user.username)
            put(KEY_EMAIL, user.email)
            put(KEY_PASSWORD_HASH, user.passwordHash)
            put(KEY_FIRST_NAME, user.firstName)
            put(KEY_LAST_NAME, user.lastName)
            put(KEY_PROFILE_IMAGE, user.profileImage)
            put(KEY_LAST_LOGIN_AT, user.lastLoginAt)
            put(KEY_IS_ACTIVE, if (user.isActive) 1 else 0)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun getUser(id: Long): User? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_USERS, null, "$KEY_ID=?", arrayOf(id.toString()), null, null, null)
        
        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USERNAME)) ?: "",
                email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)) ?: "",
                passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PASSWORD_HASH)) ?: "",
                firstName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FIRST_NAME)) ?: "",
                lastName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_NAME)) ?: "",
                profileImage = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_IMAGE)),
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT)) ?: "",
                lastLoginAt = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_LOGIN_AT)),
                isActive = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_ACTIVE)) == 1
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }
    
    fun getUserByUsername(username: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_USERS, null, "$KEY_USERNAME=? COLLATE NOCASE", arrayOf(username), null, null, null)
        
        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USERNAME)) ?: "",
                email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)) ?: "",
                passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PASSWORD_HASH)) ?: "",
                firstName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FIRST_NAME)) ?: "",
                lastName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_NAME)) ?: "",
                profileImage = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_IMAGE)),
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT)) ?: "",
                lastLoginAt = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_LOGIN_AT)),
                isActive = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_ACTIVE)) == 1
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }
    
    fun updateUser(user: User): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_USERNAME, user.username)
            put(KEY_EMAIL, user.email)
            put(KEY_PASSWORD_HASH, user.passwordHash)
            put(KEY_FIRST_NAME, user.firstName)
            put(KEY_LAST_NAME, user.lastName)
            put(KEY_PROFILE_IMAGE, user.profileImage)
            put(KEY_LAST_LOGIN_AT, user.lastLoginAt)
            put(KEY_IS_ACTIVE, if (user.isActive) 1 else 0)
        }
        return db.update(TABLE_USERS, values, "$KEY_ID=?", arrayOf(user.id.toString()))
    }
    
    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_USERS, null, "$KEY_IS_ACTIVE=?", arrayOf("1"), null, null, KEY_USERNAME)
        
        if (cursor.moveToFirst()) {
            do {
                val user = User(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                    username = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USERNAME)) ?: "",
                    email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)) ?: "",
                    passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PASSWORD_HASH)) ?: "",
                    firstName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FIRST_NAME)) ?: "",
                    lastName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_NAME)) ?: "",
                    profileImage = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_IMAGE)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT)) ?: "",
                    lastLoginAt = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_LOGIN_AT)),
                    isActive = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_ACTIVE)) == 1
                )
                users.add(user)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return users
    }

    // Category operations
    fun addCategory(category: Category): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_CATEGORY_NAME, category.name)
            put(KEY_CATEGORY_TYPE, category.type)
            put(KEY_CATEGORY_COLOR, category.color)
            put(KEY_CATEGORY_ICON, category.icon)
        }
        return db.insert(TABLE_CATEGORIES, null, values)
    }

    fun getAllCategories(): List<Category> {
        val categories = mutableListOf<Category>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CATEGORIES ORDER BY $KEY_CATEGORY_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val category = Category(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_TYPE)),
                    color = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_COLOR)),
                    icon = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_ICON))
                )
                categories.add(category)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return categories
    }

    fun getCategoriesByType(type: String): List<Category> {
        val categories = mutableListOf<Category>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CATEGORIES, null, "$KEY_CATEGORY_TYPE=?", 
            arrayOf(type), null, null, KEY_CATEGORY_NAME
        )

        if (cursor.moveToFirst()) {
            do {
                val category = Category(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_TYPE)),
                    color = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_COLOR)),
                    icon = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_ICON))
                )
                categories.add(category)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return categories
    }

    // Transaction operations
    fun addTransaction(transaction: Transaction): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_USER_ID, transaction.userId)
            put(KEY_CATEGORY_ID, transaction.categoryId)
            put(KEY_AMOUNT, transaction.amount)
            put(KEY_TYPE, transaction.type)
            put(KEY_DESCRIPTION, transaction.description)
            put(KEY_DATE, transaction.date)
        }
        return db.insert(TABLE_TRANSACTIONS, null, values)
    }

    fun getAllTransactions(): List<TransactionWithCategory> {
        val transactions = mutableListOf<TransactionWithCategory>()
        val db = this.readableDatabase
        val query = """
            SELECT t.$KEY_ID, t.$KEY_AMOUNT, t.$KEY_TYPE, t.$KEY_DESCRIPTION, t.$KEY_DATE, 
                   c.$KEY_CATEGORY_NAME, c.$KEY_CATEGORY_COLOR, c.$KEY_CATEGORY_ICON
            FROM $TABLE_TRANSACTIONS t
            INNER JOIN $TABLE_CATEGORIES c ON t.$KEY_CATEGORY_ID = c.$KEY_ID
            ORDER BY t.$KEY_DATE DESC, t.$KEY_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val transaction = TransactionWithCategory(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_AMOUNT)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)),
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME)),
                    categoryColor = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_COLOR)),
                    categoryIcon = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_ICON))
                )
                transactions.add(transaction)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactions
    }

    fun getAllTransactionsForUser(userId: Long): List<TransactionWithCategory> {
        val transactions = mutableListOf<TransactionWithCategory>()
        val db = this.readableDatabase
        val query = """
            SELECT t.$KEY_ID, t.$KEY_AMOUNT, t.$KEY_TYPE, t.$KEY_DESCRIPTION, t.$KEY_DATE, 
                   c.$KEY_CATEGORY_NAME, c.$KEY_CATEGORY_COLOR, c.$KEY_CATEGORY_ICON
            FROM $TABLE_TRANSACTIONS t
            INNER JOIN $TABLE_CATEGORIES c ON t.$KEY_CATEGORY_ID = c.$KEY_ID
            WHERE t.$KEY_USER_ID = ?
            ORDER BY t.$KEY_DATE DESC, t.$KEY_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val transaction = TransactionWithCategory(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_AMOUNT)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)),
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME)),
                    categoryColor = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_COLOR)),
                    categoryIcon = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_ICON))
                )
                transactions.add(transaction)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactions
    }

    fun getTransactionsByDateRange(startDate: String, endDate: String): List<TransactionWithCategory> {
        val transactions = mutableListOf<TransactionWithCategory>()
        val db = this.readableDatabase
        val query = """
            SELECT t.$KEY_ID, t.$KEY_AMOUNT, t.$KEY_TYPE, t.$KEY_DESCRIPTION, t.$KEY_DATE, 
                   c.$KEY_CATEGORY_NAME, c.$KEY_CATEGORY_COLOR, c.$KEY_CATEGORY_ICON
            FROM $TABLE_TRANSACTIONS t
            INNER JOIN $TABLE_CATEGORIES c ON t.$KEY_CATEGORY_ID = c.$KEY_ID
            WHERE t.$KEY_DATE BETWEEN ? AND ?
            ORDER BY t.$KEY_DATE DESC, t.$KEY_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))

        if (cursor.moveToFirst()) {
            do {
                val transaction = TransactionWithCategory(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_AMOUNT)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)),
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME)),
                    categoryColor = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_COLOR)),
                    categoryIcon = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_ICON))
                )
                transactions.add(transaction)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactions
    }

    fun getTransactionsByDateRangeForUser(userId: Long, startDate: String, endDate: String): List<TransactionWithCategory> {
        val transactions = mutableListOf<TransactionWithCategory>()
        val db = this.readableDatabase
        val query = """
            SELECT t.$KEY_ID, t.$KEY_AMOUNT, t.$KEY_TYPE, t.$KEY_DESCRIPTION, t.$KEY_DATE, 
                   c.$KEY_CATEGORY_NAME, c.$KEY_CATEGORY_COLOR, c.$KEY_CATEGORY_ICON
            FROM $TABLE_TRANSACTIONS t
            INNER JOIN $TABLE_CATEGORIES c ON t.$KEY_CATEGORY_ID = c.$KEY_ID
            WHERE t.$KEY_USER_ID = ? AND t.$KEY_DATE BETWEEN ? AND ?
            ORDER BY t.$KEY_DATE DESC, t.$KEY_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), startDate, endDate))

        if (cursor.moveToFirst()) {
            do {
                val transaction = TransactionWithCategory(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_AMOUNT)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)),
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME)),
                    categoryColor = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_COLOR)),
                    categoryIcon = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_ICON))
                )
                transactions.add(transaction)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactions
    }

    fun deleteTransaction(id: Long): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_TRANSACTIONS, "$KEY_ID=?", arrayOf(id.toString()))
    }

    // Analytics operations
    fun getMonthlySummary(monthYear: String): MonthlySummary {
        val db = this.readableDatabase
        val startDate = "$monthYear-01"
        val endDate = "$monthYear-31"
        
        // Get total income
        val incomeQuery = """
            SELECT SUM($KEY_AMOUNT) as total_income 
            FROM $TABLE_TRANSACTIONS 
            WHERE $KEY_TYPE = 'income' AND $KEY_DATE BETWEEN ? AND ?
        """.trimIndent()
        val incomeCursor = db.rawQuery(incomeQuery, arrayOf(startDate, endDate))
        val totalIncome = if (incomeCursor.moveToFirst()) {
            incomeCursor.getDouble(0)
        } else 0.0
        incomeCursor.close()

        // Get total expenses
        val expenseQuery = """
            SELECT SUM($KEY_AMOUNT) as total_expense 
            FROM $TABLE_TRANSACTIONS 
            WHERE $KEY_TYPE = 'expense' AND $KEY_DATE BETWEEN ? AND ?
        """.trimIndent()
        val expenseCursor = db.rawQuery(expenseQuery, arrayOf(startDate, endDate))
        val totalExpense = if (expenseCursor.moveToFirst()) {
            expenseCursor.getDouble(0)
        } else 0.0
        expenseCursor.close()

        return MonthlySummary(
            monthYear = monthYear,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense
        )
    }

    fun getCategoryWiseExpense(monthYear: String): List<CategoryExpense> {
        val categoryExpenses = mutableListOf<CategoryExpense>()
        val db = this.readableDatabase
        val startDate = "$monthYear-01"
        val endDate = "$monthYear-31"
        
        val query = """
            SELECT c.$KEY_CATEGORY_NAME, c.$KEY_CATEGORY_COLOR, c.$KEY_CATEGORY_ICON, 
                   SUM(t.$KEY_AMOUNT) as total_amount
            FROM $TABLE_TRANSACTIONS t
            INNER JOIN $TABLE_CATEGORIES c ON t.$KEY_CATEGORY_ID = c.$KEY_ID
            WHERE t.$KEY_TYPE = 'expense' AND t.$KEY_DATE BETWEEN ? AND ?
            GROUP BY c.$KEY_ID, c.$KEY_CATEGORY_NAME
            ORDER BY total_amount DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))

        if (cursor.moveToFirst()) {
            do {
                val categoryExpense = CategoryExpense(
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME)),
                    categoryColor = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_COLOR)),
                    categoryIcon = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_ICON)),
                    totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"))
                )
                categoryExpenses.add(categoryExpense)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return categoryExpenses
    }

    // Budget operations
    fun addBudget(budget: Budget): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_USER_ID, budget.userId)
            put(KEY_CATEGORY_ID, budget.categoryId)
            put(KEY_AMOUNT, budget.amount)
            put(KEY_MONTH_YEAR, budget.monthYear)
        }
        return db.insert(TABLE_BUDGETS, null, values)
    }

    fun getBudgetForMonth(monthYear: String): List<BudgetWithCategory> {
        val budgets = mutableListOf<BudgetWithCategory>()
        val db = this.readableDatabase
        val query = """
            SELECT b.$KEY_ID, b.$KEY_AMOUNT, b.$KEY_MONTH_YEAR,
                   c.$KEY_CATEGORY_NAME, c.$KEY_CATEGORY_COLOR, c.$KEY_CATEGORY_ICON
            FROM $TABLE_BUDGETS b
            INNER JOIN $TABLE_CATEGORIES c ON b.$KEY_CATEGORY_ID = c.$KEY_ID
            WHERE b.$KEY_MONTH_YEAR = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(monthYear))

        if (cursor.moveToFirst()) {
            do {
                val budget = BudgetWithCategory(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_AMOUNT)),
                    monthYear = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MONTH_YEAR)),
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME)),
                    categoryColor = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_COLOR)),
                    categoryIcon = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_ICON))
                )
                budgets.add(budget)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return budgets
    }

    fun getBudgetForMonthForUser(userId: Long, monthYear: String): List<BudgetWithCategory> {
        val budgets = mutableListOf<BudgetWithCategory>()
        val db = this.readableDatabase
        val query = """
            SELECT b.$KEY_ID, b.$KEY_AMOUNT, b.$KEY_MONTH_YEAR,
                   c.$KEY_CATEGORY_NAME, c.$KEY_CATEGORY_COLOR, c.$KEY_CATEGORY_ICON
            FROM $TABLE_BUDGETS b
            INNER JOIN $TABLE_CATEGORIES c ON b.$KEY_CATEGORY_ID = c.$KEY_ID
            WHERE b.$KEY_USER_ID = ? AND b.$KEY_MONTH_YEAR = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), monthYear))

        if (cursor.moveToFirst()) {
            do {
                val budget = BudgetWithCategory(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_AMOUNT)),
                    monthYear = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MONTH_YEAR)),
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME)),
                    categoryColor = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_COLOR)),
                    categoryIcon = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_ICON))
                )
                budgets.add(budget)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return budgets
    }

    fun getCurrentBalance(): Double {
        val db = this.readableDatabase
        
        // Get total income
        val incomeQuery = "SELECT SUM($KEY_AMOUNT) as total FROM $TABLE_TRANSACTIONS WHERE $KEY_TYPE = 'income'"
        val incomeCursor = db.rawQuery(incomeQuery, null)
        val totalIncome = if (incomeCursor.moveToFirst()) {
            incomeCursor.getDouble(0)
        } else 0.0
        incomeCursor.close()

        // Get total expenses
        val expenseQuery = "SELECT SUM($KEY_AMOUNT) as total FROM $TABLE_TRANSACTIONS WHERE $KEY_TYPE = 'expense'"
        val expenseCursor = db.rawQuery(expenseQuery, null)
        val totalExpense = if (expenseCursor.moveToFirst()) {
            expenseCursor.getDouble(0)
        } else 0.0
        expenseCursor.close()

        return totalIncome - totalExpense
    }

    fun getCurrentBalanceForUser(userId: Long): Double {
        val db = this.readableDatabase
        
        // Get total income
        val incomeQuery = "SELECT SUM($KEY_AMOUNT) as total FROM $TABLE_TRANSACTIONS WHERE $KEY_USER_ID = ? AND $KEY_TYPE = 'income'"
        val incomeCursor = db.rawQuery(incomeQuery, arrayOf(userId.toString()))
        val totalIncome = if (incomeCursor.moveToFirst()) {
            incomeCursor.getDouble(0)
        } else 0.0
        incomeCursor.close()

        // Get total expenses
        val expenseQuery = "SELECT SUM($KEY_AMOUNT) as total FROM $TABLE_TRANSACTIONS WHERE $KEY_USER_ID = ? AND $KEY_TYPE = 'expense'"
        val expenseCursor = db.rawQuery(expenseQuery, arrayOf(userId.toString()))
        val totalExpense = if (expenseCursor.moveToFirst()) {
            expenseCursor.getDouble(0)
        } else 0.0
        expenseCursor.close()

        return totalIncome - totalExpense
    }

    fun getMonthlySummaryForUser(userId: Long, monthYear: String): MonthlySummary {
        val db = this.readableDatabase
        val startDate = "$monthYear-01"
        val endDate = "$monthYear-31"
        
        // Get total income
        val incomeQuery = """
            SELECT SUM($KEY_AMOUNT) as total_income 
            FROM $TABLE_TRANSACTIONS 
            WHERE $KEY_USER_ID = ? AND $KEY_TYPE = 'income' AND $KEY_DATE BETWEEN ? AND ?
        """.trimIndent()
        val incomeCursor = db.rawQuery(incomeQuery, arrayOf(userId.toString(), startDate, endDate))
        val totalIncome = if (incomeCursor.moveToFirst()) {
            incomeCursor.getDouble(0)
        } else 0.0
        incomeCursor.close()

        // Get total expenses
        val expenseQuery = """
            SELECT SUM($KEY_AMOUNT) as total_expense 
            FROM $TABLE_TRANSACTIONS 
            WHERE $KEY_USER_ID = ? AND $KEY_TYPE = 'expense' AND $KEY_DATE BETWEEN ? AND ?
        """.trimIndent()
        val expenseCursor = db.rawQuery(expenseQuery, arrayOf(userId.toString(), startDate, endDate))
        val totalExpense = if (expenseCursor.moveToFirst()) {
            expenseCursor.getDouble(0)
        } else 0.0
        expenseCursor.close()

        return MonthlySummary(
            monthYear = monthYear,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense
        )
    }

    fun getCategoryWiseExpenseForUser(userId: Long, monthYear: String): List<CategoryExpense> {
        val categoryExpenses = mutableListOf<CategoryExpense>()
        val db = this.readableDatabase
        val startDate = "$monthYear-01"
        val endDate = "$monthYear-31"
        
        val query = """
            SELECT c.$KEY_CATEGORY_NAME, c.$KEY_CATEGORY_COLOR, c.$KEY_CATEGORY_ICON, 
                   SUM(t.$KEY_AMOUNT) as total_amount
            FROM $TABLE_TRANSACTIONS t
            INNER JOIN $TABLE_CATEGORIES c ON t.$KEY_CATEGORY_ID = c.$KEY_ID
            WHERE t.$KEY_USER_ID = ? AND t.$KEY_TYPE = 'expense' AND t.$KEY_DATE BETWEEN ? AND ?
            GROUP BY c.$KEY_ID, c.$KEY_CATEGORY_NAME
            ORDER BY total_amount DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), startDate, endDate))

        if (cursor.moveToFirst()) {
            do {
                val categoryExpense = CategoryExpense(
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME)),
                    categoryColor = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_COLOR)),
                    categoryIcon = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_ICON)),
                    totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"))
                )
                categoryExpenses.add(categoryExpense)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return categoryExpenses
    }
}