package com.example.codvedx_t_2.ui.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.codvedx_t_2.data.local.AppDatabase
import com.example.codvedx_t_2.data.local.entity.TransactionEntity
import com.example.codvedx_t_2.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseRepository(AppDatabase.getInstance(application))

    val allTransactions = repository.allTransactions.asLiveData()
    val allCategories = repository.allCategories.asLiveData()
    val allAccounts = repository.allAccounts.asLiveData()

    fun saveTransaction(
        id: Long = 0,
        type: String,
        title: String,
        amount: Double,
        categoryId: Long,
        categoryName: String,
        categoryIcon: String,
        categoryColor: Int,
        accountId: Long,
        accountName: String,
        paymentMethod: String,
        date: Long,
        notes: String?,
        tags: String?,
        isRecurring: Boolean,
        oldTransaction: TransactionEntity? = null
    ) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                id = id,
                type = type,
                title = title,
                amount = amount,
                categoryId = categoryId,
                categoryName = categoryName,
                categoryIcon = categoryIcon,
                categoryColor = categoryColor,
                accountId = accountId,
                accountName = accountName,
                paymentMethod = paymentMethod,
                date = date,
                notes = notes,
                tags = tags,
                isRecurring = isRecurring
            )

            if (oldTransaction != null) {
                repository.updateTransaction(transaction, oldTransaction)
            } else {
                repository.insertTransaction(transaction)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}
