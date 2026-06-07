package com.example.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue

// Helper context wrapper extension to safely retrieve FragmentActivity
fun Context.findActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun LoginScreen(
    savedPin: String?,
    isBiometricEnabled: Boolean,
    onLoginSuccess: () -> Unit,
    onSetupNewPin: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSetupMode = savedPin == null
    var enteredPin by remember { mutableStateOf("") }
    
    val onBg = MaterialTheme.colorScheme.onBackground
    var statusColor by remember { mutableStateOf(onBg.copy(alpha = 0.6f)) }
    
    var statusText by remember { mutableStateOf(if (isSetupMode) "Buat PIN Baru (4 Digit)" else "Masukkan PIN Keamanan") }
    
    // Sync with theme updates
    LaunchedEffect(onBg) {
        if (statusColor != Color(0xFFEF4444) && statusColor != Color(0xFFF59E0B)) {
            statusColor = onBg.copy(alpha = 0.6f)
        }
    }

    // Native Biometric Prompt launcher
    val showRealBiometricPrompt = {
        val activity = context.findActivity()
        if (activity != null) {
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        statusText = "Error: $errString"
                        statusColor = Color(0xFFEF4444)
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onLoginSuccess()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        statusText = "Sidik jari tidak cocok!"
                        statusColor = Color(0xFFEF4444)
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Kunci Keamanan NMM")
                .setSubtitle("Gunakan sidik jari Anda")
                .setNegativeButtonText("Gunakan PIN")
                .build()

            try {
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                statusText = "Biometrik tidak didukung di perangkat ini."
                statusColor = Color(0xFFEF4444)
            }
        } else {
            statusText = "Konteks aktivitas tidak valid."
            statusColor = Color(0xFFEF4444)
        }
    }

    // Fingerprint auto-prompt on display
    LaunchedEffect(isBiometricEnabled, isSetupMode) {
        if (isBiometricEnabled && !isSetupMode) {
            showRealBiometricPrompt()
        }
    }

    // Process PIN input completions
    LaunchedEffect(enteredPin) {
        if (enteredPin.length == 4) {
            if (isSetupMode) {
                // Save new pin
                onSetupNewPin(enteredPin)
                onLoginSuccess()
            } else {
                if (enteredPin == savedPin) {
                    onLoginSuccess()
                } else {
                    // Fail PIN check
                    enteredPin = ""
                    statusText = "PIN Salah, silakan coba lagi!"
                    statusColor = Color(0xFFEF4444)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("login_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Lock",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Neo Money Management",
                    color = Color(0xCC00E5FF),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }

            // PIN Indicator Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                for (i in 0 until 4) {
                    val isActive = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(16.dp)
                            .background(
                                color = if (isActive) PrimaryBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    )
                }
            }

            // Numeric Keyboard Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIO", "0", "DEL")
                )

                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        when (key) {
                                            "DEL" -> {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                    statusText = if (isSetupMode) "Buat PIN Baru (4 Digit)" else "Masukkan PIN Keamanan"
                                                    statusColor = Color(0xFF94A3B8)
                                                }
                                            }
                                            "BIO" -> {
                                                if (!isSetupMode && isBiometricEnabled) {
                                                    showRealBiometricPrompt()
                                                } else if (isSetupMode) {
                                                    statusText = "Setup PIN terlebih dahulu."
                                                    statusColor = Color(0xFFF59E0B)
                                                } else {
                                                    statusText = "Biometrik belum aktif."
                                                    statusColor = Color(0xFFF59E0B)
                                                }
                                            }
                                            else -> {
                                                if (enteredPin.length < 4) {
                                                    enteredPin += key
                                                }
                                            }
                                        }
                                    }
                                    .background(
                                        color = if (key == "DEL" || key == "BIO") Color.Transparent else MaterialTheme.colorScheme.surface
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when (key) {
                                    "DEL" -> Icon(
                                        imageVector = Icons.Filled.Backspace,
                                        contentDescription = "Hapus",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                    "BIO" -> Icon(
                                        imageVector = Icons.Filled.Fingerprint,
                                        contentDescription = "Biometrik",
                                        tint = if (isBiometricEnabled) PrimaryBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                    )
                                    else -> Text(
                                        text = key,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 24.sp,
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
}
