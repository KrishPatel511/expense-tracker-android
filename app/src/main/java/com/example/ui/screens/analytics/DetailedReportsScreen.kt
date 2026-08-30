package com.example.ui.screens.analytics

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedReportsScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val monthlyBudgets by viewModel.monthlyBudgets.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    val currencySymbol = userProfile.currencySymbol
    val curMonthYear = viewModel.currentMonthYear
    val categoryMap = categories.associateBy { it.name }

    val monthTransactions = remember(allTransactions, curMonthYear) {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        allTransactions.filter { sdf.format(Date(it.dateInMillis)) == curMonthYear }
    }

    val totalIncome = remember(monthTransactions) {
        monthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val totalExpense = remember(monthTransactions) {
        monthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val netSavings = totalIncome - totalExpense

    // Payment Method Distribution
    val paymentDistribution = remember(monthTransactions) {
        monthTransactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.paymentMethod }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detailed Monthly Report",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("reports_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val reportText = viewModel.exportMonthlyReportSummary()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, reportText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Monthly Report"))
                        },
                        modifier = Modifier.testTag("reports_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Indigo600
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Month Header Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Indigo600
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Report Period",
                            style = MaterialTheme.typography.labelSmall.copy(color = Indigo100)
                        )
                        Text(
                            text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Income", color = Indigo100, fontSize = 11.sp)
                                Text(
                                    formatCurrency(totalIncome, currencySymbol),
                                    color = Color(0xFF86EFAC),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Column {
                                Text("Expenses", color = Indigo100, fontSize = 11.sp)
                                Text(
                                    formatCurrency(totalExpense, currencySymbol),
                                    color = Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Net Savings", color = Indigo100, fontSize = 11.sp)
                                Text(
                                    formatCurrency(netSavings, currencySymbol),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Payment Methods Breakdown
            item {
                Text(
                    text = "Spending by Payment Method",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(paymentDistribution.entries.toList()) { (method, amount) ->
                val pct = if (totalExpense > 0) ((amount / totalExpense) * 100).toInt() else 0
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Slate100, RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = Indigo600)
                            Text(text = method.displayName, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${formatCurrency(amount, currencySymbol)} ($pct%)",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Transactions Summary List
            item {
                Text(
                    text = "All Records for this Month (${monthTransactions.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(monthTransactions, key = { it.id }) { trans ->
                val isIncome = trans.type == TransactionType.INCOME
                val catEntity = categoryMap[trans.categoryName]

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Slate100, RoundedCornerShape(14.dp)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CategoryIconBadge(
                                iconName = trans.categoryIcon,
                                colorHex = catEntity?.colorHex ?: "#4F46E5",
                                size = 32
                            )
                            Column {
                                Text(trans.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    "${trans.categoryName} • ${trans.paymentMethod.displayName}",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                            }
                        }
                        Text(
                            text = if (isIncome) "+ ${formatCurrency(trans.amount, currencySymbol)}" else "- ${formatCurrency(trans.amount, currencySymbol)}",
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) IncomeGreen else ExpenseRed,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
