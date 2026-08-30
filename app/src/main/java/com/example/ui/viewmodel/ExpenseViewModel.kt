package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.*
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

enum class DateRangeFilter(val displayName: String) {
    ALL("All Time"),
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year")
}

enum class SortOption(val displayName: String) {
    LATEST_FIRST("Latest First"),
    OLDEST_FIRST("Oldest First"),
    HIGHEST_AMOUNT("Highest Amount"),
    LOWEST_AMOUNT("Lowest Amount")
}

enum class AnalyticsTimeframe(val displayName: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
    ALL_TIME("All Time")
}

data class CategorySpend(
    val categoryName: String,
    val amount: Double,
    val percentage: Float,
    val colorHex: String,
    val iconName: String,
    val transactionCount: Int
)

data class TrendPoint(
    val label: String,
    val expense: Double,
    val income: Double
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val preferencesManager = PreferencesManager(application)
    val repository = ExpenseRepository(database, preferencesManager)

    val userProfile: StateFlow<UserProfile> = repository.userProfileFlow
    val allTransactions: StateFlow<List<TransactionEntity>> =
        repository.allTransactionsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentTransactions: StateFlow<List<TransactionEntity>> =
        repository.recentTransactionsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCategories: StateFlow<List<CategoryEntity>> =
        repository.allCategoriesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allGoals: StateFlow<List<GoalEntity>> =
        repository.allGoalsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allNotifications: StateFlow<List<NotificationEntity>> =
        repository.allNotificationsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val unreadNotificationCount: StateFlow<Int> =
        repository.unreadNotificationCountFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentMonthYear = repository.getCurrentMonthYear()
    val monthlyBudgets: StateFlow<List<BudgetEntity>> =
        repository.getBudgetsForMonthFlow(currentMonthYear).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search & Filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedTypeFilter: StateFlow<TransactionType?> = _selectedTypeFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedPaymentFilter = MutableStateFlow<PaymentMethod?>(null)
    val selectedPaymentFilter: StateFlow<PaymentMethod?> = _selectedPaymentFilter.asStateFlow()

    private val _selectedDateRangeFilter = MutableStateFlow(DateRangeFilter.ALL)
    val selectedDateRangeFilter: StateFlow<DateRangeFilter> = _selectedDateRangeFilter.asStateFlow()

    private val _selectedSortOption = MutableStateFlow(SortOption.LATEST_FIRST)
    val selectedSortOption: StateFlow<SortOption> = _selectedSortOption.asStateFlow()

    private val _minAmountFilter = MutableStateFlow<Double?>(null)
    val minAmountFilter: StateFlow<Double?> = _minAmountFilter.asStateFlow()

    private val _maxAmountFilter = MutableStateFlow<Double?>(null)
    val maxAmountFilter: StateFlow<Double?> = _maxAmountFilter.asStateFlow()

    // Analytics Timeframe State
    private val _analyticsTimeframe = MutableStateFlow(AnalyticsTimeframe.MONTHLY)
    val analyticsTimeframe: StateFlow<AnalyticsTimeframe> = _analyticsTimeframe.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
        }
    }

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        searchQuery,
        selectedTypeFilter,
        selectedCategoryFilter,
        selectedPaymentFilter,
        selectedDateRangeFilter,
        selectedSortOption
    ) { params ->
        val list = params[0] as List<TransactionEntity>
        val query = params[1] as String
        val type = params[2] as TransactionType?
        val category = params[3] as String?
        val payment = params[4] as PaymentMethod?
        val dateRange = params[5] as DateRangeFilter
        val sort = params[6] as SortOption

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        list.filter { item ->
            // Search Query
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.categoryName.contains(query, ignoreCase = true) ||
                    item.notes.contains(query, ignoreCase = true) ||
                    item.paymentMethod.displayName.contains(query, ignoreCase = true)

            // Type
            val matchesType = type == null || item.type == type

            // Category
            val matchesCategory = category == null || item.categoryName == category

            // Payment
            val matchesPayment = payment == null || item.paymentMethod == payment

            // Date Range
            val matchesDate = when (dateRange) {
                DateRangeFilter.ALL -> true
                DateRangeFilter.TODAY -> {
                    val itemCal = Calendar.getInstance().apply { timeInMillis = item.dateInMillis }
                    cal.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == itemCal.get(Calendar.DAY_OF_YEAR)
                }
                DateRangeFilter.YESTERDAY -> {
                    val yestCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                    val itemCal = Calendar.getInstance().apply { timeInMillis = item.dateInMillis }
                    yestCal.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                            yestCal.get(Calendar.DAY_OF_YEAR) == itemCal.get(Calendar.DAY_OF_YEAR)
                }
                DateRangeFilter.THIS_WEEK -> {
                    val weekStart = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }.timeInMillis
                    item.dateInMillis >= weekStart
                }
                DateRangeFilter.THIS_MONTH -> {
                    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    sdf.format(Date(item.dateInMillis)) == sdf.format(Date())
                }
                DateRangeFilter.THIS_YEAR -> {
                    val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
                    sdf.format(Date(item.dateInMillis)) == sdf.format(Date())
                }
            }

            matchesQuery && matchesType && matchesCategory && matchesPayment && matchesDate
        }.let { filtered ->
            when (sort) {
                SortOption.LATEST_FIRST -> filtered.sortedByDescending { it.dateInMillis }
                SortOption.OLDEST_FIRST -> filtered.sortedBy { it.dateInMillis }
                SortOption.HIGHEST_AMOUNT -> filtered.sortedByDescending { it.amount }
                SortOption.LOWEST_AMOUNT -> filtered.sortedBy { it.amount }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Metrics
    val totalIncome: StateFlow<Double> = allTransactions.map { list ->
        list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = allTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentMonthTransactions: StateFlow<List<TransactionEntity>> = allTransactions.map { list ->
        val current = currentMonthYear
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        list.filter { sdf.format(Date(it.dateInMillis)) == current }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyIncome: StateFlow<Double> = currentMonthTransactions.map { list ->
        list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyExpense: StateFlow<Double> = currentMonthTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val overallMonthlyBudget: StateFlow<BudgetEntity?> = monthlyBudgets.map { list ->
        list.find { it.categoryName == null }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Category breakdown for Analytics
    val expenseByCategory: StateFlow<List<CategorySpend>> = combine(
        allTransactions,
        allCategories,
        analyticsTimeframe
    ) { transactions, categories, timeframe ->
        val filtered = filterTransactionsByTimeframe(transactions, timeframe)
            .filter { it.type == TransactionType.EXPENSE }
        val totalExp = filtered.sumOf { it.amount }

        val categoryMap = categories.associateBy { it.name }

        filtered.groupBy { it.categoryName }
            .map { (catName, items) ->
                val sum = items.sumOf { it.amount }
                val catEntity = categoryMap[catName]
                CategorySpend(
                    categoryName = catName,
                    amount = sum,
                    percentage = if (totalExp > 0) (sum / totalExp).toFloat() else 0f,
                    colorHex = catEntity?.colorHex ?: "#64748B",
                    iconName = catEntity?.iconName ?: "category",
                    transactionCount = items.size
                )
            }
            .sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Spending Trends Data
    val spendingTrends: StateFlow<List<TrendPoint>> = combine(
        allTransactions,
        analyticsTimeframe
    ) { transactions, timeframe ->
        generateTrendPoints(transactions, timeframe)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic Financial Insights
    val dynamicInsights: StateFlow<List<String>> = combine(
        allTransactions,
        monthlyBudgets
    ) { transactions, budgets ->
        generateInsights(transactions, budgets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Setters for Filters
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setTypeFilter(type: TransactionType?) { _selectedTypeFilter.value = type }
    fun setCategoryFilter(category: String?) { _selectedCategoryFilter.value = category }
    fun setPaymentFilter(payment: PaymentMethod?) { _selectedPaymentFilter.value = payment }
    fun setDateRangeFilter(range: DateRangeFilter) { _selectedDateRangeFilter.value = range }
    fun setSortOption(sort: SortOption) { _selectedSortOption.value = sort }
    fun setAnalyticsTimeframe(timeframe: AnalyticsTimeframe) { _analyticsTimeframe.value = timeframe }
    fun resetFilters() {
        _searchQuery.value = ""
        _selectedTypeFilter.value = null
        _selectedCategoryFilter.value = null
        _selectedPaymentFilter.value = null
        _selectedDateRangeFilter.value = DateRangeFilter.ALL
        _selectedSortOption.value = SortOption.LATEST_FIRST
        _minAmountFilter.value = null
        _maxAmountFilter.value = null
    }

    // CRUD Actions
    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        categoryName: String,
        categoryIcon: String,
        paymentMethod: PaymentMethod,
        dateInMillis: Long,
        timeString: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    title = title.trim(),
                    amount = amount,
                    type = type,
                    categoryName = categoryName,
                    categoryIcon = categoryIcon,
                    paymentMethod = paymentMethod,
                    dateInMillis = dateInMillis,
                    timeString = timeString,
                    notes = notes.trim()
                )
            )
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteTransactionById(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun addCategory(name: String, iconName: String, type: CategoryType, colorHex: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getCategoryByNameAndType(name.trim(), type)
            if (existing != null) {
                onResult(false, "A category with this name already exists.")
                return@launch
            }
            repository.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    iconName = iconName,
                    type = type,
                    colorHex = colorHex,
                    isDefault = false
                )
            )
            onResult(true, null)
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun setBudget(categoryName: String?, limitAmount: Double) {
        viewModelScope.launch {
            val current = repository.getCurrentMonthYear()
            repository.insertOrUpdateBudget(
                BudgetEntity(
                    categoryName = categoryName,
                    monthYear = current,
                    limitAmount = limitAmount
                )
            )
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    fun addGoal(name: String, targetAmount: Double, savedAmount: Double, targetDateInMillis: Long, description: String, iconName: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertGoal(
                GoalEntity(
                    name = name.trim(),
                    targetAmount = targetAmount,
                    savedAmount = savedAmount,
                    targetDateInMillis = targetDateInMillis,
                    description = description.trim(),
                    iconName = iconName,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun addMoneyToGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            repository.addMoneyToGoal(goalId, amount)
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotificationById(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        repository.updateProfile(profile)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        repository.setOnboardingCompleted(completed)
    }

    fun setCurrency(code: String, symbol: String) {
        repository.setCurrency(code, symbol)
    }

    fun setThemeMode(mode: String) {
        repository.setThemeMode(mode)
    }

    fun setUserName(name: String) {
        repository.setUserName(name)
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
        }
    }

    // Export formatting
    fun exportTransactionsAsCsv(): String {
        val list = allTransactions.value
        val sb = StringBuilder()
        sb.append("ID,Title,Amount,Type,Category,PaymentMethod,Date,Time,Notes\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        list.forEach {
            sb.append("${it.id},\"${it.title.replace("\"", "\"\"")}\",${it.amount},${it.type.name},\"${it.categoryName}\",${it.paymentMethod.displayName},${sdf.format(Date(it.dateInMillis))},\"${it.timeString}\",\"${it.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    fun exportMonthlyReportSummary(): String {
        val curMonth = currentMonthYear
        val transactions = currentMonthTransactions.value
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val savings = income - expense
        val symbol = userProfile.value.currencySymbol

        val sb = StringBuilder()
        sb.append("FINANCIAL MONTHLY REPORT - $curMonth\n")
        sb.append("====================================\n")
        sb.append("Total Monthly Income: $symbol${String.format(Locale.getDefault(), "%,.2f", income)}\n")
        sb.append("Total Monthly Expense: $symbol${String.format(Locale.getDefault(), "%,.2f", expense)}\n")
        sb.append("Net Monthly Savings: $symbol${String.format(Locale.getDefault(), "%,.2f", savings)}\n")
        sb.append("Total Transactions: ${transactions.size}\n\n")
        sb.append("SPENDING BY CATEGORY:\n")
        transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryName }
            .forEach { (cat, items) ->
                val sum = items.sumOf { it.amount }
                val pct = if (expense > 0) (sum / expense) * 100 else 0.0
                sb.append("- $cat: $symbol${String.format(Locale.getDefault(), "%,.2f", sum)} (${String.format(Locale.getDefault(), "%.1f", pct)}%)\n")
            }
        return sb.toString()
    }

    // Helper functions
    private fun filterTransactionsByTimeframe(list: List<TransactionEntity>, timeframe: AnalyticsTimeframe): List<TransactionEntity> {
        val now = Calendar.getInstance()
        return when (timeframe) {
            AnalyticsTimeframe.WEEKLY -> {
                val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
                list.filter { it.dateInMillis >= weekAgo }
            }
            AnalyticsTimeframe.MONTHLY -> {
                val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                val current = sdf.format(Date())
                list.filter { sdf.format(Date(it.dateInMillis)) == current }
            }
            AnalyticsTimeframe.YEARLY -> {
                val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
                val current = sdf.format(Date())
                list.filter { sdf.format(Date(it.dateInMillis)) == current }
            }
            AnalyticsTimeframe.ALL_TIME -> list
        }
    }

    private fun generateTrendPoints(list: List<TransactionEntity>, timeframe: AnalyticsTimeframe): List<TrendPoint> {
        val points = mutableListOf<TrendPoint>()
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        when (timeframe) {
            AnalyticsTimeframe.WEEKLY -> {
                for (i in 6 downTo 0) {
                    val c = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -i)
                    }
                    val dayStart = Calendar.getInstance().apply {
                        timeInMillis = c.timeInMillis
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }.timeInMillis
                    val dayEnd = dayStart + 24 * 60 * 60 * 1000L

                    val dayTransactions = list.filter { it.dateInMillis in dayStart until dayEnd }
                    val exp = dayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    val inc = dayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    points.add(TrendPoint(label = dayFormat.format(c.time), expense = exp, income = inc))
                }
            }
            AnalyticsTimeframe.MONTHLY -> {
                // 4 weekly buckets in month
                for (w in 1..4) {
                    val weekName = "W$w"
                    val daysInBucket = 7
                    val startDay = (w - 1) * daysInBucket
                    val endDay = w * daysInBucket

                    val curMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                    val monthTrans = list.filter {
                        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.dateInMillis)) == curMonth
                    }
                    val bucketTrans = monthTrans.filter {
                        val dayOfMonth = Calendar.getInstance().apply { timeInMillis = it.dateInMillis }.get(Calendar.DAY_OF_MONTH)
                        dayOfMonth in (startDay + 1)..endDay
                    }
                    val exp = bucketTrans.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    val inc = bucketTrans.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    points.add(TrendPoint(label = weekName, expense = exp, income = inc))
                }
            }
            AnalyticsTimeframe.YEARLY, AnalyticsTimeframe.ALL_TIME -> {
                for (m in 5 downTo 0) {
                    val c = Calendar.getInstance().apply {
                        add(Calendar.MONTH, -m)
                    }
                    val mStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(c.time)
                    val mTrans = list.filter {
                        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.dateInMillis)) == mStr
                    }
                    val exp = mTrans.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    val inc = mTrans.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    points.add(TrendPoint(label = monthFormat.format(c.time), expense = exp, income = inc))
                }
            }
        }
        return points
    }

    private fun generateInsights(transactions: List<TransactionEntity>, budgets: List<BudgetEntity>): List<String> {
        val insights = mutableListOf<String>()
        val currentMonth = currentMonthYear
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        val thisMonthExpenses = transactions.filter {
            sdf.format(Date(it.dateInMillis)) == currentMonth && it.type == TransactionType.EXPENSE
        }
        val thisMonthIncome = transactions.filter {
            sdf.format(Date(it.dateInMillis)) == currentMonth && it.type == TransactionType.INCOME
        }.sumOf { it.amount }

        val totalSpentThisMonth = thisMonthExpenses.sumOf { it.amount }

        // 1. Highest spending category
        val topCategory = thisMonthExpenses.groupBy { it.categoryName }
            .maxByOrNull { (_, items) -> items.sumOf { it.amount } }

        if (topCategory != null) {
            val amount = topCategory.value.sumOf { it.amount }
            insights.add("Your highest spending category this month is ${topCategory.key} (${formatAmount(amount)}).")
        } else {
            insights.add("No expense records logged yet for this month.")
        }

        // 2. Month-over-month comparison
        val prevMonthCal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val prevMonthStr = sdf.format(prevMonthCal.time)
        val prevMonthExpenses = transactions.filter {
            sdf.format(Date(it.dateInMillis)) == prevMonthStr && it.type == TransactionType.EXPENSE
        }.sumOf { it.amount }

        if (prevMonthExpenses > 0) {
            val diff = totalSpentThisMonth - prevMonthExpenses
            val pct = ((Math.abs(diff) / prevMonthExpenses) * 100).toInt()
            if (diff < 0) {
                insights.add("You spent $pct% less compared to last month. Excellent job!")
            } else if (diff > 0) {
                insights.add("You spent $pct% more compared to last month. Consider reviewing high-ticket items.")
            } else {
                insights.add("Your spending is identical to last month.")
            }
        } else {
            insights.add("Track your expenses regularly to unlock smart month-over-month comparison trends.")
        }

        // 3. Savings Rate
        if (thisMonthIncome > 0) {
            val savings = thisMonthIncome - totalSpentThisMonth
            val rate = ((savings / thisMonthIncome) * 100).toInt()
            if (rate > 0) {
                insights.add("Your current monthly savings rate is $rate% (${formatAmount(savings)} saved).")
            } else {
                insights.add("Your expenses exceed your income this month by ${formatAmount(Math.abs(savings))}.")
            }
        }

        // 4. Budget health
        val overallBudget = budgets.find { it.categoryName == null }
        if (overallBudget != null) {
            val remaining = overallBudget.limitAmount - totalSpentThisMonth
            if (remaining < 0) {
                insights.add("⚠️ You have exceeded your monthly budget by ${formatAmount(Math.abs(remaining))}.")
            } else {
                val usedPct = ((totalSpentThisMonth / overallBudget.limitAmount) * 100).toInt()
                insights.add("You have used $usedPct% of your monthly budget (${formatAmount(remaining)} remaining).")
            }
        }

        return insights
    }

    fun formatAmount(amount: Double): String {
        val symbol = userProfile.value.currencySymbol
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        return "$symbol${formatter.format(amount)}"
    }
}
