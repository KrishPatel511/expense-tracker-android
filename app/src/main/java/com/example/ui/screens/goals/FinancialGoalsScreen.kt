package com.example.ui.screens.goals

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoalEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialGoalsScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val goals by viewModel.allGoals.collectAsState()
    val currencySymbol = userProfile.currencySymbol

    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalForDeposit by remember { mutableStateOf<GoalEntity?>(null) }
    var goalToDelete by remember { mutableStateOf<GoalEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Financial Goals",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("goals_back_button")
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
                    showCreateGoalDialog = true
                },
                containerColor = Indigo600,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_goal")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { innerPadding ->
        if (goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Default.Savings,
                    title = "No financial goals yet",
                    description = "Create goals like saving for a vacation, new laptop, or emergency reserve.",
                    actionText = "Create Goal",
                    onAction = { showCreateGoalDialog = true }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(goals, key = { it.id }) { goal ->
                    val progress = (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                    val pct = goal.progressPercentage
                    val isCompleted = goal.isCompleted || goal.savedAmount >= goal.targetAmount
                    val remaining = goal.remainingAmount

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .border(1.dp, Slate100, RoundedCornerShape(22.dp)),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CategoryIconBadge(
                                        iconName = goal.iconName,
                                        colorHex = goal.colorHex,
                                        size = 42
                                    )
                                    Column {
                                        Text(
                                            text = goal.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Target: ${formatDate(goal.targetDateInMillis)}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 11.sp)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isCompleted) {
                                        StatusBadge(
                                            statusText = "COMPLETED 🎉",
                                            containerColor = IncomeGreenContainer,
                                            contentColor = IncomeGreenDark
                                        )
                                    } else {
                                        StatusBadge(
                                            statusText = "$pct%",
                                            containerColor = Indigo50,
                                            contentColor = Indigo600
                                        )
                                    }
                                    IconButton(
                                        onClick = { goalToDelete = goal },
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

                            if (goal.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = goal.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Saved", style = MaterialTheme.typography.labelSmall.copy(color = Slate400))
                                    Text(
                                        text = formatCurrency(goal.savedAmount, currencySymbol),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCompleted) IncomeGreenDark else Indigo600
                                        )
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Target", style = MaterialTheme.typography.labelSmall.copy(color = Slate400))
                                    Text(
                                        text = formatCurrency(goal.targetAmount, currencySymbol),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isCompleted) IncomeGreen else Indigo600,
                                trackColor = Slate100
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isCompleted) "Goal achieved!" else "${formatCurrency(remaining, currencySymbol)} left to reach goal",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isCompleted) IncomeGreenDark else Slate500,
                                        fontWeight = FontWeight.Medium
                                    )
                                )

                                Button(
                                    onClick = { selectedGoalForDeposit = goal },
                                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Money", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Money Dialog
        if (selectedGoalForDeposit != null) {
            DepositToGoalDialog(
                goal = selectedGoalForDeposit!!,
                currencySymbol = currencySymbol,
                onDismiss = { selectedGoalForDeposit = null },
                onDeposit = { amount ->
                    viewModel.addMoneyToGoal(selectedGoalForDeposit!!.id, amount)
                    selectedGoalForDeposit = null
                }
            )
        }

        // Create Goal Dialog
        if (showCreateGoalDialog) {
            CreateGoalDialog(
                onDismiss = { showCreateGoalDialog = false },
                onSave = { name, target, initial, dateMillis, desc, icon, colorHex ->
                    viewModel.addGoal(
                        name = name,
                        targetAmount = target,
                        savedAmount = initial,
                        targetDateInMillis = dateMillis,
                        description = desc,
                        iconName = icon,
                        colorHex = colorHex
                    )
                    showCreateGoalDialog = false
                }
            )
        }

        // Delete Goal Dialog
        if (goalToDelete != null) {
            DeleteConfirmationDialog(
                title = "Delete Goal?",
                message = "Are you sure you want to delete '${goalToDelete!!.name}'?",
                onConfirm = {
                    viewModel.deleteGoal(goalToDelete!!)
                    goalToDelete = null
                },
                onDismiss = { goalToDelete = null }
            )
        }
    }
}

@Composable
fun DepositToGoalDialog(
    goal: GoalEntity,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onDeposit: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Money to '${goal.name}'", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Current: ${formatCurrency(goal.savedAmount, currencySymbol)} / Target: ${formatCurrency(goal.targetAmount, currencySymbol)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amountText = it },
                    leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold, color = Indigo600) },
                    placeholder = { Text("Deposit amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMessage ?: "", color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        errorMessage = "Please enter an amount greater than 0."
                        return@Button
                    }
                    onDeposit(amt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Money", fontWeight = FontWeight.Bold)
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

@Composable
fun CreateGoalDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, target: Double, initial: Double, dateMillis: Long, desc: String, icon: String, colorHex: String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var initialText by remember { mutableStateOf("0") }
    var description by remember { mutableStateOf("") }
    var selectedDateMillis by remember {
        mutableStateOf(Calendar.getInstance().apply { add(Calendar.MONTH, 3) }.timeInMillis)
    }
    var selectedIcon by remember { mutableStateOf("savings") }
    var selectedColorHex by remember { mutableStateOf("#4F46E5") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val iconOptions = listOf("savings", "laptop", "flight", "directions_car", "home", "school", "beach_access", "store")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Financial Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g. MacBook Pro, Trip to Tokyo") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) targetText = it },
                        label = { Text("Target Amount") },
                        placeholder = { Text("50000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = initialText,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) initialText = it },
                        label = { Text("Initial Saved") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Date Selector
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                    selectedDateMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = Indigo600, modifier = Modifier.size(18.dp))
                        Text("Target: ${formatDate(selectedDateMillis)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Select Icon", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate600))
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(iconOptions) { iconName ->
                        val isSelected = selectedIcon == iconName
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Indigo50 else Slate100)
                                .border(if (isSelected) 2.dp else 0.dp, Indigo600, RoundedCornerShape(8.dp))
                                .clickable { selectedIcon = iconName },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(getCategoryIcon(iconName), contentDescription = null, tint = if (isSelected) Indigo600 else Slate600, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull()
                    val initial = initialText.toDoubleOrNull() ?: 0.0
                    if (name.isBlank()) {
                        errorMessage = "Please enter a goal title."
                        return@Button
                    }
                    if (target == null || target <= 0) {
                        errorMessage = "Please enter a valid target amount."
                        return@Button
                    }
                    onSave(name.trim(), target, initial, selectedDateMillis, description.trim(), selectedIcon, selectedColorHex)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Create Goal", fontWeight = FontWeight.Bold)
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
