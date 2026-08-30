package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val targetDateInMillis: Long,
    val description: String = "",
    val iconName: String = "laptop",
    val colorHex: String = "#4F46E5",
    val isCompleted: Boolean = false
) {
    val progressPercentage: Int
        get() = if (targetAmount > 0) ((savedAmount / targetAmount) * 100).toInt().coerceIn(0, 100) else 0

    val remainingAmount: Double
        get() = (targetAmount - savedAmount).coerceAtLeast(0.0)
}
