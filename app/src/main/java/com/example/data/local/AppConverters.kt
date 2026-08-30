package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CategoryType
import com.example.data.model.NotificationType
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionType

class AppConverters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        try { TransactionType.valueOf(value) } catch (e: Exception) { TransactionType.EXPENSE }

    @TypeConverter
    fun fromCategoryType(value: CategoryType): String = value.name

    @TypeConverter
    fun toCategoryType(value: String): CategoryType =
        try { CategoryType.valueOf(value) } catch (e: Exception) { CategoryType.EXPENSE }

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod =
        try { PaymentMethod.valueOf(value) } catch (e: Exception) { PaymentMethod.OTHER }

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType =
        try { NotificationType.valueOf(value) } catch (e: Exception) { NotificationType.GENERAL }
}
