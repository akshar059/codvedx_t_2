package com.example.codvedx_t_2.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.codvedx_t_2.data.local.AppDatabase
import com.example.codvedx_t_2.data.local.entity.CategoryEntity
import com.example.codvedx_t_2.data.preferences.UserPreferences
import com.example.codvedx_t_2.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseRepository(AppDatabase.getInstance(application))
    val userPreferences = UserPreferences(application)

    val allCategories = repository.allCategories.asLiveData()
    val allTransactions = repository.allTransactions.asLiveData()
    val totalExpense = repository.totalExpenseAllTime.asLiveData()

    fun updateMonthlyBudget(newBudget: Double) {
        userPreferences.monthlyBudget = newBudget
    }

    fun updateCategoryBudget(category: CategoryEntity, newBudget: Double) {
        viewModelScope.launch {
            repository.insertCategory(category.copy(budget = newBudget))
        }
    }
}
