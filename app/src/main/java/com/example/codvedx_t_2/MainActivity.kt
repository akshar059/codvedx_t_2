package com.example.codvedx_t_2

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.codvedx_t_2.data.local.AppDatabase
import com.example.codvedx_t_2.data.preferences.UserPreferences
import com.example.codvedx_t_2.data.repository.ExpenseRepository
import com.example.codvedx_t_2.databinding.ActivityMainBinding
import com.example.codvedx_t_2.ui.onboarding.OnboardingActivity
import com.example.codvedx_t_2.ui.transaction.AddEditTransactionBottomSheet
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        userPreferences = UserPreferences(this)

        // Check Onboarding status
        if (!userPreferences.isOnboardingCompleted) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.mainRoot.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        // Seed default database data
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val repository = ExpenseRepository(db)
            repository.ensureDefaultDataSeeded()
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        binding.fabAddTransaction.setOnClickListener {
            val bottomSheet = AddEditTransactionBottomSheet()
            bottomSheet.show(supportFragmentManager, "AddEditTransactionBottomSheet")
        }
    }
}
