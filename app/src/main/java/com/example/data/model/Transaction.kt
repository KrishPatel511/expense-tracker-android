package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    BANK_ACCOUNT("Bank Account"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    OTHER("Other")
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryName: String,
    val categoryIcon: String,
    val paymentMethod: PaymentMethod,
    val dateInMillis: Long,
    val timeString: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
