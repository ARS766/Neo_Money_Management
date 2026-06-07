package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.GreyText
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fullText = "Neo Money Management"
    var typedText by remember { mutableStateOf("") }
    var textVisible by remember { mutableStateOf(false) }
    var screenAlpha by remember { mutableStateOf(1f) }

    val animatedScreenAlpha by animateFloatAsState(
        targetValue = screenAlpha,
        animationSpec = tween(durationMillis = 800),
        label = "ScreenAlpha"
    )

    LaunchedEffect(Unit) {
        // Start typing text directly with a short delay for smoother entry
        delay(200)
        textVisible = true
        for (i in 1..fullText.length) {
            typedText = fullText.substring(0, i)
            delay(80) // Typing speed
        }
        
        // Wait 1 second when fully completed
        delay(1000)
        
        // Fade Out entire screen
        screenAlpha = 0f
        delay(800)
        
        // Callback
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .alpha(animatedScreenAlpha)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Typographic Typing Effect
            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = typedText,
                        color = Color(0xCC00E5FF),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.testTag("splash_title")
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Smart Budgeting, Better Future",
                        color = GreyText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
