package com.example.data.local

import androidx.room.*
import com.example.data.model.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetsForMonthFlow(monthYear: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear AND categoryName IS NULL LIMIT 1")
    fun getOverallBudgetFlow(monthYear: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear AND categoryName = :categoryName LIMIT 1")
    suspend fun getBudgetForCategory(monthYear: String, categoryName: String): BudgetEntity?

    @Transaction
    suspend fun upsertBudget(budget: BudgetEntity): Long {
        val existing = if (budget.categoryName == null) {
            // Check if overall budget exists for this month
            // We can query directly or handle via unique index conflict.
            // Using REPLACE strategy can delete and re-insert which affects ID generation.
            null
        } else {
            getBudgetForCategory(budget.monthYear, budget.categoryName)
        }
        
        return if (existing != null && existing.id != 0L) {
            insertOrUpdateBudget(budget.copy(id = existing.id))
        } else {
            insertOrUpdateBudget(budget)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: Long)

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()
}
