package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryBlue

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tint: Color
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "Smart Tracking",
                description = "Catat pemasukan dan pengeluaran harian Anda secara terperinci dan otomatis terklasifikasi.",
                icon = Icons.Filled.AccountBalanceWallet,
                tint = PrimaryBlue
            ),
            OnboardingPage(
                title = "Strict Budget Planner",
                description = "Kelola anggaran bulanan per kategori untuk mencegah pemborosan dengan notifikasi pintar.",
                icon = Icons.Filled.PieChart,
                tint = SecondaryBlue
            ),
            OnboardingPage(
                title = "Savings Target",
                description = "Wujudkan impian finansial Anda dengan monitor tabungan berjangka yang intuitif.",
                icon = Icons.Filled.Stars,
                tint = Color(0xFF22C55E)
            )
        )
    }

    var currentPageIndex by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("onboarding_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Neo Money Management",
                    color = Color(0xCC00E5FF),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                )
            }

            // Animated Slide Content
            AnimatedContent(
                targetState = currentPageIndex,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { it } with fadeOut() + slideOutHorizontally { -it }
                },
                label = "OnboardingSlideChange",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) { targetIndex ->
                val page = pages[targetIndex]
                val scrollState = rememberScrollState()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = page.title,
                            modifier = Modifier.size(80.dp),
                            tint = page.tint
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = page.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = page.description,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            // Bottom Navigation Indicators and Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicator Bullets
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    pages.forEachIndexed { index, _ ->
                        val isSelected = index == currentPageIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = if (isSelected) 24.dp else 8.dp, height = 8.dp)
                                .background(
                                    color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable { currentPageIndex = index }
                        )
                    }
                }

                // CTA Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (currentPageIndex > 0) {
                        OutlinedButton(
                            onClick = { currentPageIndex-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("onboarding_back_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                            border = BorderStroke(1.dp, PrimaryBlue)
                        ) {
                            Text(
                                text = "Kembali",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (currentPageIndex < pages.size - 1) {
                                currentPageIndex++
                            } else {
                                onOnboardingFinished()
                            }
                        },
                        modifier = Modifier
                            .weight(if (currentPageIndex > 0) 1.5f else 1f)
                            .height(56.dp)
                            .testTag("onboarding_next_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (currentPageIndex == pages.size - 1) "Mulai Sekarang" else "Lanjutkan",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
