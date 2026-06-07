package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import androidx.compose.ui.graphics.asImageBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    currency: String,
    onSaveTransaction: (title: String, amount: Double, date: Long, category: String, note: String, type: String) -> Unit,
    onNavigateBack: () -> Unit,
    incomeCategories: List<String> = listOf("Gaji", "Freelance", "Bonus", "Investasi", "Lainnya"),
    expenseCategories: List<String> = listOf("Makanan", "Transportasi", "Belanja", "Hiburan", "Pendidikan", "Kesehatan", "Tagihan", "Lainnya"),
    modifier: Modifier = Modifier,
    initialType: String = "EXPENSE"
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Screen Form States
    var selectedType by remember(initialType) { mutableStateOf(initialType) } // "INCOME" or "EXPENSE"
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var selectedCategory by remember(selectedType, incomeCategories, expenseCategories) { 
        mutableStateOf(
            if (selectedType == "INCOME") {
                if (incomeCategories.isNotEmpty()) incomeCategories[0] else ""
            } else {
                if (expenseCategories.isNotEmpty()) expenseCategories[0] else ""
            }
        ) 
    }

    var showScanDialog by remember { mutableStateOf(false) }

    var scannedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessingOcr by remember { mutableStateOf(false) }
    var ocrErrorMessage by remember { mutableStateOf<String?>(null) }

    fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: java.lang.Exception) {
            null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            scannedBitmap = bitmap
            ocrErrorMessage = null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bitmap = uriToBitmap(uri)
            if (bitmap != null) {
                scannedBitmap = bitmap
                ocrErrorMessage = null
            } else {
                ocrErrorMessage = "Gagal memuat gambar dari galeri"
            }
        }
    }

    // Date calculations helper
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedDate = calendar.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("add_transaction_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tambah Transaksi",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showScanDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Scan Struk",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // INCOME / EXPENSE Tabs selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                // EXPENSE Tab button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedType = "EXPENSE" }
                        .background(if (selectedType == "EXPENSE") DangerRed else Color.Transparent)
                        .padding(vertical = 12.dp)
                        .testTag("add_expense_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PENGELUARAN",
                        color = if (selectedType == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // INCOME Tab button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedType = "INCOME" }
                        .background(if (selectedType == "INCOME") SuccessGreen else Color.Transparent)
                        .padding(vertical = 12.dp)
                        .testTag("add_income_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PEMASUKAN",
                        color = if (selectedType == "INCOME") Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Input Nominal Amount
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "JUMLAH NOMINAL (${selectedType})",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currency,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        TextField(
                            value = amountText,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.all { it.isDigit() || it == '.' || it == ',' }) {
                                    amountText = input
                                }
                            },
                            placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 32.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("transaction_amount_input")
                        )
                    }
                }
            }

            // SCAN BANNER BUTTON
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showScanDialog = true }
                    .testTag("scan_receipt_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryBlue.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(PrimaryBlue.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "Scan Logo",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pindai Struk / Bon Belanja",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Masukkan data transaksi otomatis dengan scan struk belanjaan",
                            color = GreyText,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Buka",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Scanning Receipts Dialog Model
            if (showScanDialog) {
                var scanStep by remember { mutableStateOf(0) } // 0: setup, 1: scanning layout, 2: verify/view result
                var manualOcrText by remember { mutableStateOf("") }
                var selectedScanCategory by remember { mutableStateOf(expenseCategories.firstOrNull() ?: "Belanja") }
                
                var extractedMerchant by remember { mutableStateOf("") }
                var extractedAmount by remember { mutableStateOf("") }
                var extractedDate by remember { mutableStateOf(System.currentTimeMillis()) }
                var extractedNote by remember { mutableStateOf("") }

                val infiniteTransition = rememberInfiniteTransition(label = "laser")
                val laserOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "laser_y"
                )

                AlertDialog(
                    onDismissRequest = { showScanDialog = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = if (scanStep == 0) "Scan Struk Keuangan" 
                                       else if (scanStep == 1) "Memindai Struk..." 
                                       else "Hasil Pemindaian",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (scanStep == 0) {
                                Text(
                                    text = "Ambil foto struk belanja Anda untuk dipindai otomatis menggunakan deteksi OCR asli ML Kit, atau ketik teks secara manual.",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )

                                // REQUIREMENT: "pemilihan kategori tetap memilih sendiri saat akan scan"
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "1. PILIH KATEGORI (MANDIRI)",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        expenseCategories.forEach { cat ->
                                            val isSelected = cat == selectedScanCategory
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedScanCategory = cat },
                                                label = { Text(cat, fontSize = 11.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = DangerRed,
                                                    containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // Real Scanning Controls
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "2. AMBIL FOTO / INPUT TEKS STRUK",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { cameraLauncher.launch() },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CameraAlt,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Ambil Foto", fontSize = 11.sp, color = Color.White)
                                        }

                                        Button(
                                            onClick = { galleryLauncher.launch("image/*") },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.PhotoLibrary,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Pilih Galeri", fontSize = 11.sp, color = Color.White)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (scannedBitmap != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(2.dp, SuccessGreen, RoundedCornerShape(12.dp))
                                        ) {
                                            androidx.compose.foundation.Image(
                                                bitmap = scannedBitmap!!.asImageBitmap(),
                                                contentDescription = "Receipt Image Result",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                    .clickable { 
                                                        scannedBitmap = null
                                                    }
                                                    .padding(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = "Hapus foto",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "Atau silakan ketik teks struk belanjaan di bawah:",
                                            color = GreyText,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                        OutlinedTextField(
                                            value = manualOcrText,
                                            onValueChange = { 
                                                manualOcrText = it
                                            },
                                            placeholder = { Text("Ketik nama toko & total di sini (cth: Mart Super Rp 70.000)", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth().height(65.dp),
                                            textStyle = TextStyle(fontSize = 11.sp),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }

                            } else if (scanStep == 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.05f))
                                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (scannedBitmap != null) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            androidx.compose.foundation.Image(
                                                bitmap = scannedBitmap!!.asImageBitmap(),
                                                contentDescription = "Receipt Process",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                                            
                                            Text(
                                                text = "PROCESSING REAL OCR...",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier
                                                .padding(horizontal = 30.dp, vertical = 15.dp)
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.White)
                                                .border(1.dp, Color(0xFFE2E8F0))
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val monospacedText = TextStyle(
                                                color = Color.DarkGray, 
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = "MANUAL RECEIPT INPUT",
                                                fontWeight = FontWeight.Bold,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth(),
                                                style = monospacedText
                                            )
                                            Text("-----------------------------", style = monospacedText, maxLines = 1)
                                            Text(text = manualOcrText.take(100), style = monospacedText, maxLines = 3)
                                            Text("-----------------------------", style = monospacedText, maxLines = 1)
                                            Text(
                                                text = "TOTAL HARGA: Memindai...",
                                                fontWeight = FontWeight.Bold,
                                                style = monospacedText
                                            )
                                        }
                                    }

                                    // Moving laser scanning line
                                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                        val h = maxHeight
                                        val movingLaserY = h * laserOffset
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(3.dp)
                                                .offset(y = movingLaserY)
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            Color(0xFF10B981).copy(alpha = 0.1f),
                                                            Color(0xFF10B981),
                                                            Color(0xFF10B981).copy(alpha = 0.1f)
                                                        )
                                                    )
                                                )
                                        )
                                    }
                                }

                                LaunchedEffect(Unit) {
                                    if (scannedBitmap != null) {
                                        val image = InputImage.fromBitmap(scannedBitmap!!, 0)
                                        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                        isProcessingOcr = true
                                        ocrErrorMessage = null
                                        
                                        // Give tiny delay for laser visual scan animation
                                        kotlinx.coroutines.delay(1200)

                                        recognizer.process(image)
                                            .addOnSuccessListener { visionText ->
                                                isProcessingOcr = false
                                                val rawText = visionText.text
                                                if (rawText.isBlank()) {
                                                    extractedMerchant = "Struk Belanjaan"
                                                    extractedAmount = "0"
                                                } else {
                                                    val parsedResult = parseRealReceiptText(rawText)
                                                    extractedMerchant = parsedResult.first
                                                    extractedAmount = parsedResult.second.toInt().toString()
                                                }
                                                extractedNote = "Deteksi cerdas OCR asli"
                                                extractedDate = System.currentTimeMillis()
                                                scanStep = 2
                                            }
                                            .addOnFailureListener { e ->
                                                isProcessingOcr = false
                                                ocrErrorMessage = "Akurasi bermasalah: " + e.localizedMessage
                                                extractedMerchant = "Struk Belanjaan"
                                                extractedAmount = "0"
                                                extractedNote = "Gagal memindai teks"
                                                extractedDate = System.currentTimeMillis()
                                                scanStep = 2
                                            }
                                    } else {
                                        kotlinx.coroutines.delay(1800)
                                        val parsed = parseReceiptText(manualOcrText)
                                        extractedMerchant = parsed.first
                                        extractedAmount = parsed.second.toInt().toString()
                                        extractedNote = "Pemindaian teks manual"
                                        extractedDate = System.currentTimeMillis()
                                        scanStep = 2
                                    }
                                }
                            } else {
                                Text(
                                    text = "Konfirmasi detail bon belanjaan hasil deteksi cerdas.",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )

                                OutlinedTextField(
                                    value = extractedMerchant,
                                    onValueChange = { extractedMerchant = it },
                                    label = { Text("Nama Toko / Merchant", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(fontSize = 12.sp)
                                )

                                OutlinedTextField(
                                    value = extractedAmount,
                                    onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' || char == ',' }) extractedAmount = it },
                                    label = { Text("Jumlah Nominal Terdeteksi ($currency)", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(fontSize = 12.sp)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SuccessGreen.copy(alpha = 0.08f))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Kategori yang Anda Pilih",
                                            color = GreyText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = selectedScanCategory,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        if (scanStep == 0) {
                            Button(
                                onClick = { 
                                    if (scannedBitmap != null || manualOcrText.isNotBlank()) {
                                        scanStep = 1 
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                enabled = scannedBitmap != null || manualOcrText.isNotBlank(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Mulai Pindai", color = Color.White)
                            }
                        } else if (scanStep == 2) {
                            Button(
                                onClick = {
                                    title = extractedMerchant
                                    amountText = extractedAmount
                                    selectedCategory = selectedScanCategory
                                    note = extractedNote
                                    selectedDate = extractedDate
                                    selectedType = "EXPENSE"
                                    showScanDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Terapkan data ke form", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showScanDialog = false }
                        ) {
                            Text("Batal", color = Color(0xFFEF4444))
                        }
                    }
                )
            }

            // Form Fields Cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Transaction title / deskripsi
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nama Transaksi", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        placeholder = { Text("cth: Belanja Bulanan, Gaji Kantor", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_title_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Transaction Date Select
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
                                    Text("Tanggal Transaksi", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 11.sp)
                                    Text(
                                        text = dateFormatter.format(Date(selectedDate)),
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

                    // Category selection list
                    Column {
                        Text(
                            text = "Pilih Kategori",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        val categories = if (selectedType == "INCOME") incomeCategories else expenseCategories
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = cat == selectedCategory
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (selectedType == "EXPENSE") DangerRed else SuccessGreen,
                                        containerColor = MaterialTheme.colorScheme.background
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        selectedBorderColor = Color.Transparent,
                                        enabled = true,
                                        selected = isSelected
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("category_chip_$cat")
                                )
                            }
                        }
                    }

                    // Notes details inputs
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Catatan Tambahan (Opsional)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        placeholder = { Text("Ketik catatan transaksi...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save CTA Button
            Button(
                onClick = {
                    val finalAmount = parseMoneyToDouble(amountText)
                    onSaveTransaction(
                        title,
                        finalAmount,
                        selectedDate,
                        selectedCategory,
                        note,
                        selectedType
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_transaction_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == "EXPENSE") DangerRed else SuccessGreen
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Simpan",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Simpan Transaksi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

// Receipt Scanning Helper Algorithms

fun parseReceiptText(text: String): Pair<String, Double> {
    val lines = text.split("\n")
    var merchant = "Struk Belanjaan"
    var amount = 0.0
    
    if (lines.isNotEmpty() && lines[0].isNotBlank()) {
        merchant = lines[0].trim().take(30)
    }
    
    // Find numbers or amounts
    val numberRegex = Regex("""(?:Rp\.?\s*|Rp\s*)?(\d{1,3}(?:\.\d{3})+|\d{3,9})""")
    var maxVal = 0.0
    lines.forEach { line ->
        numberRegex.findAll(line).forEach { match ->
            val numStr = match.groupValues[1].replace(".", "").replace(",", "")
            val parsed = numStr.toDoubleOrNull() ?: 0.0
            if (parsed > maxVal && parsed < 50000000.0) {
                maxVal = parsed
            }
        }
    }
    
    if (maxVal > 0.0) {
        amount = maxVal
    }
    
    return Pair(merchant, amount)
}

fun parseRealReceiptText(text: String): Pair<String, Double> {
    val lines = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
    if (lines.isEmpty()) return Pair("Struk Belanjaan", 0.0)
    
    // Extract merchant name
    var merchant = "Struk Belanjaan"
    val blacklist = listOf("no", "telp", "receipt", "invoice", "struk", "nota", "copy", "tanggal", "date", "cashier", "kasir")
    for (line in lines) {
        val lower = line.lowercase()
        if (line.length >= 3 && blacklist.none { lower.contains(it) }) {
            merchant = line
            break
        }
    }
    if (merchant.length > 30) {
        merchant = merchant.take(30) + "..."
    }
    
    // Extract total amount
    var amount = 0.0
    val totalKeywords = listOf("total", "jumlah", "bayar", "nett", "grand total", "subtotal", "belanja", "cash")
    var foundFromKeyword = false
    
    for (line in lines) {
        val lower = line.lowercase()
        if (totalKeywords.any { lower.contains(it) }) {
            val numbers = Regex("""\d+[\d.,]*""").findAll(line).map { match ->
                val cleaned = match.value.replace(".", "").replace(",", "")
                cleaned.toDoubleOrNull() ?: 0.0
            }.filter { it > 500.0 && it < 10000000.0 }.toList()
            
            if (numbers.isNotEmpty()) {
                amount = numbers.maxOrNull() ?: 0.0
                foundFromKeyword = true
                break
            }
        }
    }
    
    if (!foundFromKeyword) {
        var maxVal = 0.0
        lines.forEach { line ->
            Regex("""\r?\n""").split(line).forEach { word ->
                Regex("""\d+[\d.,]*""").findAll(word).forEach { match ->
                    val cleaned = match.value.replace(".", "").replace(",", "")
                    val parsed = cleaned.toDoubleOrNull() ?: 0.0
                    if (parsed > maxVal && parsed >= 1000.0 && parsed < 10000000.0) {
                        maxVal = parsed
                    }
                }
            }
        }
        if (maxVal > 0.0) {
            amount = maxVal
        }
    }
    
    return Pair(merchant, amount)
}
