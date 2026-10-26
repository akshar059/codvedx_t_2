package com.example.codvedx_t_2.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.codvedx_t_2.MainActivity
import com.example.codvedx_t_2.R
import com.example.codvedx_t_2.data.preferences.UserPreferences
import com.example.codvedx_t_2.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userPreferences = UserPreferences(this)

        binding.btnGetStarted.setOnClickListener {
            val name = binding.etUserName.text.toString().trim()
            val budgetStr = binding.etMonthlyBudget.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budget = budgetStr.toDoubleOrNull() ?: 30000.0

            var selectedCurrency = "₹"
            val checkedChipId = binding.chipGroupCurrency.checkedChipId
            when (checkedChipId) {
                R.id.chip_usd -> selectedCurrency = "$"
                R.id.chip_eur -> selectedCurrency = "€"
                R.id.chip_gbp -> selectedCurrency = "£"
                else -> selectedCurrency = "₹"
            }

            userPreferences.userName = name
            userPreferences.monthlyBudget = budget
            userPreferences.currencySymbol = selectedCurrency
            userPreferences.isOnboardingCompleted = true

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
