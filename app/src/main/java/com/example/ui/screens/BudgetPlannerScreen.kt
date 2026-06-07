package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Budget
import com.example.data.model.Transaction
import com.example.ui.theme.*
import com.example.util.TranslationHelper
import com.example.data.model.isInPeriod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetPlannerScreen(
    budgets: List<Budget>,
    transactions: List<Transaction>,
    currency: String,
    onSaveBudget: (category: String, limit: Double, period: String) -> Unit,
    onDeleteBudget: (Budget) -> Unit,
    expenseCategories: List<String> = listOf("Makanan", "Transportasi", "Belanja", "Hiburan", "Pendidikan", "Kesehatan", "Tagihan", "Lainnya"),
    selectedLanguage: String = "ID",
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember(expenseCategories) { 
        mutableStateOf(if (expenseCategories.isNotEmpty()) expenseCategories[0] else "") 
    }
    var limitText by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("MONTHLY") }
    var editingBudget by remember { mutableStateOf<Budget?>(null) }
    var showForm by remember { mutableStateOf(false) }

    val isLimitFormatError = remember(limitText) {
        if (limitText.isBlank()) {
            false
        } else {
            val hasDigits = limitText.any { it.isDigit() }
            if (!hasDigits) {
                true
            } else {
                val clean = limitText.replace(".", "").replace(",", "")
                val onlyDigits = clean.all { it.isDigit() }
                if (!onlyDigits) {
                    true
                } else if (limitText.contains("..") || limitText.contains(",,") || limitText.contains(".,") || limitText.contains(",.")) {
                    true
                } else {
                    false
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("budget_planner_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Title Header Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = TranslationHelper.translate("budget_planner", selectedLanguage),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = TranslationHelper.translate("budget_subtitle", selectedLanguage),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            showForm = !showForm 
                            if (!showForm) {
                                editingBudget = null
                                limitText = ""
                            }
                        }
                        .testTag("add_budget_toggle_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (showForm) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = "Config form toggle",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Add Budget Collapse Form
        item {
            AnimatedVisibility(
                visible = showForm,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (editingBudget == null) "Konfigurasi Anggaran Baru" else "Edit Anggaran: ${editingBudget?.category}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        // Categoric Chips List Selection
                        Column {
                            Text(
                                text = "Kategori Pengeluaran",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                expenseCategories.forEach { cat ->
                                    val isSelected = cat == selectedCategory
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { 
                                            // Only allow selecting category when not editing since category is primary key
                                            if (editingBudget == null) {
                                                selectedCategory = cat 
                                            }
                                        },
                                        enabled = editingBudget == null,
                                        label = { Text(cat, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = WarningGold,
                                            containerColor = MaterialTheme.colorScheme.background,
                                            disabledSelectedContainerColor = WarningGold.copy(alpha = 0.5f),
                                            disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                            selectedBorderColor = Color.Transparent,
                                            enabled = editingBudget == null,
                                            selected = isSelected
                                        )
                                    )
                                }
                            }
                        }

                        // Pemilihan Periode (Harian, Mingguan, Bulanan)
                        Column {
                            Text(
                                text = "Periode Anggaran",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple("Harian", "DAILY", WarningGold),
                                    Triple("Mingguan", "WEEKLY", WarningGold),
                                    Triple("Bulanan", "MONTHLY", WarningGold)
                                ).forEach { (label, value, color) ->
                                    val isSelected = selectedPeriod == value
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedPeriod = value },
                                        label = { Text(label, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = color,
                                            containerColor = MaterialTheme.colorScheme.background
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                            selectedBorderColor = Color.Transparent,
                                            enabled = true,
                                            selected = isSelected
                                        )
                                    )
                                }
                            }
                        }

                        // Nominal Threshold limit input
                        OutlinedTextField(
                            value = limitText,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.all { it.isDigit() || it == '.' || it == ',' }) {
                                    limitText = input
                                }
                            },
                            label = { Text("Batas Anggaran ($currency)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                            isError = isLimitFormatError,
                            supportingText = {
                                if (isLimitFormatError) {
                                    Text(
                                        text = "Format nominal tidak valid. Gunakan pemisah angka yang benar.",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = PrimaryBlue,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                errorLabelColor = MaterialTheme.colorScheme.error,
                                errorSupportingTextColor = MaterialTheme.colorScheme.error
                            ),
                            placeholder = { Text("cth: 1000000", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("budget_limit_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Save Budget Button
                        Button(
                            onClick = {
                                if (limitText.isNotBlank() && !isLimitFormatError) {
                                    val limitDouble = parseMoneyToDouble(limitText)
                                    onSaveBudget(selectedCategory, limitDouble, selectedPeriod)
                                    limitText = ""
                                    editingBudget = null
                                    showForm = false
                                }
                            },
                            enabled = limitText.isNotBlank() && !isLimitFormatError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_budget_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarningGold,
                                disabledContainerColor = WarningGold.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (editingBudget == null) "Tetapkan Anggaran" else "Perbarui Anggaran",
                                fontWeight = FontWeight.Bold,
                                color = if (limitText.isNotBlank() && !isLimitFormatError) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // Budget Title list
        item {
            Text(
                text = "Daftar Anggaran Aktif",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // List components containing budgets
        if (budgets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
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
                            imageVector = Icons.Filled.ListAlt,
                            contentDescription = "Empty list",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum menetapkan batas anggaran",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ketuk tombol '+' di kanan atas untuk membuat limit kategori belanja.",
                            color = GreyText,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(budgets) { budget ->
                val spent = transactions
                    .filter { it.type == "EXPENSE" && it.category.equals(budget.category, ignoreCase = true) && it.isInPeriod(budget.period) }
                    .sumOf { it.amount }
                val ratio = if (budget.limitAmount > 0) (spent / budget.limitAmount).toFloat().coerceIn(0f, 1.2f) else 1f
                val remaining = (budget.limitAmount - spent).coerceAtLeast(0.0)
                
                val periodLabel = when (budget.period.uppercase()) {
                    "DAILY" -> "Harian"
                    "WEEKLY" -> "Mingguan"
                    else -> "Bulanan"
                }

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("budget_item_card_${budget.category}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(WarningGold.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(budget.category),
                                        contentDescription = budget.category,
                                        tint = WarningGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = budget.category,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = periodLabel,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Sisa: $currency ${formatMoney(remaining)}",
                                        color = if (remaining <= 0) DangerRed else SuccessGreen,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        editingBudget = budget
                                        selectedCategory = budget.category
                                        limitText = budget.limitAmount.toInt().toString()
                                        selectedPeriod = budget.period
                                        showForm = true
                                    },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.background, CircleShape).size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteBudget(budget) },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.background, CircleShape).size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Hapus",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Usage linear gauge meter indicator
                        LinearProgressIndicator(
                            progress = { ratio.coerceAtMost(1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when {
                                ratio >= 1.0f -> DangerRed
                                ratio >= 0.8f -> WarningGold
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
                                text = "Terpakai: $currency ${formatMoney(spent)} dari $currency ${formatMoney(budget.limitAmount)}",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${(ratio * 100).toInt()}%",
                                color = when {
                                    ratio >= 1.0f -> DangerRed
                                    ratio >= 0.8f -> WarningGold
                                    else -> SuccessGreen
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Warnings
                        if (ratio >= 1.0f) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x15EF4444), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = "Warning Limit",
                                    tint = DangerRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Anggaran ${budget.category} telah terlampaui!",
                                    color = DangerRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else if (ratio >= 0.8f) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x15F59E0B), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ErrorOutline,
                                    contentDescription = "Warning Limit Almost",
                                    tint = WarningGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Anggaran hampir mencapai limit (80%+).",
                                    color = WarningGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
