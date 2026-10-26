package com.example.codvedx_t_2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val billingCycle: String, // "Monthly", "Yearly"
    val nextBillingDate: Long,
    val paymentMethod: String,
    val reminderDaysBefore: Int = 1
)
