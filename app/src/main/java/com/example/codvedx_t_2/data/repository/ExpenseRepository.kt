package com.example.codvedx_t_2.data.repository

import android.graphics.Color
import com.example.codvedx_t_2.data.local.AppDatabase
import com.example.codvedx_t_2.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class ExpenseRepository(private val db: AppDatabase) {

    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    val allCategories: Flow<List<CategoryEntity>> = db.categoryDao().getAllCategories()
    val allAccounts: Flow<List<AccountEntity>> = db.accountDao().getAllAccounts()
    val totalBalance: Flow<Double?> = db.accountDao().getTotalBalance()
    val totalIncomeAllTime: Flow<Double?> = db.transactionDao().getTotalIncomeAllTime()
    val totalExpenseAllTime: Flow<Double?> = db.transactionDao().getTotalExpenseAllTime()
    val allGoals: Flow<List<GoalEntity>> = db.goalDao().getAllGoals()
    val allSubscriptions: Flow<List<SubscriptionEntity>> = db.subscriptionDao().getAllSubscriptions()

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>> {
        return db.transactionDao().getTransactionsByDateRange(startDate, endDate)
    }

    fun getTotalIncomeByDateRange(startDate: Long, endDate: Long): Flow<Double?> {
        return db.transactionDao().getTotalIncomeByDateRange(startDate, endDate)
    }

    fun getTotalExpenseByDateRange(startDate: Long, endDate: Long): Flow<Double?> {
        return db.transactionDao().getTotalExpenseByDateRange(startDate, endDate)
    }

    fun getOverallBudget(month: Int, year: Int): Flow<BudgetEntity?> {
        return db.budgetDao().getOverallBudget(month, year)
    }

    fun getCategoryBudgets(month: Int, year: Int): Flow<List<BudgetEntity>> {
        return db.budgetDao().getCategoryBudgets(month, year)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        val id = db.transactionDao().insertTransaction(transaction)
        // Update account balance
        val sign = if (transaction.type == "INCOME") 1.0 else -1.0
        db.accountDao().updateAccountBalance(transaction.accountId, transaction.amount * sign)
        id
    }

    suspend fun updateTransaction(transaction: TransactionEntity, oldTransaction: TransactionEntity) = withContext(Dispatchers.IO) {
        // Revert old transaction balance impact
        val oldSign = if (oldTransaction.type == "INCOME") -1.0 else 1.0
        db.accountDao().updateAccountBalance(oldTransaction.accountId, oldTransaction.amount * oldSign)

        // Apply new transaction balance impact
        val newSign = if (transaction.type == "INCOME") 1.0 else -1.0
        db.accountDao().updateAccountBalance(transaction.accountId, transaction.amount * newSign)

        db.transactionDao().updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        // Revert balance impact
        val sign = if (transaction.type == "INCOME") -1.0 else 1.0
        db.accountDao().updateAccountBalance(transaction.accountId, transaction.amount * sign)
        db.transactionDao().deleteTransaction(transaction)
    }

    suspend fun insertCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        db.categoryDao().insertCategory(category)
    }

    suspend fun insertAccount(account: AccountEntity): Long = withContext(Dispatchers.IO) {
        db.accountDao().insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        db.accountDao().updateAccount(account)
    }

    suspend fun deleteAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        db.accountDao().deleteAccount(account)
    }

    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long = withContext(Dispatchers.IO) {
        db.budgetDao().insertOrUpdateBudget(budget)
    }

    suspend fun insertGoal(goal: GoalEntity): Long = withContext(Dispatchers.IO) {
        db.goalDao().insertGoal(goal)
    }

    suspend fun updateGoal(goal: GoalEntity) = withContext(Dispatchers.IO) {
        db.goalDao().updateGoal(goal)
    }

    suspend fun deleteGoal(goal: GoalEntity) = withContext(Dispatchers.IO) {
        db.goalDao().deleteGoal(goal)
    }

    suspend fun insertSubscription(subscription: SubscriptionEntity): Long = withContext(Dispatchers.IO) {
        db.subscriptionDao().insertSubscription(subscription)
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) = withContext(Dispatchers.IO) {
        db.subscriptionDao().updateSubscription(subscription)
    }

    suspend fun deleteSubscription(subscription: SubscriptionEntity) = withContext(Dispatchers.IO) {
        db.subscriptionDao().deleteSubscription(subscription)
    }

    suspend fun ensureDefaultDataSeeded() = withContext(Dispatchers.IO) {
        if (db.categoryDao().getCategoryCount() == 0) {
            val defaultCategories = listOf(
                CategoryEntity(name = "Food & Dining", type = "EXPENSE", iconName = "ic_food", colorHex = Color.parseColor("#FF5722"), budget = 5000.0, isDefault = true),
                CategoryEntity(name = "Groceries", type = "EXPENSE", iconName = "ic_grocery", colorHex = Color.parseColor("#4CAF50"), budget = 6000.0, isDefault = true),
                CategoryEntity(name = "Shopping", type = "EXPENSE", iconName = "ic_shopping", colorHex = Color.parseColor("#E91E63"), budget = 7000.0, isDefault = true),
                CategoryEntity(name = "Transportation", type = "EXPENSE", iconName = "ic_transport", colorHex = Color.parseColor("#2196F3"), budget = 3000.0, isDefault = true),
                CategoryEntity(name = "Fuel", type = "EXPENSE", iconName = "ic_fuel", colorHex = Color.parseColor("#FF9800"), budget = 2500.0, isDefault = true),
                CategoryEntity(name = "Bills & Utilities", type = "EXPENSE", iconName = "ic_bills", colorHex = Color.parseColor("#9C27B0"), budget = 4000.0, isDefault = true),
                CategoryEntity(name = "Rent", type = "EXPENSE", iconName = "ic_rent", colorHex = Color.parseColor("#795548"), budget = 12000.0, isDefault = true),
                CategoryEntity(name = "Entertainment", type = "EXPENSE", iconName = "ic_entertainment", colorHex = Color.parseColor("#673AB7"), budget = 3000.0, isDefault = true),
                CategoryEntity(name = "Healthcare", type = "EXPENSE", iconName = "ic_health", colorHex = Color.parseColor("#F44336"), budget = 2000.0, isDefault = true),
                CategoryEntity(name = "Subscriptions", type = "EXPENSE", iconName = "ic_subscription", colorHex = Color.parseColor("#00BCD4"), budget = 1500.0, isDefault = true),
                CategoryEntity(name = "Salary", type = "INCOME", iconName = "ic_salary", colorHex = Color.parseColor("#2E7D32"), isDefault = true),
                CategoryEntity(name = "Freelance", type = "INCOME", iconName = "ic_freelance", colorHex = Color.parseColor("#1565C0"), isDefault = true),
                CategoryEntity(name = "Investments", type = "INCOME", iconName = "ic_investment", colorHex = Color.parseColor("#F57F17"), isDefault = true),
                CategoryEntity(name = "Other Income", type = "INCOME", iconName = "ic_other", colorHex = Color.parseColor("#607D8B"), isDefault = true)
            )
            db.categoryDao().insertCategories(defaultCategories)
        }

        if (db.accountDao().getAccountCount() == 0) {
            val defaultAccounts = listOf(
                AccountEntity(name = "Cash Wallet", type = "Cash", balance = 5000.0, initialBalance = 5000.0, colorHex = Color.parseColor("#4CAF50")),
                AccountEntity(name = "HDFC Bank", type = "Bank", balance = 45000.0, initialBalance = 45000.0, colorHex = Color.parseColor("#1976D2")),
                AccountEntity(name = "Credit Card", type = "Credit Card", balance = -2500.0, initialBalance = 0.0, colorHex = Color.parseColor("#D32F2F")),
                AccountEntity(name = "Paytm Wallet", type = "Digital Wallet", balance = 3000.0, initialBalance = 3000.0, colorHex = Color.parseColor("#00838F"))
            )
            db.accountDao().insertAccounts(defaultAccounts)

            // Add sample transactions
            val cal = Calendar.getInstance()
            val now = cal.timeInMillis

            cal.add(Calendar.DAY_OF_MONTH, -1)
            val yesterday = cal.timeInMillis

            cal.add(Calendar.DAY_OF_MONTH, -2)
            val threeDaysAgo = cal.timeInMillis

            val sampleTransactions = listOf(
                TransactionEntity(type = "INCOME", title = "Monthly Salary", amount = 60000.0, categoryId = 11, categoryName = "Salary", categoryIcon = "ic_salary", categoryColor = Color.parseColor("#2E7D32"), accountId = 2, accountName = "HDFC Bank", paymentMethod = "Bank account", date = threeDaysAgo, notes = "October Salary"),
                TransactionEntity(type = "EXPENSE", title = "Groceries Shopping", amount = 1200.0, categoryId = 2, categoryName = "Groceries", categoryIcon = "ic_grocery", categoryColor = Color.parseColor("#4CAF50"), accountId = 2, accountName = "HDFC Bank", paymentMethod = "Debit card", date = yesterday, notes = "Weekly vegetables & essentials"),
                TransactionEntity(type = "EXPENSE", title = "Fuel Refill", amount = 1000.0, categoryId = 5, categoryName = "Fuel", categoryIcon = "ic_fuel", categoryColor = Color.parseColor("#FF9800"), accountId = 1, accountName = "Cash Wallet", paymentMethod = "Cash", date = yesterday),
                TransactionEntity(type = "EXPENSE", title = "Restaurant Dinner", amount = 850.0, categoryId = 1, categoryName = "Food & Dining", categoryIcon = "ic_food", categoryColor = Color.parseColor("#FF5722"), accountId = 3, accountName = "Credit Card", paymentMethod = "Credit card", date = now, notes = "Dinner with friends"),
                TransactionEntity(type = "EXPENSE", title = "Internet Bill", amount = 799.0, categoryId = 6, categoryName = "Bills & Utilities", categoryIcon = "ic_bills", categoryColor = Color.parseColor("#9C27B0"), accountId = 4, accountName = "Paytm Wallet", paymentMethod = "UPI", date = now)
            )

            sampleTransactions.forEach { db.transactionDao().insertTransaction(it) }

            // Add default goal
            val targetCal = Calendar.getInstance()
            targetCal.add(Calendar.MONTH, 6)
            db.goalDao().insertGoal(GoalEntity(name = "New Laptop", targetAmount = 80000.0, currentAmount = 50000.0, targetDate = targetCal.timeInMillis, note = "MacBook Pro M-series"))

            // Add default subscription
            val subCal = Calendar.getInstance()
            subCal.add(Calendar.DAY_OF_MONTH, 15)
            db.subscriptionDao().insertSubscription(SubscriptionEntity(name = "Netflix Standard", amount = 499.0, billingCycle = "Monthly", nextBillingDate = subCal.timeInMillis, paymentMethod = "Credit Card"))
        }
    }
}
