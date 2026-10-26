package com.example.codvedx_t_2.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.codvedx_t_2.data.local.AppDatabase
import com.example.codvedx_t_2.data.preferences.UserPreferences
import com.example.codvedx_t_2.data.repository.ExpenseRepository

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseRepository(AppDatabase.getInstance(application))
    val userPreferences = UserPreferences(application)

    val allTransactions = repository.allTransactions.asLiveData()
    val totalBalance = repository.totalBalance.asLiveData()
    val totalIncome = repository.totalIncomeAllTime.asLiveData()
    val totalExpense = repository.totalExpenseAllTime.asLiveData()
}
