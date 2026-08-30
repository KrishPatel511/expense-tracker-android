package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class ExpenseRepository(
    private val database: AppDatabase,
    private val preferencesManager: PreferencesManager
) {
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val budgetDao = database.budgetDao()
    private val goalDao = database.goalDao()
    private val notificationDao = database.notificationDao()

    val userProfileFlow = preferencesManager.userProfileFlow

    // Flows
    val allTransactionsFlow: Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow()
    val recentTransactionsFlow: Flow<List<TransactionEntity>> = transactionDao.getRecentTransactionsFlow(10)
    val allCategoriesFlow: Flow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()
    val allGoalsFlow: Flow<List<GoalEntity>> = goalDao.getAllGoalsFlow()
    val allNotificationsFlow: Flow<List<NotificationEntity>> = notificationDao.getAllNotificationsFlow()
    val unreadNotificationCountFlow: Flow<Int> = notificationDao.getUnreadCountFlow()

    fun getBudgetsForMonthFlow(monthYear: String): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsForMonthFlow(monthYear)

    fun getOverallBudgetFlow(monthYear: String): Flow<BudgetEntity?> =
        budgetDao.getOverallBudgetFlow(monthYear)

    // Current Month String helper
    fun getCurrentMonthYear(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun initializeDefaultDataIfNeeded() {
        val categories = categoryDao.getAllCategoriesFlow().first()
        if (categories.isEmpty()) {
            val defaultCategories = listOf(
                // Expense Categories
                CategoryEntity(name = "Food & Dining", iconName = "fastfood", type = CategoryType.EXPENSE, colorHex = "#F97316", isDefault = true),
                CategoryEntity(name = "Groceries", iconName = "shopping_cart", type = CategoryType.EXPENSE, colorHex = "#EA580C", isDefault = true),
                CategoryEntity(name = "Shopping", iconName = "shopping_bag", type = CategoryType.EXPENSE, colorHex = "#EC4899", isDefault = true),
                CategoryEntity(name = "Transportation", iconName = "directions_car", type = CategoryType.EXPENSE, colorHex = "#3B82F6", isDefault = true),
                CategoryEntity(name = "Travel", iconName = "flight", type = CategoryType.EXPENSE, colorHex = "#06B6D4", isDefault = true),
                CategoryEntity(name = "Rent", iconName = "home", type = CategoryType.EXPENSE, colorHex = "#6366F1", isDefault = true),
                CategoryEntity(name = "Bills", iconName = "receipt_long", type = CategoryType.EXPENSE, colorHex = "#8B5CF6", isDefault = true),
                CategoryEntity(name = "Entertainment", iconName = "movie", type = CategoryType.EXPENSE, colorHex = "#A855F7", isDefault = true),
                CategoryEntity(name = "Health", iconName = "favorite", type = CategoryType.EXPENSE, colorHex = "#EF4444", isDefault = true),
                CategoryEntity(name = "Education", iconName = "school", type = CategoryType.EXPENSE, colorHex = "#14B8A6", isDefault = true),
                CategoryEntity(name = "Subscriptions", iconName = "subscriptions", type = CategoryType.EXPENSE, colorHex = "#6366F1", isDefault = true),
                CategoryEntity(name = "Other", iconName = "category", type = CategoryType.EXPENSE, colorHex = "#64748B", isDefault = true),

                // Income Categories
                CategoryEntity(name = "Salary", iconName = "account_balance_wallet", type = CategoryType.INCOME, colorHex = "#10B981", isDefault = true),
                CategoryEntity(name = "Freelance", iconName = "laptop", type = CategoryType.INCOME, colorHex = "#059669", isDefault = true),
                CategoryEntity(name = "Business", iconName = "store", type = CategoryType.INCOME, colorHex = "#0D9488", isDefault = true),
                CategoryEntity(name = "Investment", iconName = "trending_up", type = CategoryType.INCOME, colorHex = "#2563EB", isDefault = true),
                CategoryEntity(name = "Bonus", iconName = "card_giftcard", type = CategoryType.INCOME, colorHex = "#F59E0B", isDefault = true),
                CategoryEntity(name = "Gift", iconName = "redeem", type = CategoryType.INCOME, colorHex = "#D97706", isDefault = true),
                CategoryEntity(name = "Other", iconName = "payments", type = CategoryType.INCOME, colorHex = "#64748B", isDefault = true)
            )
            categoryDao.insertCategories(defaultCategories)
        }

        val transactions = transactionDao.getAllTransactionsFlow().first()
        val currentMonth = getCurrentMonthYear()
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        if (transactions.isEmpty()) {
            // Seed initial realistic sample transactions matching the high-density preview
            val sampleTransactions = listOf(
                TransactionEntity(
                    title = "Salary Credit",
                    amount = 50000.0,
                    type = TransactionType.INCOME,
                    categoryName = "Salary",
                    categoryIcon = "account_balance_wallet",
                    paymentMethod = PaymentMethod.BANK_ACCOUNT,
                    dateInMillis = now - (24 * 60 * 60 * 1000L),
                    timeString = "09:30 AM",
                    notes = "Monthly salary from HCL Technologies"
                ),
                TransactionEntity(
                    title = "Groceries & Vegetables",
                    amount = 1240.0,
                    type = TransactionType.EXPENSE,
                    categoryName = "Groceries",
                    categoryIcon = "shopping_cart",
                    paymentMethod = PaymentMethod.UPI,
                    dateInMillis = now - (2 * 60 * 60 * 1000L),
                    timeString = "11:15 AM",
                    notes = "Star Supermarket monthly groceries"
                ),
                TransactionEntity(
                    title = "Netflix India Subscription",
                    amount = 499.0,
                    type = TransactionType.EXPENSE,
                    categoryName = "Subscriptions",
                    categoryIcon = "subscriptions",
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    dateInMillis = now - (5 * 60 * 60 * 1000L),
                    timeString = "02:00 PM",
                    notes = "Premium 4K plan renewal"
                ),
                TransactionEntity(
                    title = "McDonald's Dinner",
                    amount = 450.0,
                    type = TransactionType.EXPENSE,
                    categoryName = "Food & Dining",
                    categoryIcon = "fastfood",
                    paymentMethod = PaymentMethod.UPI,
                    dateInMillis = now - (12 * 60 * 60 * 1000L),
                    timeString = "08:30 PM",
                    notes = "Meal with friends"
                ),
                TransactionEntity(
                    title = "House Rent",
                    amount = 15000.0,
                    type = TransactionType.EXPENSE,
                    categoryName = "Rent",
                    categoryIcon = "home",
                    paymentMethod = PaymentMethod.BANK_ACCOUNT,
                    dateInMillis = now - (3 * 24 * 60 * 60 * 1000L),
                    timeString = "10:00 AM",
                    notes = "Monthly flat rental payment"
                ),
                TransactionEntity(
                    title = "Electricity & Wi-Fi Bills",
                    amount = 2450.0,
                    type = TransactionType.EXPENSE,
                    categoryName = "Bills",
                    categoryIcon = "receipt_long",
                    paymentMethod = PaymentMethod.UPI,
                    dateInMillis = now - (4 * 24 * 60 * 60 * 1000L),
                    timeString = "04:15 PM",
                    notes = "Broadband and Power bill"
                ),
                TransactionEntity(
                    title = "Freelance UI Design",
                    amount = 8500.0,
                    type = TransactionType.INCOME,
                    categoryName = "Freelance",
                    categoryIcon = "laptop",
                    paymentMethod = PaymentMethod.BANK_ACCOUNT,
                    dateInMillis = now - (5 * 24 * 60 * 60 * 1000L),
                    timeString = "06:45 PM",
                    notes = "Mobile app wireframe sprint"
                ),
                TransactionEntity(
                    title = "Petrol & Fuel",
                    amount = 1800.0,
                    type = TransactionType.EXPENSE,
                    categoryName = "Transportation",
                    categoryIcon = "directions_car",
                    paymentMethod = PaymentMethod.DEBIT_CARD,
                    dateInMillis = now - (6 * 24 * 60 * 60 * 1000L),
                    timeString = "01:30 PM",
                    notes = "Car tank full"
                ),
                TransactionEntity(
                    title = "Weekend Movie Tickets",
                    amount = 720.0,
                    type = TransactionType.EXPENSE,
                    categoryName = "Entertainment",
                    categoryIcon = "movie",
                    paymentMethod = PaymentMethod.UPI,
                    dateInMillis = now - (7 * 24 * 60 * 60 * 1000L),
                    timeString = "07:00 PM",
                    notes = "IMAX 3D cinema"
                ),
                TransactionEntity(
                    title = "Clothing & Apparel",
                    amount = 2000.0,
                    type = TransactionType.EXPENSE,
                    categoryName = "Shopping",
                    categoryIcon = "shopping_bag",
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    dateInMillis = now - (8 * 24 * 60 * 60 * 1000L),
                    timeString = "05:00 PM",
                    notes = "Formal shirts and jeans"
                )
            )
            transactionDao.insertAll(sampleTransactions)
        }

        val budgets = budgetDao.getBudgetsForMonthFlow(currentMonth).first()
        if (budgets.isEmpty()) {
            budgetDao.insertOrUpdateBudget(
                BudgetEntity(categoryName = null, monthYear = currentMonth, limitAmount = 30000.0)
            )
            budgetDao.insertOrUpdateBudget(
                BudgetEntity(categoryName = "Food & Dining", monthYear = currentMonth, limitAmount = 5000.0)
            )
            budgetDao.insertOrUpdateBudget(
                BudgetEntity(categoryName = "Groceries", monthYear = currentMonth, limitAmount = 4000.0)
            )
            budgetDao.insertOrUpdateBudget(
                BudgetEntity(categoryName = "Shopping", monthYear = currentMonth, limitAmount = 3000.0)
            )
            budgetDao.insertOrUpdateBudget(
                BudgetEntity(categoryName = "Entertainment", monthYear = currentMonth, limitAmount = 2000.0)
            )
        }

        val goals = goalDao.getAllGoalsFlow().first()
        if (goals.isEmpty()) {
            val calGoal = Calendar.getInstance()
            calGoal.add(Calendar.MONTH, 3)
            goalDao.insertGoal(
                GoalEntity(
                    name = "Buy a MacBook Pro",
                    targetAmount = 120000.0,
                    savedAmount = 65000.0,
                    targetDateInMillis = calGoal.timeInMillis,
                    description = "M3 Pro MacBook for development",
                    iconName = "laptop",
                    colorHex = "#4F46E5"
                )
            )
            val calGoal2 = Calendar.getInstance()
            calGoal2.add(Calendar.MONTH, 6)
            goalDao.insertGoal(
                GoalEntity(
                    name = "Emergency Safety Fund",
                    targetAmount = 100000.0,
                    savedAmount = 45000.0,
                    targetDateInMillis = calGoal2.timeInMillis,
                    description = "6 months of living expenses reserve",
                    iconName = "security",
                    colorHex = "#10B981"
                )
            )
            val calGoal3 = Calendar.getInstance()
            calGoal3.add(Calendar.MONTH, 2)
            goalDao.insertGoal(
                GoalEntity(
                    name = "Goa Beach Vacation",
                    targetAmount = 25000.0,
                    savedAmount = 18500.0,
                    targetDateInMillis = calGoal3.timeInMillis,
                    description = "Weekend getaway trip with friends",
                    iconName = "flight",
                    colorHex = "#F59E0B"
                )
            )
        }

        val notifications = notificationDao.getAllNotificationsFlow().first()
        if (notifications.isEmpty()) {
            notificationDao.insertAll(
                listOf(
                    NotificationEntity(
                        title = "Monthly Budget Alert",
                        message = "You have used 60% of your overall monthly budget (₹18,000 of ₹30,000).",
                        type = NotificationType.BUDGET_ALERT,
                        dateInMillis = System.currentTimeMillis() - (1 * 60 * 60 * 1000L),
                        isRead = false
                    ),
                    NotificationEntity(
                        title = "Goal Progress Milestone",
                        message = "You are ₹6,500 away from achieving your Goa Beach Vacation goal!",
                        type = NotificationType.GOAL_REMINDER,
                        dateInMillis = System.currentTimeMillis() - (24 * 60 * 60 * 1000L),
                        isRead = false
                    ),
                    NotificationEntity(
                        title = "Monthly Expense Review",
                        message = "Review your financial breakdown and spending insights for this month.",
                        type = NotificationType.MONTHLY_REMINDER,
                        dateInMillis = System.currentTimeMillis() - (48 * 60 * 60 * 1000L),
                        isRead = true
                    )
                )
            )
        }
    }

    // Transaction CRUD
    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        val id = transactionDao.insertTransaction(transaction)
        checkAndTriggerBudgetAlerts(transaction)
        return id
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
        checkAndTriggerBudgetAlerts(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun getTransactionById(id: Long): TransactionEntity? =
        transactionDao.getTransactionById(id)

    // Category CRUD
    suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        categoryDao.deleteCategory(category)

    suspend fun getCategoryByNameAndType(name: String, type: CategoryType): CategoryEntity? =
        categoryDao.getCategoryByNameAndType(name, type)

    // Budget CRUD
    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long =
        budgetDao.insertOrUpdateBudget(budget)

    suspend fun deleteBudget(budget: BudgetEntity) =
        budgetDao.deleteBudget(budget)

    suspend fun deleteBudgetById(id: Long) =
        budgetDao.deleteBudgetById(id)

    // Goal CRUD
    suspend fun insertGoal(goal: GoalEntity): Long =
        goalDao.insertGoal(goal)

    suspend fun updateGoal(goal: GoalEntity) =
        goalDao.updateGoal(goal)

    suspend fun deleteGoal(goal: GoalEntity) =
        goalDao.deleteGoal(goal)

    suspend fun addMoneyToGoal(goalId: Long, amount: Double) {
        val goal = goalDao.getGoalById(goalId) ?: return
        val newSaved = goal.savedAmount + amount
        val isCompleted = newSaved >= goal.targetAmount
        val updated = goal.copy(
            savedAmount = newSaved,
            isCompleted = isCompleted
        )
        goalDao.updateGoal(updated)

        if (isCompleted) {
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Goal Achieved! 🎉",
                    message = "Congratulations! You reached your goal for '${goal.name}' (₹${goal.targetAmount.toLong()}).",
                    type = NotificationType.GOAL_REMINDER
                )
            )
        }
    }

    // Notifications
    suspend fun markNotificationAsRead(id: Long) = notificationDao.markAsRead(id)
    suspend fun markAllNotificationsAsRead() = notificationDao.markAllAsRead()
    suspend fun deleteNotificationById(id: Long) = notificationDao.deleteNotificationById(id)
    suspend fun clearAllNotifications() = notificationDao.deleteAllNotifications()

    // Profile & Preferences
    fun updateProfile(profile: UserProfile) = preferencesManager.updateUserProfile(profile)
    fun setOnboardingCompleted(completed: Boolean) = preferencesManager.setOnboardingCompleted(completed)
    fun setCurrency(code: String, symbol: String) = preferencesManager.setCurrency(code, symbol)
    fun setThemeMode(mode: String) = preferencesManager.setThemeMode(mode)
    fun setUserName(name: String) = preferencesManager.setUserName(name)

    // Data Management
    suspend fun deleteAllData() {
        transactionDao.deleteAllTransactions()
        budgetDao.deleteAllBudgets()
        goalDao.deleteAllGoals()
        notificationDao.deleteAllNotifications()
        categoryDao.deleteCustomCategories()
        preferencesManager.clearAllPreferences()
        initializeDefaultDataIfNeeded()
    }

    // Budget Alert Checks
    private suspend fun checkAndTriggerBudgetAlerts(transaction: TransactionEntity) {
        if (transaction.type != TransactionType.EXPENSE) return
        val currentMonth = getCurrentMonthYear()
        val allBudgets = budgetDao.getBudgetsForMonthFlow(currentMonth).first()
        val allTransactions = transactionDao.getAllTransactionsFlow().first()

        val monthTransactions = allTransactions.filter {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            sdf.format(Date(it.dateInMillis)) == currentMonth && it.type == TransactionType.EXPENSE
        }
        val totalSpentThisMonth = monthTransactions.sumOf { it.amount }

        allBudgets.forEach { budget ->
            if (budget.categoryName == null) {
                // Overall budget
                val ratio = totalSpentThisMonth / budget.limitAmount
                if (ratio >= 1.0) {
                    notificationDao.insertNotification(
                        NotificationEntity(
                            title = "Budget Exceeded! ⚠️",
                            message = "You have exceeded your overall monthly budget of ₹${budget.limitAmount.toLong()} (Total spent: ₹${totalSpentThisMonth.toLong()}).",
                            type = NotificationType.BUDGET_ALERT
                        )
                    )
                } else if (ratio >= 0.8) {
                    notificationDao.insertNotification(
                        NotificationEntity(
                            title = "Budget Warning ⚠️",
                            message = "You have used ${(ratio * 100).toInt()}% of your monthly budget.",
                            type = NotificationType.BUDGET_ALERT
                        )
                    )
                }
            } else {
                // Category budget
                val categorySpent = monthTransactions.filter { it.categoryName == budget.categoryName }.sumOf { it.amount }
                val ratio = categorySpent / budget.limitAmount
                if (ratio >= 1.0) {
                    notificationDao.insertNotification(
                        NotificationEntity(
                            title = "${budget.categoryName} Budget Exceeded! ⚠️",
                            message = "You have exceeded your ${budget.categoryName} budget of ₹${budget.limitAmount.toLong()}.",
                            type = NotificationType.BUDGET_ALERT
                        )
                    )
                } else if (ratio >= 0.8) {
                    notificationDao.insertNotification(
                        NotificationEntity(
                            title = "${budget.categoryName} Budget Warning ⚠️",
                            message = "You have used ${(ratio * 100).toInt()}% of your ${budget.categoryName} budget.",
                            type = NotificationType.BUDGET_ALERT
                        )
                    )
                }
            }
        }
    }
}
