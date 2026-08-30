package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CategoryType {
    EXPENSE,
    INCOME
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val iconName: String,
    val type: CategoryType,
    val colorHex: String = "#4F46E5",
    val isDefault: Boolean = false
)
