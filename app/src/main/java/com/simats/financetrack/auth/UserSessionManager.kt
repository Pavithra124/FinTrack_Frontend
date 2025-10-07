package com.simats.financetrack.auth

import android.content.Context
import android.content.SharedPreferences
import com.simats.financetrack.models.User
import com.simats.financetrack.models.UserSession
import com.simats.financetrack.database.DatabaseHelper
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class UserSessionManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val usersPrefs: SharedPreferences = context.getSharedPreferences(USERS_PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "user_session_prefs"
        private const val USERS_PREFS_NAME = "users_data_prefs"
        
        // Session Keys
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_LAST_ACTIVITY = "last_activity"
        private const val KEY_IS_SESSION_ACTIVE = "is_session_active"
        private const val KEY_REMEMBER_LAST_USER = "remember_last_user"
        private const val KEY_LAST_LOGGED_USER_ID = "last_logged_user_id"
        
        // Session timeout (24 hours in milliseconds)
        private const val SESSION_TIMEOUT = 24 * 60 * 60 * 1000L
        
        // User data keys prefix
        private const val USER_DATA_PREFIX = "user_"
        private const val USER_USERNAME_SUFFIX = "_username"
        private const val USER_EMAIL_SUFFIX = "_email"
        private const val USER_PASSWORD_HASH_SUFFIX = "_password_hash"
        private const val USER_FIRST_NAME_SUFFIX = "_first_name"
        private const val USER_LAST_NAME_SUFFIX = "_last_name"
        private const val USER_PROFILE_IMAGE_SUFFIX = "_profile_image"
        private const val USER_CREATED_AT_SUFFIX = "_created_at"
        private const val USER_LAST_LOGIN_SUFFIX = "_last_login"
        private const val USER_IS_ACTIVE_SUFFIX = "_is_active"
        private const val USER_LIST_KEY = "user_list"
    }
    
    /**
     * Create a new user session
     */
    fun createSession(user: User): Boolean {
        return try {
            val currentTime = System.currentTimeMillis()
            val sessionToken = generateSessionToken(user.id, currentTime)
            
            prefs.edit().apply {
                putLong(KEY_CURRENT_USER_ID, user.id)
                putString(KEY_SESSION_TOKEN, sessionToken)
                putLong(KEY_LOGIN_TIME, currentTime)
                putLong(KEY_LAST_ACTIVITY, currentTime)
                putBoolean(KEY_IS_SESSION_ACTIVE, true)
                putLong(KEY_LAST_LOGGED_USER_ID, user.id)
                apply()
            }
            
            // Update user's last login time
            updateUserLastLogin(user.id, currentTime)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Get current active session
     */
    fun getCurrentSession(): UserSession? {
        if (!hasActiveSession()) return null
        
        val userId = prefs.getLong(KEY_CURRENT_USER_ID, 0)
        val user = getStoredUser(userId) ?: return null
        
        return UserSession(
            userId = userId,
            username = user.username,
            loginTime = prefs.getLong(KEY_LOGIN_TIME, 0),
            lastActivityTime = prefs.getLong(KEY_LAST_ACTIVITY, 0),
            isActive = prefs.getBoolean(KEY_IS_SESSION_ACTIVE, false)
        )
    }
    
    /**
     * Check if there's an active session
     */
    fun hasActiveSession(): Boolean {
        val isActive = prefs.getBoolean(KEY_IS_SESSION_ACTIVE, false)
        val lastActivity = prefs.getLong(KEY_LAST_ACTIVITY, 0)
        val currentTime = System.currentTimeMillis()
        
        // Check if session is expired
        if (isActive && (currentTime - lastActivity) > SESSION_TIMEOUT) {
            expireSession()
            return false
        }
        
        return isActive && prefs.getLong(KEY_CURRENT_USER_ID, 0) > 0
    }
    
    /**
     * Switch to a different user
     */
    fun switchUser(userId: Long): Boolean {
        val user = getStoredUser(userId) ?: return false
        endSession()
        return createSession(user)
    }
    
    /**
     * End current session
     */
    fun endSession() {
        prefs.edit().apply {
            putBoolean(KEY_IS_SESSION_ACTIVE, false)
            remove(KEY_CURRENT_USER_ID)
            remove(KEY_SESSION_TOKEN)
            remove(KEY_LOGIN_TIME)
            remove(KEY_LAST_ACTIVITY)
            apply()
        }
    }
    
    /**
     * Expire session due to timeout
     */
    private fun expireSession() {
        prefs.edit().putBoolean(KEY_IS_SESSION_ACTIVE, false).apply()
    }
    
    /**
     * Store user data in SharedPreferences
     */
    fun storeUser(user: User): Boolean {
        return try {
            val userIds = getUserIds().toMutableSet()
            userIds.add(user.id)
            
            usersPrefs.edit().apply {
                // Store user data
                putString("$USER_DATA_PREFIX${user.id}$USER_USERNAME_SUFFIX", user.username)
                putString("$USER_DATA_PREFIX${user.id}$USER_EMAIL_SUFFIX", user.email)
                putString("$USER_DATA_PREFIX${user.id}$USER_PASSWORD_HASH_SUFFIX", user.passwordHash)
                putString("$USER_DATA_PREFIX${user.id}$USER_FIRST_NAME_SUFFIX", user.firstName)
                putString("$USER_DATA_PREFIX${user.id}$USER_LAST_NAME_SUFFIX", user.lastName)
                putString("$USER_DATA_PREFIX${user.id}$USER_PROFILE_IMAGE_SUFFIX", user.profileImage ?: "")
                putString("$USER_DATA_PREFIX${user.id}$USER_CREATED_AT_SUFFIX", user.createdAt)
                putString("$USER_DATA_PREFIX${user.id}$USER_LAST_LOGIN_SUFFIX", user.lastLoginAt ?: "")
                putBoolean("$USER_DATA_PREFIX${user.id}$USER_IS_ACTIVE_SUFFIX", user.isActive)
                
                // Update user list
                putStringSet(USER_LIST_KEY, userIds.map { it.toString() }.toSet())
                apply()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Get stored user by ID
     */
    fun getStoredUser(userId: Long): User? {
        return try {
            if (!getUserIds().contains(userId)) return null
            
            User(
                id = userId,
                username = usersPrefs.getString("$USER_DATA_PREFIX${userId}$USER_USERNAME_SUFFIX", "") ?: "",
                email = usersPrefs.getString("$USER_DATA_PREFIX${userId}$USER_EMAIL_SUFFIX", "") ?: "",
                passwordHash = usersPrefs.getString("$USER_DATA_PREFIX${userId}$USER_PASSWORD_HASH_SUFFIX", "") ?: "",
                firstName = usersPrefs.getString("$USER_DATA_PREFIX${userId}$USER_FIRST_NAME_SUFFIX", "") ?: "",
                lastName = usersPrefs.getString("$USER_DATA_PREFIX${userId}$USER_LAST_NAME_SUFFIX", "") ?: "",
                profileImage = usersPrefs.getString("$USER_DATA_PREFIX${userId}$USER_PROFILE_IMAGE_SUFFIX", "")?.let { if (it.isEmpty()) null else it },
                createdAt = usersPrefs.getString("$USER_DATA_PREFIX${userId}$USER_CREATED_AT_SUFFIX", "") ?: "",
                lastLoginAt = usersPrefs.getString("$USER_DATA_PREFIX${userId}$USER_LAST_LOGIN_SUFFIX", "")?.let { if (it.isEmpty()) null else it },
                isActive = usersPrefs.getBoolean("$USER_DATA_PREFIX${userId}$USER_IS_ACTIVE_SUFFIX", true)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get all stored users
     */
    fun getAllStoredUsers(): List<User> {
        return getUserIds().mapNotNull { getStoredUser(it) }
    }
    
    /**
     * Get stored user by username
     */
    fun getStoredUserByUsername(username: String): User? {
        return getAllStoredUsers().find { it.username.equals(username, ignoreCase = true) }
    }
    
    /**
     * Verify user password
     */
    fun verifyPassword(user: User, password: String): Boolean {
        val hashedPassword = hashPassword(password)
        return user.passwordHash == hashedPassword
    }
    
    /**
     * Hash password using SHA-256
     */
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): Long = prefs.getLong(KEY_CURRENT_USER_ID, 0)
    
    /**
     * Get current user
     */
    fun getCurrentUser(): User? {
        val userId = getCurrentUserId()
        return if (userId > 0) getStoredUser(userId) else null
    }
    
    /**
     * Get last logged in user ID
     */
    fun getLastLoggedUserId(): Long = prefs.getLong(KEY_LAST_LOGGED_USER_ID, 0)
    
    /**
     * Check if any users exist
     */
    fun hasAnyUsers(): Boolean = getUserIds().isNotEmpty()
    
    /**
     * Update last activity time for session management
     */
    fun updateLastActivity() {
        if (hasActiveSession()) {
            val currentTime = System.currentTimeMillis()
            prefs.edit().putLong(KEY_LAST_ACTIVITY, currentTime).apply()
        }
    }
    
    /**
     * Update user information
     */
    fun updateUser(user: User): Boolean {
        return try {
            val db = DatabaseHelper(context)
            val result = db.updateUser(user)
            result > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Delete user and all associated data
     */
    fun deleteUser(userId: Long): Boolean {
        return try {
            val userIds = getUserIds().toMutableSet()
            userIds.remove(userId)
            
            // Remove current session if it's this user
            if (getCurrentUserId() == userId) {
                endSession()
            }
            
            usersPrefs.edit().apply {
                // Remove all user data
                remove("$USER_DATA_PREFIX${userId}$USER_USERNAME_SUFFIX")
                remove("$USER_DATA_PREFIX${userId}$USER_EMAIL_SUFFIX")
                remove("$USER_DATA_PREFIX${userId}$USER_PASSWORD_HASH_SUFFIX")
                remove("$USER_DATA_PREFIX${userId}$USER_FIRST_NAME_SUFFIX")
                remove("$USER_DATA_PREFIX${userId}$USER_LAST_NAME_SUFFIX")
                remove("$USER_DATA_PREFIX${userId}$USER_PROFILE_IMAGE_SUFFIX")
                remove("$USER_DATA_PREFIX${userId}$USER_CREATED_AT_SUFFIX")
                remove("$USER_DATA_PREFIX${userId}$USER_LAST_LOGIN_SUFFIX")
                remove("$USER_DATA_PREFIX${userId}$USER_IS_ACTIVE_SUFFIX")
                
                // Update user list
                putStringSet(USER_LIST_KEY, userIds.map { it.toString() }.toSet())
                apply()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Get next available user ID
     */
    fun getNextUserId(): Long {
        val existingIds = getUserIds()
        return if (existingIds.isEmpty()) 1L else existingIds.maxOrNull()!! + 1L
    }
    
    /**
     * Clear all user data
     */
    fun clearAllUsers() {
        endSession()
        usersPrefs.edit().clear().apply()
    }
    
    /**
     * Generate session token
     */
    private fun generateSessionToken(userId: Long, loginTime: Long): String {
        val data = "$userId-$loginTime-${System.currentTimeMillis()}"
        return hashPassword(data)
    }
    
    /**
     * Update user's last login time
     */
    private fun updateUserLastLogin(userId: Long, loginTime: Long) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val loginTimeStr = dateFormat.format(Date(loginTime))
        
        usersPrefs.edit()
            .putString("$USER_DATA_PREFIX${userId}$USER_LAST_LOGIN_SUFFIX", loginTimeStr)
            .apply()
    }
    
    /**
     * Get all user IDs
     */
    private fun getUserIds(): Set<Long> {
        val userIdsSet = usersPrefs.getStringSet(USER_LIST_KEY, emptySet()) ?: emptySet()
        return userIdsSet.mapNotNull { it.toLongOrNull() }.toSet()
    }
}