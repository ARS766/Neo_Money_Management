package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavingsGoal
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalScreen(
    goals: List<SavingsGoal>,
    currency: String,
    onSaveGoal: (name: String, targetAmount: Double, currentAmount: Double, targetDate: Long) -> Unit,
    onUpdateContribution: (SavingsGoal, Double) -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showForm by remember { mutableStateOf(false) }

    // Creating new goals state
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var initialText by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)) } // 30 days ahead

    val parsedTarget = remember(targetText) { parseMoneyToDouble(targetText) }
    val parsedInitial = remember(initialText) { parseMoneyToDouble(initialText) }

    val isTargetTooLow = remember(parsedTarget, currency) {
        val minTargetVal = when {
            currency.contains("Rp", ignoreCase = true) -> 10000.0
            else -> 10.0
        }
        targetText.isNotBlank() && parsedTarget < minTargetVal
    }

    val isInitialTooHigh = remember(parsedInitial, parsedTarget, targetText) {
        targetText.isNotBlank() && initialText.isNotBlank() && parsedInitial > parsedTarget
    }

    val isFormValid = remember(name, targetText, isTargetTooLow, isInitialTooHigh) {
        name.isNotBlank() && targetText.isNotBlank() && !isTargetTooLow && !isInitialTooHigh
    }

    // Contribution dialog popup
    var showContributionDialog by remember { mutableStateOf(false) }
    var activeGoalForContribution by remember { mutableStateOf<SavingsGoal?>(null) }
    var contributionText by remember { mutableStateOf("") }

    // Date picker dialog
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                targetDate = calendar.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("savings_goals_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Goal header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Target Tabungan",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Wujudkan impian belanja masa depan Anda",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                IconButton(
                    onClick = { showForm = !showForm },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(
                        imageVector = if (showForm) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = "Form toggle",
                        tint = SuccessGreen
                    )
                }
            }
        }

        // Expanded Save Goal Form
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
                            text = "Buat Impian Tabungan Baru",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nama Rekening / Laptop Baru", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = PrimaryBlue,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            placeholder = { Text("cth: Mobil Baru, Rumah", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("goal_name_input")
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = targetText,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.all { it.isDigit() || it == '.' || it == ',' }) targetText = input
                                },
                                label = { Text("Target Sasaran ($currency)", color = if (isTargetTooLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                                isError = isTargetTooLow,
                                supportingText = {
                                    if (isTargetTooLow) {
                                        val limitStr = when (currency) {
                                            "Rp" -> "Rp 10.000"
                                            "$" -> "$10"
                                            "€" -> "€10"
                                            "£" -> "£10"
                                            "¥" -> "¥10"
                                            else -> "10"
                                        }
                                        Text("Target minimal: $limitStr", color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isTargetTooLow) MaterialTheme.colorScheme.error else PrimaryBlue,
                                    unfocusedBorderColor = if (isTargetTooLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedLabelColor = if (isTargetTooLow) MaterialTheme.colorScheme.error else PrimaryBlue,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    errorLabelColor = MaterialTheme.colorScheme.error,
                                    errorSupportingTextColor = MaterialTheme.colorScheme.error
                                ),
                                placeholder = { Text("15000000", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("goal_target_input")
                            )

                            OutlinedTextField(
                                value = initialText,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.all { it.isDigit() || it == '.' || it == ',' }) initialText = input
                                },
                                label = { Text("Saldo Awal ($currency)", color = if (isInitialTooHigh) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                                isError = isInitialTooHigh,
                                supportingText = {
                                    if (isInitialTooHigh) {
                                        Text("Tidak boleh melebihi target", color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isInitialTooHigh) MaterialTheme.colorScheme.error else PrimaryBlue,
                                    unfocusedBorderColor = if (isInitialTooHigh) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedLabelColor = if (isInitialTooHigh) MaterialTheme.colorScheme.error else PrimaryBlue,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    errorLabelColor = MaterialTheme.colorScheme.error,
                                    errorSupportingTextColor = MaterialTheme.colorScheme.error
                                ),
                                placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Target Date Select
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .clickable { datePickerDialog.show() }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.DateRange,
                                        contentDescription = "Tanggal",
                                        tint = SecondaryBlue
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Estimasi Tanggal Pencapaian", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 11.sp)
                                        Text(
                                            text = dateFormatter.format(Date(targetDate)),
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit Date",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Create Goal Button
                        Button(
                            onClick = {
                                if (isFormValid) {
                                    val targetAmount = parseMoneyToDouble(targetText)
                                    val currentAmount = parseMoneyToDouble(initialText)
                                    onSaveGoal(name, targetAmount, currentAmount, targetDate)
                                    name = ""
                                    targetText = ""
                                    initialText = ""
                                    showForm = false
                                }
                            },
                            enabled = isFormValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_goal_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen,
                                disabledContainerColor = SuccessGreen.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Buat Rekening Impian",
                                fontWeight = FontWeight.Bold,
                                color = if (isFormValid) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // Active Goals List title
        item {
            Text(
                text = "Daftar Sasaran Tabungan",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Populate items
        if (goals.isEmpty()) {
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
                            imageVector = Icons.Filled.Flag,
                            contentDescription = "Empty goals",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum memiliki sasaran tabungan",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ketuk tombol '+' di kanan atas untuk mulai menabung barang impian.",
                            color = GreyText,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(goals) { goal ->
                val ratio = (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                val percentage = (ratio * 100).toInt()
                val isReached = goal.currentAmount >= goal.targetAmount
                val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
                val isExceeded = goal.currentAmount > goal.targetAmount
                val excessValue = if (isExceeded) goal.currentAmount - goal.targetAmount else 0.0

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("savings_item_card_${goal.name}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(SuccessGreen.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Stars,
                                        contentDescription = "Goal Icon",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = goal.name,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (isExceeded) "Lebih Target Sasaran (+ $currency ${formatMoney(excessValue)})" else if (isReached) "Impian Tercapai! 🎉" else "Sisa: $currency ${formatMoney(remaining)} lagi",
                                        color = if (isReached) SuccessGreen else WarningGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Add Contribution Quick Actions
                                IconButton(
                                    onClick = {
                                        activeGoalForContribution = goal
                                        contributionText = ""
                                        showContributionDialog = true
                                    },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.background, CircleShape).size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Savings,
                                        contentDescription = "Menabung",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Delete Goal Action
                                IconButton(
                                    onClick = { onDeleteGoal(goal) },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.background, CircleShape).size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Linear Progress Indicators
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = SuccessGreen,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Terkumpul: $currency ${formatMoney(goal.currentAmount)} dari $currency ${formatMoney(goal.targetAmount)}",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "$percentage%",
                                color = SuccessGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Estimated completion detail
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = "Kalender",
                                tint = SecondaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Estimasi target tercapai: ${dateFormatter.format(Date(goal.targetDate))}",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal contribution bottom sheets or text Dialogs
    if (showContributionDialog && activeGoalForContribution != null) {
        val currentGoalItem = activeGoalForContribution!!
        val contributionAmount = parseMoneyToDouble(contributionText)
        val totalProposed = currentGoalItem.currentAmount + contributionAmount
        val excessLimit = currentGoalItem.targetAmount * 1.5
        val isContributionTooHigh = totalProposed > excessLimit

        val isProposingExcess = totalProposed > currentGoalItem.targetAmount && !isContributionTooHigh
        val proposedExcessValue = if (totalProposed > currentGoalItem.targetAmount) {
            totalProposed - currentGoalItem.targetAmount
        } else {
            0.0
        }

        AlertDialog(
            onDismissRequest = { showContributionDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = "Tambah Tabungan: ${currentGoalItem.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Masukkan nilai nominal tambahan tabungan untuk mempercepat tercapainya sasaran belanja Anda.",
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = contributionText,
                        onValueChange = { input ->
                            // Support negative too to withdraw option
                            if (input.isEmpty() || input.all { it.isDigit() || it == '-' || it == '.' || it == ',' }) {
                                contributionText = input
                            }
                        },
                        label = { Text("Jumlah Menabung ($currency)", color = if (isContributionTooHigh) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                        isError = isContributionTooHigh,
                        supportingText = {
                            if (isContributionTooHigh) {
                                Text(
                                    "Maksimal 150% target ($currency ${formatMoney(excessLimit)})",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 10.sp
                                )
                            } else if (isProposingExcess) {
                                Text(
                                    "Kelebihan target: + $currency ${formatMoney(proposedExcessValue)}",
                                    color = WarningGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isContributionTooHigh) MaterialTheme.colorScheme.error else PrimaryBlue,
                            unfocusedBorderColor = if (isContributionTooHigh) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = if (isContributionTooHigh) MaterialTheme.colorScheme.error else PrimaryBlue,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorLabelColor = MaterialTheme.colorScheme.error,
                            errorSupportingTextColor = MaterialTheme.colorScheme.error
                        ),
                        placeholder = { Text("cth: 500000", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contribution_amount_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isContributionTooHigh && contributionText.isNotBlank()) {
                            val amount = parseMoneyToDouble(contributionText)
                            onUpdateContribution(currentGoalItem, amount)
                            showContributionDialog = false
                        }
                    },
                    enabled = !isContributionTooHigh && contributionText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen,
                        disabledContainerColor = SuccessGreen.copy(alpha = 0.4f)
                    )
                ) {
                    Text("Menabung", color = if (!isContributionTooHigh && contributionText.isNotBlank()) Color.White else Color.White.copy(alpha = 0.5f))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showContributionDialog = false }
                ) {
                    Text("Batal", color = Color(0xFFEF4444))
                }
            }
        )
    }
}
