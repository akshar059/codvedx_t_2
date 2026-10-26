package com.example.codvedx_t_2.data.local.dao

import androidx.room.*
import com.example.codvedx_t_2.data.local.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY nextBillingDate ASC")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubscription(subscription: SubscriptionEntity): Long

    @Update
    fun updateSubscription(subscription: SubscriptionEntity)

    @Delete
    fun deleteSubscription(subscription: SubscriptionEntity)
}
