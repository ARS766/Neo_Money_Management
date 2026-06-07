package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.content.ClipboardManager
import android.content.ClipData
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.Budget
import com.example.data.model.SavingsGoal
import com.example.ui.theme.*
import com.example.util.TranslationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentCurrency: String,
    onCurrencyChange: (String) -> Unit,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    savedPin: String?,
    onSavePin: (String?) -> Unit,
    isBiometricEnabled: Boolean,
    onBiometricChange: (Boolean) -> Unit,
    onResetData: () -> Unit,
    transactions: List<Transaction>,
    budgets: List<Budget> = emptyList(),
    goals: List<SavingsGoal> = emptyList(),
    onRestoreBackup: (String, (Boolean, String) -> Unit) -> Unit = { _, _ -> },
    onGenerateBackup: () -> String = { "" },
    isAutoBackupEnabled: Boolean = false,
    onAutoBackupChange: (Boolean) -> Unit = {},
    selectedLanguage: String = "ID",
    onLanguageChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Form settings dialogue states
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinText by remember { mutableStateOf("") }
    var showResetConfirm by remember { mutableStateOf(false) }

    // Backup & Restore states
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var backupTabSelected by remember { mutableStateOf(true) } // true: Backup, false: Restore
    var pasteBackupText by remember { mutableStateOf("") }
    var restoreFeedbackMessage by remember { mutableStateOf("") }
    var restoreFeedbackSuccess by remember { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Settings page title
        Column {
            Text(
                text = TranslationHelper.translate("settings", selectedLanguage),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = TranslationHelper.translate("settings_subtitle", selectedLanguage),
                color = GreyText,
                fontSize = 12.sp
            )
        }

        // Section 1: Visual themes and Currencies
        Text(
            text = TranslationHelper.translate("personality_display", selectedLanguage),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Dark mode Switch Option with interactive rotating sun / moon icon and animation
                val rotationAngle by animateFloatAsState(
                    targetValue = if (isDarkMode) 360f else 0f,
                    animationSpec = tween(durationMillis = 600)
                )
                val scaleFactor by animateFloatAsState(
                    targetValue = if (isDarkMode) 1.15f else 1.0f,
                    animationSpec = tween(durationMillis = 400)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Filled.NightsStay else Icons.Filled.WbSunny,
                            contentDescription = if (isDarkMode) "Dark Theme" else "Light Theme",
                            tint = if (isDarkMode) Color(0xFFF1C40F) else Color(0xFFFFB300),
                            modifier = Modifier
                                .rotate(rotationAngle)
                                .scale(scaleFactor)
                                .size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = TranslationHelper.translate("dark_mode", selectedLanguage),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = TranslationHelper.translate("dark_mode_desc", selectedLanguage),
                                color = GreyText,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeChange,
                        thumbContent = {
                            val thumbIcon = if (isDarkMode) Icons.Filled.NightsStay else Icons.Filled.WbSunny
                            Icon(
                                imageVector = thumbIcon,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                tint = if (isDarkMode) Color(0xFFF1C40F) else Color(0xFFFFB300)
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF22D3EE),
                            checkedTrackColor = PrimaryBlue,
                            uncheckedThumbColor = Color(0xFFFFB300),
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))

                // Currency Dropdown setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCurrencyDialog = true }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = "Mata Uang",
                            tint = SecondaryBlue
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = TranslationHelper.translate("currency", selectedLanguage),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = TranslationHelper.translate("currency_desc", selectedLanguage),
                                color = GreyText,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Text(
                        text = currentCurrency,
                        color = SecondaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))

                // Language selection setting
                var showLanguageDialog by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true }
                        .padding(12.dp)
                        .testTag("row_language_selection"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = "Language",
                            tint = SecondaryBlue
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = TranslationHelper.translate("language", selectedLanguage),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = TranslationHelper.translate("language_desc", selectedLanguage),
                                color = GreyText,
                                fontSize = 11.sp
                            )
                        }
                    }
                    val currentLangName = when (selectedLanguage) {
                        "ID" -> "Bahasa Indonesia"
                        "EN" -> "English (US)"
                        "ES" -> "Español"
                        "UK" -> "English (UK)"
                        "JA" -> "日本語"
                        "ZH" -> "简体中文"
                        else -> "Bahasa Indonesia"
                    }
                    Text(
                        text = currentLangName,
                        color = SecondaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Modal Language Dialog Select
                if (showLanguageDialog) {
                    val languagesList = listOf(
                        "ID" to "Bahasa Indonesia",
                        "EN" to "English (US)",
                        "ES" to "Español",
                        "UK" to "English (UK)",
                        "JA" to "日本語",
                        "ZH" to "简体中文"
                    )
                    AlertDialog(
                        onDismissRequest = { showLanguageDialog = false },
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        title = { Text(TranslationHelper.translate("language", selectedLanguage), fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                languagesList.forEach { (langCode, langName) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onLanguageChange(langCode)
                                                showLanguageDialog = false
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = langName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                        if (langCode == selectedLanguage) {
                                            Icon(imageVector = Icons.Filled.Check, contentDescription = "Active", tint = SecondaryBlue)
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {}
                    )
                }
            }
        }

        // Section 2: Financial Securitites
        Text(
            text = TranslationHelper.translate("financial_security", selectedLanguage),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Pin Config row option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            pinText = ""
                            showPinDialog = true
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Security PIN",
                            tint = WarningGold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(TranslationHelper.translate("pin_lock", selectedLanguage), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (savedPin == null) TranslationHelper.translate("pin_inactive", selectedLanguage) else TranslationHelper.translate("pin_active_desc", selectedLanguage),
                                color = GreyText,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Text(
                        text = if (savedPin == null) TranslationHelper.translate("pin_setting", selectedLanguage) else TranslationHelper.translate("pin_active", selectedLanguage),
                        color = if (savedPin == null) WarningGold else SuccessGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))

                // Fingerprint biometric Toggle option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "Biometrik",
                            tint = WarningGold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(TranslationHelper.translate("biometric", selectedLanguage), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(TranslationHelper.translate("biometric_desc", selectedLanguage), color = GreyText, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { onBiometricChange(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = WarningGold),
                        enabled = savedPin != null // Must define PIN first before biometrics
                    )
                }
            }
        }

        // Section 3: Backup & Exports real systems
        Text(
            text = TranslationHelper.translate("backup_export", selectedLanguage),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // PDF Sharing Simulation Output text report
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { shareReportAsPDFStyleText(context, transactions, currentCurrency) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = "Export PDF",
                            tint = SuccessGreen
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = TranslationHelper.translate("export_pdf", selectedLanguage),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = TranslationHelper.translate("export_pdf_desc", selectedLanguage),
                                color = GreyText,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Export",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))

                // Excel CSV Exporting sharing
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { shareReportAsExcelStyleCSV(context, transactions) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.TableChart,
                            contentDescription = "Export CSV",
                            tint = SuccessGreen
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = TranslationHelper.translate("export_csv", selectedLanguage),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = TranslationHelper.translate("export_csv_desc", selectedLanguage),
                                color = GreyText,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Export",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))

                // Backup Database interactive backup and restore dialog trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBackupRestoreDialog = true }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CloudUpload,
                            contentDescription = "Backup",
                            tint = SuccessGreen
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = TranslationHelper.translate("backup_db", selectedLanguage),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = TranslationHelper.translate("backup_db_desc", selectedLanguage),
                                color = GreyText,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))

                // Auto Cloud Backup Switch Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudUpload,
                            contentDescription = "Pencadangan Otomatis",
                            tint = SuccessGreen
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = TranslationHelper.translate("auto_backup", selectedLanguage),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = TranslationHelper.translate("auto_backup_desc", selectedLanguage),
                                color = GreyText,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = isAutoBackupEnabled,
                        onCheckedChange = onAutoBackupChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SuccessGreen,
                            checkedTrackColor = PrimaryBlue,
                            uncheckedThumbColor = GreyText,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("switch_auto_backup")
                    )
                }
            }
        }

        // Section 4: Support & Feedback
        Text(
            text = TranslationHelper.translate("support_feedback", selectedLanguage),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth().testTag("card_support_feedback"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Apps Feedback Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("neo.developer.apps@gmail.com"))
                                putExtra(Intent.EXTRA_SUBJECT, "Feedback & Masukan Aplikasi NMM")
                                putExtra(Intent.EXTRA_TEXT, "Halo Developer,\n\nBerikut masukan saya untuk aplikasi NMM:\n\n")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Tidak ada aplikasi email yang ditemukan", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("feedback_apps_row")
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Feedback,
                            contentDescription = "Apps Feedback",
                            tint = WarningGold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = TranslationHelper.translate("feedback_apps", selectedLanguage),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = TranslationHelper.translate("feedback_desc", selectedLanguage),
                                color = GreyText,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Email Developer",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Wipe / Reset App controls with red accent warning styling
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showResetConfirm = true },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = "Reset",
                    tint = DangerRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = TranslationHelper.translate("clear_data", selectedLanguage),
                        color = DangerRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = TranslationHelper.translate("clear_data_desc", selectedLanguage),
                        color = DangerRed,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    // Modal Currencies Dialog Select
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text(TranslationHelper.translate("select_currency_symbol", selectedLanguage), fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Rp", "$", "€", "£", "¥ (Yen)", "元 (Yuan)").forEach { symb ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onCurrencyChange(symb)
                                    showCurrencyDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = symb, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                            if (symb == currentCurrency) {
                                Icon(imageVector = Icons.Filled.Check, contentDescription = "Active", tint = SecondaryBlue)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Modal Interactive Backup & Restore Dialog
    if (showBackupRestoreDialog) {
        val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
        val generatedBackupCode = remember { onGenerateBackup() }

        AlertDialog(
            onDismissRequest = { 
                showBackupRestoreDialog = false
                restoreFeedbackMessage = ""
                restoreFeedbackSuccess = null
                pasteBackupText = ""
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Backup & Pemulihan Data",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { showBackupRestoreDialog = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Tutup")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Custom Tab Switcher (Backup vs Restore)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (backupTabSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { backupTabSelected = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Cadangkan Data",
                                color = if (backupTabSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!backupTabSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { backupTabSelected = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Pulihkan Data",
                                color = if (!backupTabSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (backupTabSelected) {
                        // BACKUP TAB CONTENT
                        Text(
                            text = "Kode di bawah berisi seluruh transaksi, anggaran, dan tabungan Anda secara terenkripsi aman.",
                            fontSize = 12.sp,
                            color = GreyText
                        )

                        androidx.compose.foundation.text.selection.SelectionContainer {
                            OutlinedTextField(
                                value = generatedBackupCode,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kode Backup") },
                                textStyle = TextStyle(
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 10
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val clip = ClipData.newPlainText("Backup Keuangan", generatedBackupCode)
                                    clipboardManager.setPrimaryClip(clip)
                                    Toast.makeText(context, "Kode backup berhasil disalin!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Salin Kode", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    try {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, generatedBackupCode)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Bagikan Kode Backup"))
                                    } catch (_: Exception) {}
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bagikan", fontSize = 12.sp)
                            }
                        }
                    } else {
                        // RESTORE TAB CONTENT
                        Text(
                            text = "Masukkan atau tempel kode backup Anda di bawah ini untuk mengembalikan riwayat keuangan Anda.",
                            fontSize = 12.sp,
                            color = GreyText
                        )

                        OutlinedTextField(
                            value = pasteBackupText,
                            onValueChange = { 
                                pasteBackupText = it
                                restoreFeedbackMessage = ""
                                restoreFeedbackSuccess = null
                            },
                            label = { Text("Kode Backup Keuangan") },
                            placeholder = { Text("Tempel kode backup berupa string acak di sini...") },
                            textStyle = TextStyle(
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 10
                        )

                        if (restoreFeedbackMessage.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (restoreFeedbackSuccess == true) 
                                        Color(0x1510B981) else Color(0x15EF4444)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (restoreFeedbackSuccess == true) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                        contentDescription = null,
                                        tint = if (restoreFeedbackSuccess == true) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = restoreFeedbackMessage,
                                        fontSize = 12.sp,
                                        color = if (restoreFeedbackSuccess == true) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                onRestoreBackup(pasteBackupText) { success, msg ->
                                    restoreFeedbackSuccess = success
                                    restoreFeedbackMessage = msg
                                    if (success) {
                                        pasteBackupText = ""
                                    }
                                }
                            },
                            enabled = pasteBackupText.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mulai Pulihkan Data", fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Modal Create/Disable Pin Code set up popup
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = if (savedPin == null) "Pasang PIN Baru" else "Pengaturan PIN Keamanan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Gunakan 4 digit angka desimal untuk membatasi akses tidak berizin kepada rincian neraca keuangan Anda.",
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 4) pinText = input
                        },
                        label = { Text("4 PIN Digit Angka", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("1234", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("pin_code_setting_input")
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (savedPin != null) {
                        Button(
                            onClick = {
                                onSavePin(null)
                                onBiometricChange(false)
                                showPinDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                        ) {
                            Text("Matikan PIN", color = Color.White)
                        }
                    }

                    Button(
                        onClick = {
                            if (pinText.length == 4) {
                                onSavePin(pinText)
                                showPinDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        enabled = pinText.length == 4
                    ) {
                        Text("Simpan", color = Color.White)
                    }
                }
            }
        )
    }

    // Reset Confirmation layout modal popup
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Konfirmasi Hapus Seluruh Data?", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Perhatian! Seluruh rincian pemasukan, pengeluaran, anggaran, sasaran target tabungan, dan PIN keamanan akan terhapus secara permanen dari penyimpanan lokal Anda. Tindakan ini tidak dapat dibatalkan.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetData()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Hapus Permanen", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirm = false }
                ) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        )
    }
}

// FORMAT Ekspor PDF sharing formatted text generator
private fun shareReportAsPDFStyleText(context: android.content.Context, list: List<Transaction>, currency: String) {
    if (list.isEmpty()) {
        try {
            val noneReport = "Laporan Neraca Neo Money Management kosong."
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, noneReport)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Ekspor PDF"))
        } catch (_: Exception) {}
        return
    }

    val totalInc = list.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExp = list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val bal = totalInc - totalExp

    val pdfDocument = android.graphics.pdf.PdfDocument()
    
    // Define page size and painter (A4 size: 595 x 842 points)
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas
    
    val paint = android.graphics.Paint()
    paint.isAntiAlias = true
    
    var yPos = 50f
    
    // Title header
    paint.textSize = 18f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.DKGRAY
    canvas.drawText("NEO MONEY MANAGEMENT REPORT", 40f, yPos, paint)
    
    yPos += 20f
    paint.textSize = 11f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Tagline: Smart Budgeting, Better Future", 40f, yPos, paint)
    
    yPos += 15f
    canvas.drawLine(40f, yPos, 555f, yPos, paint)
    
    // Quick recap summary card inside canvas
    yPos += 30f
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 13f
    paint.isFakeBoldText = true
    canvas.drawText("RESUME KELAYAKAN LIKUIDITAS:", 40f, yPos, paint)
    
    yPos += 20f
    paint.textSize = 11f
    paint.isFakeBoldText = false
    canvas.drawText("- Total Pemasukan: $currency ${formatMoney(totalInc)}", 40f, yPos, paint)
    yPos += 18f
    canvas.drawText("- Total Pengeluaran: $currency ${formatMoney(totalExp)}", 40f, yPos, paint)
    yPos += 18f
    canvas.drawText("- Bersih (Saldo): $currency ${formatMoney(bal)}", 40f, yPos, paint)
    
    yPos += 15f
    canvas.drawLine(40f, yPos, 555f, yPos, paint)
    
    yPos += 25f
    paint.textSize = 13f
    paint.isFakeBoldText = true
    canvas.drawText("LOG TRANSAKSI TERBARU:", 40f, yPos, paint)
    
    yPos += 20f
    paint.textSize = 10f
    paint.isFakeBoldText = false
    
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    list.forEachIndexed { i, trx ->
        // Handle pagination overflow
        if (yPos > 780f) {
            pdfDocument.finishPage(page)
            val newPageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
            page = pdfDocument.startPage(newPageInfo)
            canvas = page.canvas
            yPos = 50f
            
            // Render pagination header
            paint.textSize = 10f
            paint.color = android.graphics.Color.GRAY
            canvas.drawText("NEO MONEY MANAGEMENT REPORT - Halaman ${pdfDocument.pages.size + 1}", 40f, yPos, paint)
            yPos += 15f
            canvas.drawLine(40f, yPos, 555f, yPos, paint)
            yPos += 20f
            paint.color = android.graphics.Color.BLACK
        }
        
        val sign = if (trx.type == "INCOME") "(+)" else "(-)"
        paint.isFakeBoldText = true
        canvas.drawText("${i + 1}. [${sdf.format(java.util.Date(trx.date))}] - ${trx.title} $sign", 40f, yPos, paint)
        
        yPos += 14f
        paint.isFakeBoldText = false
        canvas.drawText("   Kategori: ${trx.category} | Nilai: $currency ${formatMoney(trx.amount)}", 40f, yPos, paint)
        
        if (trx.note.isNotBlank()) {
            yPos += 14f
            canvas.drawText("   Catatan: ${trx.note}", 40f, yPos, paint)
        }
        yPos += 18f
    }
    
    if (yPos > 780f) {
        pdfDocument.finishPage(page)
        val newPageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
        page = pdfDocument.startPage(newPageInfo)
        canvas = page.canvas
        yPos = 50f
    }
    
    yPos += 10f
    canvas.drawLine(40f, yPos, 555f, yPos, paint)
    yPos += 15f
    paint.textSize = 9f
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Dihasilkan secara instan oleh NMM App.", 40f, yPos, paint)
    
    pdfDocument.finishPage(page)
    
    val pdfFile = java.io.File(context.cacheDir, "laporan_keuangan.pdf")
    try {
        pdfDocument.writeTo(java.io.FileOutputStream(pdfFile))
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        pdfDocument.close()
    }

    try {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = androidx.core.content.FileProvider.getUriForFile(context, authority, pdfFile)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "application/pdf"
            clipData = android.content.ClipData.newRawUri("", contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Ekspor PDF").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
    } catch (_: Exception) {}
}

// FORMAT Ekspor EXCEL CSV formatted report table
private fun shareReportAsExcelStyleCSV(context: android.content.Context, list: List<Transaction>) {
    val builder = StringBuilder()
    builder.append("ID,Tanggal,Judul Transaksi,Tipe,Kategori,Nominal,Catatan\n")
    
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    list.forEach { trx ->
        val line = "\"${trx.id}\",\"${sdf.format(java.util.Date(trx.date))}\",\"${trx.title.replace("\"", "\"\"")}\",\"${trx.type}\",\"${trx.category}\",\"${trx.amount}\",\"${trx.note.replace("\"", "\"\"")}\"\n"
        builder.append(line)
    }

    val csvFile = java.io.File(context.cacheDir, "laporan_keuangan.csv")
    try {
        csvFile.writeText(builder.toString())
    } catch (e: Exception) {
        e.printStackTrace()
    }

    try {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = androidx.core.content.FileProvider.getUriForFile(context, authority, csvFile)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "text/csv"
            clipData = android.content.ClipData.newRawUri("", contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Ekspor Excel CSV").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
    } catch (_: Exception) {}
}

// FORMAT Backup database sharing simulation
private fun backupDatabaseSim(context: android.content.Context, list: List<Transaction>) {
    val sizeText = "NMM BACKUP DATA PACKAGE\nTotal Records: ${list.size}\nBuild Timestamp: ${System.currentTimeMillis()}"
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sizeText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Cadangkan Data"))
    } catch (_: Exception) {}
}
