package com.simats.financetrack.auth

import android.content.Context
import com.simats.financetrack.models.User
import com.simats.financetrack.models.UserCredentials
import java.text.SimpleDateFormat
import java.util.*

class AuthManager(private val context: Context) {
    
    private val userSessionManager = UserSessionManager(context)
    
    /**
     * Authenticate user with username and password
     */
    fun authenticateUser(credentials: UserCredentials): User? {
        // First check SharedPreferences
        var user = userSessionManager.getStoredUserByUsername(credentials.username)
        
        // If not found in SharedPreferences, check database
        if (user == null) {
            try {
                val db = com.simats.financetrack.database.DatabaseHelper(context)
                user = db.getUserByUsername(credentials.username)
                
                // If found in database, also store in SharedPreferences for consistency
                if (user != null) {
                    userSessionManager.storeUser(user)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return if (user != null && userSessionManager.verifyPassword(user, credentials.password)) {
            user
        } else {
            null
        }
    }
    
    /**
     * Login user and create session
     */
    fun login(user: User): Boolean {
        return userSessionManager.createSession(user)
    }
    
    /**
     * Login with credentials
     */
    fun login(credentials: UserCredentials): Boolean {
        val user = authenticateUser(credentials)
        return user?.let { login(it) } ?: false
    }
    
    /**
     * Register new user
     */
    fun registerUser(username: String, email: String, password: String, firstName: String = "", lastName: String = ""): User? {
        // Check if username already exists
        if (userSessionManager.getStoredUserByUsername(username) != null) {
            return null // User already exists
        }
        
        val userId = userSessionManager.getNextUserId()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = dateFormat.format(Date())
        
        val user = User(
            id = userId,
            username = username,
            email = email,
            passwordHash = userSessionManager.hashPassword(password),
            firstName = firstName,
            lastName = lastName,
            createdAt = currentTime,
            isActive = true
        )
        
        // Store user in both SharedPreferences and SQLite database
        val storedInPrefs = userSessionManager.storeUser(user)
        val storedInDb = try {
            val db = com.simats.financetrack.database.DatabaseHelper(context)
            val dbUserId = db.addUser(user)
            dbUserId > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        
        return if (storedInPrefs && storedInDb) {
            user
        } else {
            null
        }
    }
    
    /**
     * Logout current user
     */
    fun logout() {
        userSessionManager.endSession()
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean = userSessionManager.hasActiveSession()
    
    /**
     * Get current user
     */
    fun getCurrentUser(): User? = userSessionManager.getCurrentUser()
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): Long = userSessionManager.getCurrentUserId()
    
    /**
     * Switch to different user
     */
    fun switchUser(userId: Long): Boolean = userSessionManager.switchUser(userId)
    
    /**
     * Get all users
     */
    fun getAllUsers(): List<User> = userSessionManager.getAllStoredUsers()
    
    /**
     * Check if any users exist
     */
    fun hasAnyUsers(): Boolean = userSessionManager.hasAnyUsers()
    
    /**
     * Get last logged in user
     */
    fun getLastLoggedUserId(): Long = userSessionManager.getLastLoggedUserId()
    
    /**
     * Update last activity time for session management
     */
    fun updateLastActivity() {
        userSessionManager.updateLastActivity()
    }
    
    /**
     * Update user profile
     */
    fun updateUserProfile(user: User): Boolean {
        return userSessionManager.updateUser(user)
    }
    
    /**
     * Change user password
     */
    fun changePassword(userId: Long, oldPassword: String, newPassword: String): Boolean {
        val user = userSessionManager.getStoredUser(userId) ?: return false
        if (!userSessionManager.verifyPassword(user, oldPassword)) return false
        
        val updatedUser = user.copy(passwordHash = userSessionManager.hashPassword(newPassword))
        return userSessionManager.updateUser(updatedUser)
    }
    
    /**
     * Delete user and all associated data
     */
    fun deleteUser(userId: Long): Boolean {
        return userSessionManager.deleteUser(userId)
    }
    
    /**
     * Check if this is first time setup
     */
    fun isFirstTime(): Boolean = !hasAnyUsers()
    
    /**
     * Clear all user data (for testing/debugging)
     */
    fun clearAllData() {
        userSessionManager.clearAllUsers()
    }
}