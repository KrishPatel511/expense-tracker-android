package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryType
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "fastfood", "food", "dining" -> Icons.Default.Fastfood
        "shopping_cart", "groceries", "grocery" -> Icons.Default.ShoppingCart
        "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
        "directions_car", "transportation", "car", "travel" -> Icons.Default.DirectionsCar
        "flight", "vacation" -> Icons.Default.Flight
        "home", "rent", "house" -> Icons.Default.Home
        "receipt_long", "bills", "bill" -> Icons.Default.ReceiptLong
        "movie", "entertainment" -> Icons.Default.Movie
        "favorite", "health", "medical" -> Icons.Default.Favorite
        "school", "education" -> Icons.Default.School
        "subscriptions", "subscription" -> Icons.Default.Subscriptions
        "account_balance_wallet", "wallet", "salary" -> Icons.Default.AccountBalanceWallet
        "laptop", "freelance", "work" -> Icons.Default.Laptop
        "store", "business" -> Icons.Default.Store
        "trending_up", "investment", "stocks" -> Icons.AutoMirrored.Filled.TrendingUp
        "card_giftcard", "bonus" -> Icons.Default.CardGiftcard
        "redeem", "gift" -> Icons.Default.Redeem
        "payments", "payment" -> Icons.Default.Payments
        "fitness_center", "gym" -> Icons.Default.FitnessCenter
        "pets", "pet" -> Icons.Default.Pets
        "security", "emergency" -> Icons.Default.Security
        "phone_android", "phone" -> Icons.Default.PhoneAndroid
        "build", "maintenance" -> Icons.Default.Build
        "beach_access", "trip" -> Icons.Default.BeachAccess
        "local_gas_station", "fuel" -> Icons.Default.LocalGasStation
        "local_cafe", "coffee" -> Icons.Default.LocalCafe
        "credit_card" -> Icons.Default.CreditCard
        else -> Icons.Default.Category
    }
}

fun parseColorHex(hex: String, defaultColor: Color = Indigo600): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        if (cleanHex.length == 6) {
            Color(android.graphics.Color.parseColor("#$cleanHex"))
        } else if (cleanHex.length == 8) {
            Color(android.graphics.Color.parseColor("#$cleanHex"))
        } else {
            defaultColor
        }
    } catch (e: Exception) {
        defaultColor
    }
}

fun formatCurrency(amount: Double, symbol: String = "₹"): String {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    formatter.maximumFractionDigits = 2
    formatter.minimumFractionDigits = 0
    return "$symbol${formatter.format(amount)}"
}

fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun formatTime(millis: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Composable
fun CategoryIconBadge(
    iconName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Int = 42
) {
    val baseColor = parseColorHex(colorHex)
    val containerColor = baseColor.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getCategoryIcon(iconName),
            contentDescription = null,
            tint = baseColor,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}

@Composable
fun HighDensityTransactionRow(
    transaction: TransactionEntity,
    currencySymbol: String,
    colorHex: String = "#4F46E5",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) IncomeGreen else Slate800
    val amountPrefix = if (isIncome) "+ " else "- "

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, Slate100, RoundedCornerShape(16.dp))
            .testTag("transaction_item_${transaction.id}"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                CategoryIconBadge(
                    iconName = transaction.categoryIcon,
                    colorHex = colorHex,
                    size = 42
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${transaction.categoryName} • ${transaction.paymentMethod.displayName} • ${formatDate(transaction.dateInMillis)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate400,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$amountPrefix${formatCurrency(transaction.amount, currencySymbol)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) IncomeGreen else MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun StatusBadge(
    statusText: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = contentColor,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Indigo50),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Indigo600,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Slate500,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                modifier = Modifier.testTag("empty_state_action_button")
            ) {
                Text(text = actionText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Delete",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("dialog_confirm_delete_button")
            ) {
                Text(confirmText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("dialog_cancel_button")
            ) {
                Text("Cancel", color = Slate600)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
