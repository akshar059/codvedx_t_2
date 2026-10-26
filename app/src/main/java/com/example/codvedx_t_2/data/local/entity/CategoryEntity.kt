package com.example.codvedx_t_2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val iconName: String,
    val colorHex: Int,
    val budget: Double = 0.0,
    val isDefault: Boolean = false
)
