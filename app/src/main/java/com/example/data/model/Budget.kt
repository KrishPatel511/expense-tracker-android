package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val categoryName: String? = null, // null indicates overall monthly budget
    val monthYear: String, // e.g. "2026-08"
    val limitAmount: Double
)
