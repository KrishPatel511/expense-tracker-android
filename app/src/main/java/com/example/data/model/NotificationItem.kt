package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NotificationType {
    BUDGET_ALERT,
    MONTHLY_REMINDER,
    GOAL_REMINDER,
    GENERAL
}

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val message: String,
    val type: NotificationType,
    val dateInMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
