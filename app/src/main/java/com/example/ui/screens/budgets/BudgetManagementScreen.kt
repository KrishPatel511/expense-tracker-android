package com.example.ui.screens.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryType
import com.example.data.model.TransactionType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetManagementScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val monthlyBudgets by viewModel.monthlyBudgets.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()

    val currencySymbol = userProfile.currencySymbol
    val currentMonthYear = viewModel.currentMonthYear

    // Filter current month expenses
    val currentMonthExpenses = remember(allTransactions, currentMonthYear) {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        allTransactions.filter {
            sdf.format(Date(it.dateInMillis)) == currentMonthYear && it.type == TransactionType.EXPENSE
        }
    }

    val totalMonthSpent = remember(currentMonthExpenses) {
        currentMonthExpenses.sumOf { it.amount }
    }

    val overallBudget = remember(monthlyBudgets) {
        monthlyBudgets.find { it.categoryName == null }
    }

    val categoryBudgets = remember(monthlyBudgets) {
        monthlyBudgets.filter { it.categoryName != null }
    }

    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var selectedCategoryForBudget by remember { mutableStateOf<String?>(null) }
    var budgetToDelete by remember { mutableStateOf<BudgetEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Budget Management",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("budget_management_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedCategoryForBudget = null
                    showSetBudgetDialog = true
                },
                containerColor = Indigo600,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_budget")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Overall Monthly Budget Hero Card
            item {
                val overallLimit = overallBudget?.limitAmount ?: 30000.0
                val progress = (totalMonthSpent / overallLimit).toFloat().coerceIn(0f, 1f)
                val pct = (progress * 100).toInt()
                val remaining = (overallLimit - totalMonthSpent).coerceAtLeast(0.0)

                val statusColor = when {
                    pct >= 100 -> ExpenseRed
                    pct >= 80 -> AmberWarning
                    else -> IncomeGreen
                }
                val statusText = when {
                    pct >= 100 -> "Exceeded"
                    pct >= 80 -> "Warning"
                    else -> "Normal"
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, Slate100, RoundedCornerShape(24.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Overall Monthly Budget",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()),
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                                )
                            }
                            StatusBadge(
                                statusText = statusText,
                                containerColor = statusColor.copy(alpha = 0.15f),
                                contentColor = statusColor
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Total Spent",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = formatCurrency(totalMonthSpent, currencySymbol),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (pct >= 100) ExpenseRed else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Monthly Limit",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = formatCurrency(overallLimit, currencySymbol),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = statusColor,
                            trackColor = Slate100
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Remaining: ${formatCurrency(remaining, currencySymbol)} ($pct% used)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate600,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            TextButton(
                                onClick = {
                                    selectedCategoryForBudget = null
                                    showSetBudgetDialog = true
                                }
                            ) {
                                Text("Edit Budget", color = Indigo600, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Category Budgets Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Budgets",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    TextButton(
                        onClick = {
                            val availableCats = allCategories.filter { it.type == CategoryType.EXPENSE }
                            selectedCategoryForBudget = availableCats.firstOrNull()?.name
                            showSetBudgetDialog = true
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Category Budget", color = Indigo600, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Category Budgets List
            if (categoryBudgets.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate100)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No category budgets set",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Slate500)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Set budgets for specific categories like Food, Shopping, Groceries to track detailed spending limits.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(categoryBudgets, key = { it.id }) { budget ->
                    val catName = budget.categoryName ?: "Overall"
                    val catExpenses = currentMonthExpenses.filter { it.categoryName == catName }
                    val catSpent = catExpenses.sumOf { it.amount }
                    val catProgress = (catSpent / budget.limitAmount).toFloat().coerceIn(0f, 1f)
                    val catPct = (catProgress * 100).toInt()
                    val catRemaining = (budget.limitAmount - catSpent).coerceAtLeast(0.0)

                    val catStatusColor = when {
                        catPct >= 100 -> ExpenseRed
                        catPct >= 80 -> AmberWarning
                        else -> Indigo600
                    }

                    val catEntity = allCategories.find { it.name == catName }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, Slate100, RoundedCornerShape(18.dp)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CategoryIconBadge(
                                        iconName = catEntity?.iconName ?: "category",
                                        colorHex = catEntity?.colorHex ?: "#4F46E5",
                                        size = 36
                                    )
                                    Column {
                                        Text(
                                            text = catName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${formatCurrency(catSpent, currencySymbol)} of ${formatCurrency(budget.limitAmount, currencySymbol)}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontSize = 11.sp)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(
                                        statusText = "$catPct%",
                                        containerColor = catStatusColor.copy(alpha = 0.15f),
                                        contentColor = catStatusColor
                                    )
                                    IconButton(
                                        onClick = { budgetToDelete = budget },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = Slate400,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { catProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = catStatusColor,
                                trackColor = Slate100
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Remaining: ${formatCurrency(catRemaining, currencySymbol)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate500,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }

        // Set Budget Dialog
        if (showSetBudgetDialog) {
            SetBudgetDialog(
                initialCategory = selectedCategoryForBudget,
                categories = allCategories.filter { it.type == CategoryType.EXPENSE },
                currencySymbol = currencySymbol,
                onDismiss = { showSetBudgetDialog = false },
                onSave = { categoryName, amount ->
                    viewModel.setBudget(categoryName, amount)
                    showSetBudgetDialog = false
                }
            )
        }

        if (budgetToDelete != null) {
            DeleteConfirmationDialog(
                title = "Delete Budget?",
                message = "Are you sure you want to remove the budget for '${budgetToDelete!!.categoryName ?: "Overall"}'?",
                onConfirm = {
                    viewModel.deleteBudget(budgetToDelete!!)
                    budgetToDelete = null
                },
                onDismiss = { budgetToDelete = null }
            )
        }
    }
}

@Composable
fun SetBudgetDialog(
    initialCategory: String?,
    categories: List<com.example.data.model.CategoryEntity>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (categoryName: String?, amount: Double) -> Unit
) {
    var isOverall by remember { mutableStateOf(initialCategory == null) }
    var selectedCategory by remember { mutableStateOf(initialCategory ?: categories.firstOrNull()?.name ?: "Food & Dining") }
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isOverall) "Set Overall Monthly Budget" else "Set Category Budget",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Toggle between Overall vs Category
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate100)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isOverall) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { isOverall = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Overall",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isOverall) Indigo600 else Slate500
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isOverall) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { isOverall = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (!isOverall) Indigo600 else Slate500
                            )
                        )
                    }
                }

                if (!isOverall) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate600)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(selectedCategory, color = Slate800, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCategory = cat.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Monthly Budget Amount ($currencySymbol)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate600)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amountText = it },
                    placeholder = { Text("e.g. 30000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter a valid amount greater than 0."
                        return@Button
                    }
                    onSave(if (isOverall) null else selectedCategory, amount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Budget", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
