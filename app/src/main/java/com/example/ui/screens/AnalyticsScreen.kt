package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    transactions: List<Transaction>,
    currency: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // 1. Calculations for Pie Chart (Expense Distribution)
    val expensesList = remember(transactions) {
        transactions.filter { it.type == "EXPENSE" }
    }
    val totalExpenses = remember(expensesList) {
        expensesList.sumOf { it.amount }
    }
    val expenseDistribution = remember(expensesList, totalExpenses) {
        if (totalExpenses == 0.0) emptyList()
        else {
            expensesList.groupBy { it.category }
                .map { (cat, list) ->
                    val sum = list.sumOf { it.amount }
                    val percent = (sum / totalExpenses * 100).toFloat()
                    PieSlice(
                        category = cat,
                        amount = sum,
                        percentage = percent,
                        color = getSequentialColorForCategory(cat)
                    )
                }.sortedByDescending { it.amount }
        }
    }

    // 2. Income vs Expense Calculations
    val totalIncome = remember(transactions) {
        transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }

    // 3. Line Chart Trend calculations (Weekly/Daily)
    val trendPoints = remember(transactions) {
        // Chronological sort
        val sortedList = transactions.sortedBy { it.date }
        if (sortedList.isEmpty()) emptyList()
        else {
            // Aggregate balance progressively
            var runningBalance = 0.0
            sortedList.map { trx ->
                if (trx.type == "INCOME") {
                    runningBalance += trx.amount
                } else {
                    runningBalance -= trx.amount
                }
                runningBalance.toFloat()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Analitik Keuangan",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Visualisasikan kondisi dan trend keuangan Anda",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }

        if (transactions.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(20.dp),
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
                        contentDescription = "No data analytics",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Data transaksi masih kosong",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Catat transaksi Anda terlebih dahulu untuk memuat grafik analitik.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Card 1: Perbandingan Cash Flow Bar Chart (Income vs Expense)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Perbandingan Aliran Kas",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    CashFlowBarChart(
                        income = totalIncome.toFloat(),
                        expense = totalExpenses.toFloat(),
                        currency = currency
                    )
                }
            }

            // Card 2: Pie Chart (Expense Distribution)
            if (expenseDistribution.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Proporsi Pengeluaran",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Custom drawing Pie Chart
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .weight(1.2f),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(130.dp)) {
                                    var startAngle = -90f
                                    expenseDistribution.forEach { slice ->
                                        val sweepAngle = slice.percentage * 3.6f
                                        drawArc(
                                            color = slice.color,
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        startAngle += sweepAngle
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Total",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$currency\n${formatMoney(totalExpenses)}",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Pie Chart legends
                            Column(
                                modifier = Modifier.weight(1.8f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                expenseDistribution.take(5).forEach { slice ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(11.dp)
                                                    .background(slice.color, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = slice.category,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1
                                            )
                                        }
                                        Text(
                                            text = "${slice.percentage.toInt()}%",
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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

            // Card 3: Line Chart Trend (Monthly/Balance Vectors)
            if (trendPoints.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Tren Saldo Akumulatif",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pergerakan ketersediaan likuiditas saldo dompet",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        BalanceTrendLineChart(points = trendPoints)
                    }
                }
            }
        }
    }
}

data class PieSlice(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

fun getSequentialColorForCategory(cat: String): Color {
    return when (cat.lowercase()) {
        "makanan" -> Color(0xFFEF4444)      // Red
        "transportasi" -> Color(0xFF38BDF8) // Secondary light blue
        "belanja" -> Color(0xFFF59E0B)      // Warning Gold
        "hiburan" -> Color(0xFFEC4899)      // Pink
        "pendidikan" -> Color(0xFF8B5CF6)   // Purple
        "kesehatan" -> Color(0xFF10B981)    // Accent green
        "tagihan" -> Color(0xFFF43F5E)      // Rose
        "gaji" -> Color(0xFF22C55E)         // Success
        "freelance" -> Color(0xFF06B6D4)    // Cyan
        "bonus" -> Color(0xFFFACC15)        // Yellow
        "investasi" -> Color(0xFF3B82F6)    // Primary
        else -> Color(0xFF64748B)           // Slate Grey
    }
}

// Custom side-by-side vertical bar chart layout
@Composable
fun CashFlowBarChart(
    income: Float,
    expense: Float,
    currency: String,
    modifier: Modifier = Modifier
) {
    val totalCashValue = (income + expense)
    val ratioIncome = if (totalCashValue > 0) income / totalCashValue else 0.5f
    val ratioExpense = if (totalCashValue > 0) expense / totalCashValue else 0.5f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Income Bar Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = "$currency\n${formatMoney(income.toDouble())}",
                    color = SuccessGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height((130 * ratioIncome).dp.coerceAtLeast(16.dp))
                        .background(
                            Brush.verticalGradient(listOf(SuccessGreen, Color(0xFF15803D))),
                            RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                        )
                )
                Text(
                    text = "Pemasukan",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Expense Bar Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = "$currency\n${formatMoney(expense.toDouble())}",
                    color = DangerRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height((130 * ratioExpense).dp.coerceAtLeast(16.dp))
                        .background(
                            Brush.verticalGradient(listOf(DangerRed, Color(0xFFB91C1C))),
                            RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                        )
                )
                Text(
                    text = "Pengeluaran",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Custom trend vector rendering using connected vectors canvas
@Composable
fun BalanceTrendLineChart(
    points: List<Float>,
    modifier: Modifier = Modifier
) {
    val minVal = points.minOrNull() ?: 0f
    val maxVal = points.maxOrNull() ?: 100f
    val range = if (maxVal - minVal == 0f) 1f else maxVal - minVal

    val canvasBg = MaterialTheme.colorScheme.background
    val dotColor = MaterialTheme.colorScheme.onBackground

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(canvasBg)
    ) {
        val width = size.width
        val height = size.height

        val padding = 20f
        val chartWidth = width - (padding * 2)
        val chartHeight = height - (padding * 2)

        val stepX = chartWidth / (points.size - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { i, value ->
            val relativeValue = (value - minVal) / range
            val x = padding + (i * stepX)
            val y = padding + (chartHeight - (relativeValue * chartHeight))

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (i == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.lineTo(padding, height)
                fillPath.close()
            }
        }

        // 1. Draw glowing background fill gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x332563EB), Color.Transparent)
            )
        )

        // 2. Draw sharp visual line overlay vectors
        drawPath(
            path = path,
            color = PrimaryBlue,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 3. Draw dot checkpoints on vectors
        points.forEachIndexed { i, value ->
            val relativeValue = (value - minVal) / range
            val x = padding + (i * stepX)
            val y = padding + (chartHeight - (relativeValue * chartHeight))

            drawCircle(
                color = dotColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = PrimaryBlue,
                radius = 2.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
