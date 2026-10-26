package com.example.codvedx_t_2.data.local.dao

import androidx.room.*
import com.example.codvedx_t_2.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND categoryId IS NULL LIMIT 1")
    fun getOverallBudget(month: Int, year: Int): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND categoryId IS NOT NULL")
    fun getCategoryBudgets(month: Int, year: Int): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateBudget(budget: BudgetEntity): Long

    @Delete
    fun deleteBudget(budget: BudgetEntity)
}
