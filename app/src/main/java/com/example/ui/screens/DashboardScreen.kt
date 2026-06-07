package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Budget
import com.example.data.model.Transaction
import com.example.ui.theme.*
import com.example.util.TranslationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    transactions: List<Transaction>,
    budgets: List<Budget>,
    currency: String,
    incomeCategories: List<String>,
    expenseCategories: List<String>,
    onUpdateIncomeCategories: (List<String>) -> Unit,
    onUpdateExpenseCategories: (List<String>) -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToBudgetPlanner: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    selectedLanguage: String = "ID",
    modifier: Modifier = Modifier
) {
    // Computations from real Room Db
    val totalIncome = remember(transactions) {
        transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val currentBalance = totalIncome - totalExpense

    val totalBudgetLimit = remember(budgets) {
        budgets.sumOf { it.limitAmount }
    }
    
    // Sum of expenses belonging strictly to budgeted categories
    val spentOnBudget = remember(transactions, budgets) {
        val budgetedCategories = budgets.map { it.category.lowercase() }.toSet()
        transactions.filter {
            it.type == "EXPENSE" && budgetedCategories.contains(it.category.lowercase())
        }.sumOf { it.amount }
    }

    val remainingBudget = (totalBudgetLimit - spentOnBudget).coerceAtLeast(0.0)
    val budgetUsageProgress = if (totalBudgetLimit > 0) {
        (spentOnBudget / totalBudgetLimit).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = TranslationHelper.translate("hello_user", selectedLanguage),
                        color = GreyText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = TranslationHelper.translate("dashboard_title", selectedLanguage),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                IconButton(
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Insights,
                        contentDescription = "Analysis",
                        tint = SecondaryBlue
                    )
                }
            }
        }

        // Saldo / Wallet Master Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryBlue, Color(0xFF1D4ED8))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = TranslationHelper.translate("current_balance_cap", selectedLanguage),
                            color = Color(0x99F8FAFC),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Filled.Stars,
                            contentDescription = "Premium Chip",
                            tint = SecondaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$currency ${formatMoney(currentBalance)}",
                        color = TextLight,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("dashboard_balance")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Total Pemasukan Subsection
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0x22F8FAFC), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowUpward,
                                    contentDescription = "Income Flag",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = TranslationHelper.translate("add_income", selectedLanguage),
                                    color = Color(0x99F8FAFC),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$currency ${formatMoney(totalIncome)}",
                                    color = TextLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Total Pengeluaran Subsection
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0x22F8FAFC), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDownward,
                                    contentDescription = "Expenses Flag",
                                    tint = DangerRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = TranslationHelper.translate("add_expense", selectedLanguage),
                                    color = Color(0x99F8FAFC),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$currency ${formatMoney(totalExpense)}",
                                    color = TextLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Budget utilization bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = TranslationHelper.translate("active_budget", selectedLanguage),
                                color = GreyText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (totalBudgetLimit > 0) {
                                    "$currency ${formatMoney(remainingBudget)} / $currency ${formatMoney(totalBudgetLimit)}"
                                } else {
                                    TranslationHelper.translate("budget_unset", selectedLanguage)
                                },
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = onNavigateToBudgetPlanner,
                            modifier = Modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = "Edit Budget",
                                tint = SecondaryBlue
                            )
                        }
                    }

                    if (totalBudgetLimit > 0) {
                        Spacer(modifier = Modifier.height(16.dp))

                        val animatedProgress by animateFloatAsState(targetValue = budgetUsageProgress, label = "BudgetBar")
                        
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                            color = when {
                                budgetUsageProgress >= 1.0f -> DangerRed
                                budgetUsageProgress >= 0.8f -> WarningGold
                                else -> SuccessGreen
                            },
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = TranslationHelper.translate("budget_usage", selectedLanguage),
                                color = GreyText,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${(budgetUsageProgress * 100).toInt()}%",
                                color = when {
                                    budgetUsageProgress >= 1f -> DangerRed
                                    budgetUsageProgress >= 0.8f -> WarningGold
                                    else -> SuccessGreen
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Action Shortcuts grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add Transaction Quick Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToAddTransaction() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "Tambah",
                            tint = SuccessGreen,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = TranslationHelper.translate("add_transaction", selectedLanguage),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Budget planner Shortcut
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToBudgetPlanner() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PieChart,
                            contentDescription = "Budget",
                            tint = WarningGold,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = TranslationHelper.translate("budget_planner", selectedLanguage),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Recharts-style Monthly Spending Breakdown section
        item {
            val cal = java.util.Calendar.getInstance()
            val curMonth = cal.get(java.util.Calendar.MONTH)
            val curYear = cal.get(java.util.Calendar.YEAR)
            
            val currentMonthExpensesList = remember(transactions) {
                transactions.filter { trx ->
                    if (trx.type != "EXPENSE") false
                    else {
                        val tCal = java.util.Calendar.getInstance().apply { timeInMillis = trx.date }
                        tCal.get(java.util.Calendar.MONTH) == curMonth && tCal.get(java.util.Calendar.YEAR) == curYear
                    }
                }
            }
            
            val totalMonthExpensesValue = remember(currentMonthExpensesList) {
                currentMonthExpensesList.sumOf { it.amount }
            }
            
            val monthlySpendingDistributionList = remember(currentMonthExpensesList, totalMonthExpensesValue) {
                if (totalMonthExpensesValue == 0.0) emptyList()
                else {
                    currentMonthExpensesList.groupBy { it.category }
                        .map { (cat, list) ->
                            val sum = list.sumOf { it.amount }
                            val percent = (sum / totalMonthExpensesValue * 100).toFloat()
                            PieSlice(
                                category = cat,
                                amount = sum,
                                percentage = percent,
                                color = getSequentialColorForCategory(cat)
                            )
                        }.sortedByDescending { it.amount }
                }
            }

            var selectedPieSliceIdx by remember { mutableStateOf(-1) }
            val currentMonthName = remember(selectedLanguage) {
                val locale = when (selectedLanguage) {
                    "ID" -> Locale("id", "ID")
                    "ES" -> Locale("es", "ES")
                    "JA" -> Locale.JAPAN
                    "ZH" -> Locale.CHINA
                    else -> Locale.US
                }
                val formatter = SimpleDateFormat("MMMM yyyy", locale)
                formatter.format(Date())
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("monthly_spending_pie_chart"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${TranslationHelper.translate("category_expenses", selectedLanguage)} ($currentMonthName)",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = TranslationHelper.translate("proportion_desc", selectedLanguage),
                                color = GreyText,
                                fontSize = 12.sp
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Filled.PieChart,
                            contentDescription = "Pie Chart Overview",
                            tint = PrimaryBlue
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    if (monthlySpendingDistributionList.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = TranslationHelper.translate("no_month_expenses", selectedLanguage),
                                color = GreyText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Canvas chart
                            Box(
                                modifier = Modifier.size(150.dp).weight(1.2f),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(130.dp)) {
                                    var startAngle = -90f
                                    monthlySpendingDistributionList.forEachIndexed { idx, slice ->
                                        val sweepAngle = slice.percentage * 3.6f
                                        val isSelected = selectedPieSliceIdx == idx || selectedPieSliceIdx == -1
                                        val strokeMultiplier = if (selectedPieSliceIdx == idx) 26.dp else 18.dp
                                        
                                        drawArc(
                                            color = if (isSelected) slice.color else slice.color.copy(alpha = 0.3f),
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = strokeMultiplier.toPx(), cap = StrokeCap.Round)
                                        )
                                        startAngle += sweepAngle
                                    }
                                }
                                
                                // Center focused content (interactive)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(8.dp).clickable { selectedPieSliceIdx = -1 }
                                ) {
                                    if (selectedPieSliceIdx in monthlySpendingDistributionList.indices) {
                                        val active = monthlySpendingDistributionList[selectedPieSliceIdx]
                                        Text(
                                            text = active.category,
                                            color = active.color,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${active.percentage.toInt()}%",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = "$currency\n${formatMoney(active.amount)}",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                            fontSize = 10.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            lineHeight = 11.sp
                                        )
                                    } else {
                                        Text(
                                            text = TranslationHelper.translate("total_monthly_spending", selectedLanguage),
                                            color = GreyText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Text(
                                            text = "$currency\n${formatMoney(totalMonthExpensesValue)}",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Legends list side (Recharts styled)
                            Column(
                                modifier = Modifier.weight(1.8f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                monthlySpendingDistributionList.forEachIndexed { idx, slice ->
                                    val isSelected = selectedPieSliceIdx == idx
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                                else Color.Transparent
                                            )
                                            .clickable {
                                                selectedPieSliceIdx = if (isSelected) -1 else idx
                                            }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(slice.color, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = slice.category,
                                                color = if (isSelected) slice.color else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "${slice.percentage.toInt()}%",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category Manager Card
        item {
            var showCategoryDialog by remember { mutableStateOf(false) }
            var isManagingExpense by remember { mutableStateOf(true) }
            
            // For custom edits
            var showEditDialog by remember { mutableStateOf(false) }
            var editingIndex by remember { mutableStateOf(-1) }
            var editingText by remember { mutableStateOf("") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = TranslationHelper.translate("financial_categories", selectedLanguage),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = TranslationHelper.translate("manage_categories_desc", selectedLanguage),
                                color = GreyText,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = { showCategoryDialog = true },
                            modifier = Modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Category,
                                contentDescription = "Kelola",
                                tint = SecondaryBlue
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Horizontal scroll of categories chips as preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${TranslationHelper.translate("add_expense", selectedLanguage)}:",
                            color = GreyText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            // Show first 3 categories
                            Text(
                                text = expenseCategories.joinToString(", "),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${TranslationHelper.translate("add_income", selectedLanguage)}:",
                            color = GreyText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            Text(
                                text = incomeCategories.joinToString(", "),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Categories Management Dialog
            if (showCategoryDialog) {
                var newCategoryName by remember { mutableStateOf("") }
                
                AlertDialog(
                    onDismissRequest = { showCategoryDialog = false },
                    title = {
                        Text(TranslationHelper.translate("manage_categories_title", selectedLanguage), fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Tab selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                            ) {
                                Button(
                                    onClick = { isManagingExpense = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isManagingExpense) PrimaryBlue else Color.Transparent,
                                        contentColor = if (isManagingExpense) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(TranslationHelper.translate("add_expense", selectedLanguage), fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { isManagingExpense = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isManagingExpense) PrimaryBlue else Color.Transparent,
                                        contentColor = if (!isManagingExpense) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(TranslationHelper.translate("add_income", selectedLanguage), fontSize = 12.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Category add input field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newCategoryName,
                                    onValueChange = { newCategoryName = it },
                                    placeholder = { Text(TranslationHelper.translate("new_category_hint", selectedLanguage), fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        if (newCategoryName.isNotBlank()) {
                                            val currentList = if (isManagingExpense) expenseCategories else incomeCategories
                                            if (!currentList.contains(newCategoryName)) {
                                                val updatedList = currentList + newCategoryName.trim()
                                                if (isManagingExpense) {
                                                    onUpdateExpenseCategories(updatedList)
                                                } else {
                                                    onUpdateIncomeCategories(updatedList)
                                                }
                                                newCategoryName = ""
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Tambah")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Scrollable list of categories to edit or delete
                            Text(TranslationHelper.translate("category_list_instruction", selectedLanguage), color = GreyText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val activeList = if (isManagingExpense) expenseCategories else incomeCategories
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                            ) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(activeList.size) { idx ->
                                        val cat = activeList[idx]
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                                .clickable {
                                                    editingIndex = idx
                                                    editingText = cat
                                                    showEditDialog = true
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.Edit,
                                                    contentDescription = "Edit",
                                                    tint = GreyText,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(cat, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                            }
                                            IconButton(
                                                onClick = {
                                                    val updatedList = activeList.toMutableList().apply { removeAt(idx) }
                                                    if (isManagingExpense) {
                                                        onUpdateExpenseCategories(updatedList)
                                                    } else {
                                                        onUpdateIncomeCategories(updatedList)
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = "Hapus",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showCategoryDialog = false }) {
                            Text(TranslationHelper.translate("button_close", selectedLanguage), color = PrimaryBlue)
                        }
                    }
                )
            }
            
            // Edit Specific Category Dialog
            if (showEditDialog) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text(TranslationHelper.translate("edit_category_name", selectedLanguage), fontWeight = FontWeight.Bold) },
                    text = {
                        OutlinedTextField(
                            value = editingText,
                            onValueChange = { editingText = it },
                            label = { Text(TranslationHelper.translate("category_name_label", selectedLanguage)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editingText.isNotBlank()) {
                                    val activeList = if (isManagingExpense) expenseCategories else incomeCategories
                                    val updatedList = activeList.toMutableList()
                                    if (editingIndex in updatedList.indices) {
                                        updatedList[editingIndex] = editingText.trim()
                                        if (isManagingExpense) {
                                            onUpdateExpenseCategories(updatedList)
                                        } else {
                                            onUpdateIncomeCategories(updatedList)
                                        }
                                    }
                                    showEditDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text(TranslationHelper.translate("button_save", selectedLanguage), color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text(TranslationHelper.translate("button_cancel", selectedLanguage), color = Color(0xFFEF4444))
                        }
                    }
                )
            }
        }

        // Recent transaction panel title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TranslationHelper.translate("recent_activities", selectedLanguage),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = TranslationHelper.translate("see_all", selectedLanguage),
                    color = SecondaryBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateToAddTransaction() } // Go navigate or redirect
                )
            }
        }

        // List elements
        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Empty",
                            tint = GreyText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = TranslationHelper.translate("no_transactions_registered", selectedLanguage),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = TranslationHelper.translate("tap_add_desc", selectedLanguage),
                            color = GreyText,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(transactions.take(5)) { trx ->
                TransactionRowItem(
                    transaction = trx,
                    currency = currency,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: Transaction,
    currency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == "INCOME"
    val cardColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isIncome) Color(0x1522C55E) else Color(0x15EF4444),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(transaction.category),
                        contentDescription = transaction.category,
                        tint = if (isIncome) SuccessGreen else DangerRed,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.category,
                            color = GreyText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(GreyText, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatDate(transaction.date),
                            color = GreyText,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Text(
                text = "${if (isIncome) "+" else "-"} $currency ${formatMoney(transaction.amount)}",
                color = if (isIncome) SuccessGreen else DangerRed,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.then(prefixDiffPadding(transaction.id)) // Unique tag identifiers space padding helper
            )
        }
    }
}

fun prefixDiffPadding(id: Long): Modifier {
    return Modifier.testTag("transaction_amount_$id")
}

fun formatMoney(amount: Double): String {
    return String.format("%,.0f", amount).replace(",", ".")
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "gaji" -> Icons.Filled.Payment
        "freelance" -> Icons.Filled.Work
        "bonus" -> Icons.Filled.CardGiftcard
        "investasi" -> Icons.Filled.TrendingUp
        "makanan" -> Icons.Filled.Restaurant
        "transportasi" -> Icons.Filled.DirectionsCar
        "belanja" -> Icons.Filled.ShoppingBag
        "hiburan" -> Icons.Filled.LocalActivity
        "pendidikan" -> Icons.Filled.School
        "kesehatan" -> Icons.Filled.LocalHospital
        "tagihan" -> Icons.Filled.ReceiptLong
        else -> Icons.Filled.Category
    }
}

fun parseMoneyToDouble(input: String): Double {
    if (input.isBlank()) return 0.0
    
    // Clean currency symbols, spaces, lowercase
    var cleaned = input.trim()
        .replace(Regex("(?i)rp\\.?"), "")
        .replace(Regex("(?i)idr"), "")
        .replace(Regex("(?i)\\$"), "")
        .replace(" ", "")
    
    if (cleaned.isBlank()) return 0.0
    
    // Find all indices of separator characters (dot and comma)
    val separators = mutableListOf<Pair<Char, Int>>()
    cleaned.forEachIndexed { index, char ->
        if (char == '.' || char == ',') {
            separators.add(Pair(char, index))
        }
    }
    
    if (separators.isEmpty()) {
        return cleaned.toDoubleOrNull() ?: 0.0
    }
    
    if (separators.size == 1) {
        val (char, idx) = separators.first()
        val suffix = cleaned.substring(idx + 1)
        // If it's followed by exactly 3 digits, it's a thousands separator
        if (suffix.matches(Regex("\\d{3}"))) {
            cleaned = cleaned.replace(char.toString(), "")
        } else {
            // Otherwise it's a decimal separator, normalize to standard dot "."
            cleaned = cleaned.substring(0, idx) + "." + suffix
        }
        return cleaned.toDoubleOrNull() ?: 0.0
    }
    
    // If there are multiple separators, identify the last separator
    val lastSep = separators.last()
    val lastChar = lastSep.first
    val lastIdx = lastSep.second
    val suffix = cleaned.substring(lastIdx + 1)
    
    val allSameChar = separators.all { it.first == lastChar }
    if (suffix.matches(Regex("\\d{3}")) && allSameChar) {
        // All separators are thousands separators! Just remove all of them.
        cleaned = cleaned.replace(lastChar.toString(), "")
    } else {
        // The last separator is a decimal separator. The ones before are thousands separators.
        // Remove all previous separators, and replace the last one with '.'
        val prefixWithoutSeps = cleaned.substring(0, lastIdx)
            .replace(".", "")
            .replace(",", "")
        cleaned = prefixWithoutSeps + "." + suffix
    }
    
    return cleaned.toDoubleOrNull() ?: 0.0
}
