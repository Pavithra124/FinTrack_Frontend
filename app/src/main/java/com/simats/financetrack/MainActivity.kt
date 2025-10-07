package com.simats.financetrack

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.simats.financetrack.auth.AuthManager
import com.simats.financetrack.databinding.ActivityMainBinding
import com.simats.financetrack.ui.dashboard.DashboardFragment
import com.simats.financetrack.ui.reports.ReportsFragment
import com.simats.financetrack.ui.transactions.TransactionsFragment
import com.simats.financetrack.ui.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)

        // Check if user is logged in
        if (!authManager.isLoggedIn()) {
            redirectToUserSelection()
            return
        }

        // Update last activity for session management
        authManager.updateLastActivity()
        
        setupBottomNavigation()
        
        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }
    }

    private fun redirectToUserSelection() {
        startActivity(Intent(this, com.simats.financetrack.ui.auth.LoginActivity::class.java))
        finish()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.nav_transactions -> {
                    loadFragment(TransactionsFragment())
                    true
                }
                R.id.nav_reports -> {
                    loadFragment(ReportsFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        
        // Check if session is still valid
        if (!authManager.isLoggedIn()) {
            redirectToUserSelection()
            return
        }
        
        // Update last activity for session management
        authManager.updateLastActivity()
    }

    override fun onBackPressed() {
        // If current fragment is not dashboard, go back to dashboard
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment !is DashboardFragment) {
            binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
        } else {
            super.onBackPressed()
        }
    }
}
