package com.example.codvedx_t_2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "Cash", "Bank", "Credit Card", "Digital Wallet"
    val balance: Double,
    val initialBalance: Double,
    val colorHex: Int,
    val accountNumber: String? = null
)
