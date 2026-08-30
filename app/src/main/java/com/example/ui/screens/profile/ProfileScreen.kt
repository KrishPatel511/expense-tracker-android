package com.example.ui.screens.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.model.SupportedCurrencies
import com.example.data.model.UserProfile
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ExpenseViewModel,
    onNavigateToCategories: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToGoals: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allGoals by viewModel.allGoals.collectAsState()
    val budgets by viewModel.monthlyBudgets.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showResetDataDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val initials = remember(userProfile.userName) {
        userProfile.userName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { "ME" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile & Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
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
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Card Header
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Indigo600),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Column {
                                Text(
                                    text = userProfile.userName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Offline Secure Account",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                                )
                            }
                        }

                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.testTag("button_edit_profile")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = Indigo600
                            )
                        }
                    }
                }
            }

            // Quick Stats Metric Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricBox(
                        title = "Transactions",
                        value = "${allTransactions.size}",
                        color = Indigo600,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Active Goals",
                        value = "${allGoals.size}",
                        color = PurpleAccent,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToGoals
                    )
                    MetricBox(
                        title = "Budgets",
                        value = "${budgets.size}",
                        color = AmberWarning,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToBudgets
                    )
                }
            }

            // Section: Preferences
            item {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate500
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate100)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.AttachMoney,
                            title = "Currency",
                            subtitle = "${userProfile.currencyCode} (${userProfile.currencySymbol})",
                            onClick = { showCurrencyDialog = true },
                            testTag = "settings_currency_row"
                        )
                        HorizontalDivider(color = Slate100, thickness = 1.dp)
                        SettingsRow(
                            icon = Icons.Default.DarkMode,
                            title = "Theme Mode",
                            subtitle = userProfile.themeMode.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() },
                            onClick = { showThemeDialog = true },
                            testTag = "settings_theme_row"
                        )
                        HorizontalDivider(color = Slate100, thickness = 1.dp)
                        SettingsRow(
                            icon = Icons.Default.Category,
                            title = "Manage Categories",
                            subtitle = "Custom expense and income categories",
                            onClick = onNavigateToCategories,
                            testTag = "settings_categories_row"
                        )
                    }
                }
            }

            // Section: Data & Backup
            item {
                Text(
                    text = "Data & Reports",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate500
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate100)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.FileDownload,
                            title = "Export to CSV",
                            subtitle = "Download or share all transaction data",
                            onClick = {
                                val csv = viewModel.exportTransactionsAsCsv()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                    type = "text/csv"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export Transactions CSV"))
                            },
                            testTag = "settings_export_csv"
                        )
                        HorizontalDivider(color = Slate100, thickness = 1.dp)
                        SettingsRow(
                            icon = Icons.Default.Assessment,
                            title = "Share Monthly Summary",
                            subtitle = "Send text report via messaging or email",
                            onClick = {
                                val report = viewModel.exportMonthlyReportSummary()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, report)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Monthly Report"))
                            },
                            testTag = "settings_export_report"
                        )
                        HorizontalDivider(color = Slate100, thickness = 1.dp)
                        SettingsRow(
                            icon = Icons.Default.DeleteForever,
                            title = "Reset All Data",
                            subtitle = "Clear all transactions, budgets, and goals",
                            iconColor = ExpenseRed,
                            onClick = { showResetDataDialog = true },
                            testTag = "settings_reset_data"
                        )
                    }
                }
            }

            // Section: App Info
            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate500
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate100)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Info,
                            title = "About Expense Tracker",
                            subtitle = "Version 1.0.0 • Offline-first secure storage",
                            onClick = { showAboutDialog = true },
                            testTag = "settings_about_row"
                        )
                    }
                }
            }
        }

        // Edit Profile Dialog
        if (showEditProfileDialog) {
            EditProfileDialog(
                currentProfile = userProfile,
                onDismiss = { showEditProfileDialog = false },
                onSave = { name ->
                    viewModel.setUserName(name)
                    showEditProfileDialog = false
                }
            )
        }

        // Currency Dialog
        if (showCurrencyDialog) {
            CurrencySelectionDialog(
                currentCode = userProfile.currencyCode,
                onDismiss = { showCurrencyDialog = false },
                onSelect = { code, symbol ->
                    viewModel.setCurrency(code, symbol)
                    showCurrencyDialog = false
                }
            )
        }

        // Theme Dialog
        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentTheme = userProfile.themeMode,
                onDismiss = { showThemeDialog = false },
                onSelect = { mode ->
                    viewModel.setThemeMode(mode)
                    showThemeDialog = false
                }
            )
        }

        // Reset Data Dialog
        if (showResetDataDialog) {
            DeleteConfirmationDialog(
                title = "Reset All Application Data?",
                message = "This will permanently delete all your transactions, budgets, goals, and history. Are you sure you want to proceed?",
                confirmText = "Reset Everything",
                onConfirm = {
                    viewModel.deleteAllData()
                    showResetDataDialog = false
                },
                onDismiss = { showResetDataDialog = false }
            )
        }

        // About Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = {
                    Text("Expense Tracker v1.0.0", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "A modern financial management app designed to help you track expenses, plan monthly budgets, monitor financial goals, and gain smart spending insights.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate600, lineHeight = 20.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "🔒 100% Offline & Private: Your financial data never leaves your device and is safely stored in local Room SQLite storage.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500, fontWeight = FontWeight.Medium)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAboutDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate100)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate400,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color = Indigo600,
    testTag: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate400,
                        fontSize = 11.sp
                    )
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Slate400,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun EditProfileDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (name: String) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.userName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim().ifEmpty { "John Doe" }) },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
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
fun CurrencySelectionDialog(
    currentCode: String,
    onDismiss: () -> Unit,
    onSelect: (code: String, symbol: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                SupportedCurrencies.forEach { currency ->
                    val isSelected = currentCode == currency.code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelect(currency.code, currency.symbol) }
                            .background(if (isSelected) Indigo50 else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${currency.name} (${currency.symbol})",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Indigo600 else Slate800
                            )
                        )
                        if (isSelected) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Indigo600)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val themes = listOf("SYSTEM" to "System Default", "LIGHT" to "Light Mode", "DARK" to "Dark Mode")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                themes.forEach { (key, label) ->
                    val isSelected = currentTheme.equals(key, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelect(key) }
                            .background(if (isSelected) Indigo50 else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Indigo600 else Slate800
                            )
                        )
                        if (isSelected) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Indigo600)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
