package com.example.financeapp.ui.features.scanner

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financeapp.data.model.CreateExpenseRequest
import com.example.financeapp.data.model.OcrReceiptItem
import com.example.financeapp.data.model.OcrReceiptResult
import com.example.financeapp.data.repository.FinanceRepository
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate

@Composable
fun Scanner(
    onSave: (List<CreateExpenseRequest>) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { FinanceRepository() }

    var isScannerStarted by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var ocrResult by remember { mutableStateOf<OcrReceiptResult?>(null) }

    // Editable items derived from OCR result
    var editableItems by remember { mutableStateOf<List<EditableReceiptItem>>(emptyList()) }

    val options = GmsDocumentScannerOptions.Builder()
        .setScannerMode(SCANNER_MODE_FULL)
        .setGalleryImportAllowed(true)
        .setPageLimit(1)
        .setResultFormats(RESULT_FORMAT_JPEG)
        .build()

    val scanner = remember { GmsDocumentScanning.getClient(options) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val gmsResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                val pageUri = gmsResult?.pages?.firstOrNull()?.imageUri

                if (pageUri != null) {
                    isUploading = true
                    errorMessage = null
                    scope.launch {
                        uploadAndProcess(context, repository, pageUri,
                            onSuccess = { ocrData ->
                                ocrResult = ocrData
                                editableItems = ocrData.items.map {
                                    EditableReceiptItem(name = it.name, price = it.price.toString())
                                }
                                isUploading = false
                            },
                            onError = { error ->
                                errorMessage = error
                                isUploading = false
                            }
                        )
                    }
                } else {
                    onCancel()
                }
            } else {
                onCancel()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!isScannerStarted) {
            scanner.getStartScanIntent(context as Activity)
                .addOnSuccessListener { intentSender ->
                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                    isScannerStarted = true
                }
                .addOnFailureListener {
                    onCancel()
                }
        }
    }

    // Show loading while uploading/processing
    if (isUploading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Processing receipt...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "OCR & LLM correction in progress",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    // Show error
    if (errorMessage != null && ocrResult == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    "Failed to process receipt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    errorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onCancel) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    // Show OCR review screen
    if (ocrResult != null) {
        OcrReviewScreen(
            ocrResult = ocrResult!!,
            editableItems = editableItems,
            onItemChange = { index, item -> editableItems = editableItems.toMutableList().also { it[index] = item } },
            onItemDelete = { index -> editableItems = editableItems.toMutableList().also { it.removeAt(index) } },
            onConfirm = {
                val today = LocalDate.now().toString()
                val expenses = editableItems.mapNotNull { item ->
                    val price = item.price.toDoubleOrNull()
                    if (item.name.isNotBlank() && price != null && price > 0) {
                        CreateExpenseRequest(
                            title = item.name,
                            category = "Shopping",
                            date = today,
                            amount = price
                        )
                    } else null
                }
                onSave(expenses)
            },
            onCancel = onCancel
        )
    }
}

@Composable
private fun OcrReviewScreen(
    ocrResult: OcrReceiptResult,
    editableItems: List<EditableReceiptItem>,
    onItemChange: (Int, EditableReceiptItem) -> Unit,
    onItemDelete: (Int) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Outlined.Close, contentDescription = "Cancel")
                }
                Text(
                    "Review Scanned Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val totalPrice = editableItems.sumOf { it.price.toDoubleOrNull() ?: 0.0 }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total (${editableItems.size} items)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "$${String.format("%.2f", totalPrice)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            enabled = editableItems.any {
                                it.name.isNotBlank() && (it.price.toDoubleOrNull() ?: 0.0) > 0
                            }
                        ) {
                            Text("Add Expenses")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Store name header
            if (!ocrResult.storeName.isNullOrBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "Store: ${ocrResult.storeName}",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item {
                Text(
                    "Edit items before adding as expenses:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Editable item rows
            itemsIndexed(editableItems) { index, item ->
                EditableItemRow(
                    item = item,
                    onNameChange = { onItemChange(index, item.copy(name = it)) },
                    onPriceChange = { onItemChange(index, item.copy(price = it)) },
                    onDelete = { onItemDelete(index) }
                )
            }

            if (editableItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No items detected on receipt",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableItemRow(
    item: EditableReceiptItem,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = onNameChange,
                    label = { Text("Item name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = item.price,
                    onValueChange = onPriceChange,
                    label = { Text("Price") },
                    prefix = { Text("$") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    isError = item.price.isNotEmpty() && item.price.toDoubleOrNull() == null
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Remove item",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private suspend fun uploadAndProcess(
    context: android.content.Context,
    repository: FinanceRepository,
    imageUri: Uri,
    onSuccess: (OcrReceiptResult) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val bytes = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: throw Exception("Could not read image")
        }

        val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
        val filePart = MultipartBody.Part.createFormData("file", "receipt.jpg", requestBody)

        val response = withContext(Dispatchers.IO) {
            repository.scanReceipt(filePart)
        }

        if (response.isSuccessful) {
            val result = response.body()
            if (result != null) {
                onSuccess(result)
            } else {
                onError("Empty response from server")
            }
        } else {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            onError("Server error: $errorBody")
        }
    } catch (e: Exception) {
        onError(e.message ?: "Failed to upload image")
    }
}

data class EditableReceiptItem(
    val name: String,
    val price: String
)
