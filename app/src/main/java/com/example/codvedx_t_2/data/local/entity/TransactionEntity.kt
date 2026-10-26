package com.example.codvedx_t_2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val title: String,
    val amount: Double,
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: Int,
    val subcategory: String? = null,
    val accountId: Long,
    val accountName: String,
    val paymentMethod: String,
    val date: Long, // Epoch millis
    val notes: String? = null,
    val tags: String? = null,
    val receiptUri: String? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null, // "Daily", "Weekly", "Monthly", "Yearly"
    val createdDate: Long = System.currentTimeMillis(),
    val modifiedDate: Long = System.currentTimeMillis()
)
