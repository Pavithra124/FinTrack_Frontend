package com.simats.financetrack

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root ScrollView
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.WHITE)
            setPadding(16, 16, 16, 16)
        }

        // Container LinearLayout (vertical)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Top bar
        container.addView(TextView(this).apply {
            text = "About and Guide"
            textSize = 20f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        })

        // App Icon
        container.addView(ImageView(this).apply {
            setImageResource(R.drawable.ic_launcher_foreground)
            layoutParams = LinearLayout.LayoutParams(64, 64).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        })

        // App Title
        container.addView(TextView(this).apply {
            text = "EXPENSE TRACK"
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        })

        // Subtitle
        container.addView(TextView(this).apply {
            text = "Smart Food Expense Management\nTrack, manage, and optimize your food expenses"
            textSize = 14f
            setTextColor(Color.parseColor("#666666"))
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 0)
        })

        // Section Title
        container.addView(TextView(this).apply {
            text = "Key Features"
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 24, 0, 0)
        })

        // Add Feature Card function
        fun addFeature(icon: Int, title: String, desc: String) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(Color.parseColor("#F5F5F5"))
                setPadding(12, 12, 12, 12)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8)
                }
            }

            val iconView = ImageView(this).apply {
                setImageResource(icon)
                layoutParams = LinearLayout.LayoutParams(24, 24).apply {
                    marginEnd = 12
                    gravity = Gravity.CENTER_VERTICAL
                }
            }

            val texts = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val titleView = TextView(this).apply {
                text = title
                textSize = 14f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val descView = TextView(this).apply {
                text = desc
                textSize = 12f
                setTextColor(Color.parseColor("#666666"))
            }

            texts.addView(titleView)
            texts.addView(descView)

            card.addView(iconView)
            card.addView(texts)
            container.addView(card)
        }

        // Add features
        addFeature(R.drawable.export, "Expense Tracking", "Track all your food related expenses in one place")
        addFeature(R.drawable.budget, "Budget Management", "Set and monitor your food budgets")
        addFeature(R.drawable.cameras, "Receipt Scanner", "Scan receipts for automatic expense entry")
        addFeature(R.drawable.cat, "Categories", "Organize expenses by custom categories")

        // Getting Started
        container.addView(TextView(this).apply {
            text = "Getting Started"
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 24, 0, 0)
        })

        container.addView(TextView(this).apply {
            text = "1. Create an Account\n2. Set Your Budget\n3. Add Expenses\n4. Track and Analyze"
            textSize = 14f
            setTextColor(Color.parseColor("#444444"))
            setPadding(0, 8, 0, 0)
        })

        // More Features
        container.addView(TextView(this).apply {
            text = "More Features"
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 24, 0, 0)
        })

        // Need Help
        container.addView(TextView(this).apply {
            text = "Need Help?"
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 24, 0, 0)
        })

        container.addView(TextView(this).apply {
            text = "FAQ\nEmail Support\nHelp Center\nVideo Tutorials"
            textSize = 14f
            setTextColor(Color.parseColor("#444444"))
            setPadding(0, 8, 0, 0)
        })

        // Version info
        container.addView(TextView(this).apply {
            text = "Version 2.1.0\nLast Updated: March 2024"
            textSize = 12f
            setTextColor(Color.parseColor("#888888"))
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        })

        // Footer links
        container.addView(TextView(this).apply {
            text = "Privacy Policy   Terms of Service   Data Usage"
            textSize = 12f
            setTextColor(Color.parseColor("#448AFF"))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        })

        // Attach container to scrollView
        scrollView.addView(container)
        setContentView(scrollView)
    }
}
