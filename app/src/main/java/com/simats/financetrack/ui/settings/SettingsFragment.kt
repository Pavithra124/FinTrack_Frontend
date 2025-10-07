package com.simats.financetrack.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.simats.financetrack.ui.auth.LoginActivity
import com.simats.financetrack.auth.AuthManager
import com.simats.financetrack.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        authManager = AuthManager(requireContext())
        
        setupUserInfo()
        setupClickListeners()
    }

    private fun setupUserInfo() {
        val currentUser = authManager.getCurrentUser()
        if (currentUser != null) {
            binding.tvUserName.text = currentUser.displayName
            binding.tvUserEmail.text = if (currentUser.email.isNotEmpty()) currentUser.email else "@${currentUser.username}"
            
            // Show user count
            val userCount = authManager.getAllUsers().size
            binding.tvUserCount.text = "$userCount user(s) on this device"
        }
    }

    private fun setupClickListeners() {
        binding.layoutEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
        
        binding.layoutSwitchUser.setOnClickListener {
            showUserSwitchDialog()
        }
        
        binding.layoutManageUsers.setOnClickListener {
            showManageUsersDialog()
        }
        
        binding.layoutBackupData.setOnClickListener {
            Toast.makeText(context, "Backup feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        binding.layoutAbout.setOnClickListener {
            showAboutDialog()
        }
        
        binding.layoutLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("About Finance Tracker")
            .setMessage("""
                Finance Tracker v1.0
                
                A simple, offline expense tracking application that helps you manage your finances.
                
                Features:
                • Track income and expenses
                • Categorize transactions
                • View spending analytics
                • Generate reports
                • Complete offline functionality
                
                All your data is stored securely on your device.
            """.trimIndent())
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showEditProfileDialog() {
        val currentUser = authManager.getCurrentUser() ?: return
        
        val dialogView = layoutInflater.inflate(com.simats.financetrack.R.layout.dialog_edit_profile, null)
        val etFirstName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.simats.financetrack.R.id.etFirstName)
        val etLastName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.simats.financetrack.R.id.etLastName)
        val etEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.simats.financetrack.R.id.etEmail)
        
        // Pre-fill current data
        etFirstName.setText(currentUser.firstName)
        etLastName.setText(currentUser.lastName)
        etEmail.setText(currentUser.email)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedUser = currentUser.copy(
                    firstName = etFirstName.text.toString().trim(),
                    lastName = etLastName.text.toString().trim(),
                    email = etEmail.text.toString().trim()
                )
                
                if (authManager.updateUserProfile(updatedUser)) {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    setupUserInfo() // Refresh display
                } else {
                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showUserSwitchDialog() {
        val users = authManager.getAllUsers()
        val currentUserId = authManager.getCurrentUserId()
        
        val userNames = users.map { user ->
            if (user.id == currentUserId) {
                "${user.displayName} (Current)"
            } else {
                user.displayName
            }
        }.toTypedArray()
        
        AlertDialog.Builder(requireContext())
            .setTitle("Switch User")
            .setItems(userNames) { _, which ->
                val selectedUser = users[which]
                if (selectedUser.id != currentUserId) {
                    if (authManager.switchUser(selectedUser.id)) {
                        Toast.makeText(context, "Switched to ${selectedUser.displayName}", Toast.LENGTH_SHORT).show()
                        requireActivity().finish()
                        startActivity(Intent(requireContext(), requireActivity()::class.java))
                    } else {
                        Toast.makeText(context, "Failed to switch user", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNeutralButton("Add New User") { _, _ ->
                startActivity(Intent(requireContext(), com.simats.financetrack.ui.auth.SignupActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showManageUsersDialog() {
        val users = authManager.getAllUsers()
        val currentUserId = authManager.getCurrentUserId()
        
        val userOptions = users.map { user ->
            if (user.id == currentUserId) {
                "${user.displayName} (Current)"
            } else {
                user.displayName
            }
        }.toTypedArray()
        
        AlertDialog.Builder(requireContext())
            .setTitle("Manage Users")
            .setItems(userOptions) { _, which ->
                val selectedUser = users[which]
                showUserActionsDialog(selectedUser, selectedUser.id == currentUserId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showUserActionsDialog(user: com.simats.financetrack.models.User, isCurrent: Boolean) {
        val actions = if (isCurrent) {
            arrayOf("View Details", "Change Password")
        } else {
            arrayOf("View Details", "Delete User")
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle(user.displayName)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> showUserDetailsDialog(user)
                    1 -> {
                        if (isCurrent) {
                            showChangePasswordDialog(user)
                        } else {
                            showDeleteUserDialog(user)
                        }
                    }
                }
            }
            .setNegativeButton("Back", null)
            .show()
    }
    
    private fun showUserDetailsDialog(user: com.simats.financetrack.models.User) {
        val details = """
            Username: ${user.username}
            Name: ${user.displayName}
            Email: ${user.email.ifEmpty { "Not provided" }}
            Account Created: ${user.createdAt}
            Last Login: ${user.lastLoginAt ?: "Never"}
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("User Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showChangePasswordDialog(user: com.simats.financetrack.models.User) {
        val dialogView = layoutInflater.inflate(com.simats.financetrack.R.layout.dialog_change_password, null)
        val etOldPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.simats.financetrack.R.id.etOldPassword)
        val etNewPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.simats.financetrack.R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.simats.financetrack.R.id.etConfirmPassword)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val oldPassword = etOldPassword.text.toString()
                val newPassword = etNewPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()
                
                when {
                    oldPassword.isEmpty() -> {
                        Toast.makeText(context, "Please enter current password", Toast.LENGTH_SHORT).show()
                    }
                    newPassword.length < 6 -> {
                        Toast.makeText(context, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    }
                    newPassword != confirmPassword -> {
                        Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        if (authManager.changePassword(user.id, oldPassword, newPassword)) {
                            Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Invalid current password", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteUserDialog(user: com.simats.financetrack.models.User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete ${user.displayName}'s account?\n\nThis will permanently delete all their data including transactions, budgets, and preferences.")
            .setPositiveButton("Delete") { _, _ ->
                if (authManager.deleteUser(user.id)) {
                    Toast.makeText(context, "User ${user.displayName} deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to delete user", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout? Your data will remain on this device.")
            .setPositiveButton("Logout") { _, _ ->
                authManager.logout()
                // Navigate to login screen
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}