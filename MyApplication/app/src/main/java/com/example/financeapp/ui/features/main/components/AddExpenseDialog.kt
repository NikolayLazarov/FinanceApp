package com.example.financeapp.ui.features.main.components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.financeapp.data.model.CreateExpenseRequest
import com.example.financeapp.data.model.Product
import com.example.financeapp.data.repository.FinanceRepository
import com.example.financeapp.ui.localization.LocalStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "AddExpenseDialog"

private val expenseCategories = listOf(
    "Food", "Transportation", "Utilities", "Healthcare", "Education",
    "Entertainment", "Shopping", "Travel", "Finance", "Other"
)

data class EditableScannedItem(
    val name: String,
    val price: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    expenseToEdit: Product? = null,
    onDismiss: () -> Unit,
    onConfirm: (CreateExpenseRequest) -> Unit,
    onConfirmMultiple: (List<CreateExpenseRequest>) -> Unit = {}
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { FinanceRepository() }

    var title by remember { mutableStateOf(expenseToEdit?.title ?: "") }
    var amount by remember { mutableStateOf(expenseToEdit?.amount?.toString() ?: "") }
    var selectedCategoryIndex by remember {
        mutableIntStateOf(
            if (expenseToEdit != null) {
                val index = expenseCategories.indexOfFirst { it.equals(expenseToEdit.category, ignoreCase = true) }
                if (index != -1) index else 0
            } else 0
        )
    }
    var categoryExpanded by remember { mutableStateOf(false) }
    var date by remember {
        mutableStateOf(
            expenseToEdit?.date?.take(10) ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }

    var isScanning by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }

    var scannedItems by remember { mutableStateOf<List<EditableScannedItem>?>(null) }
    var scannedStoreName by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isScanning = true
            scanError = null
            scope.launch {
                try {
                    val bytes = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: throw Exception("Could not read image")
                    }

                    val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val requestBody = bytes.toRequestBody(contentType.toMediaType())
                    val filePart = MultipartBody.Part.createFormData("file", "receipt.jpg", requestBody)

                    val response = withContext(Dispatchers.IO) {
                        repository.scanReceipt(filePart)
                    }

                    if (response.isSuccessful) {
                        val result = response.body()
                        if (result != null) {
                            Log.d(TAG, "=== OCR Result ===")
                            Log.d(TAG, "Store: ${result.storeName}")
                            Log.d(TAG, "Items (${result.itemCount}):")
                            result.items.forEach { item ->
                                Log.d(TAG, "  - ${item.name}: ${item.price}")
                            }
                            Log.d(TAG, "Total: ${result.total}")
                            Log.d(TAG, "Raw text: ${result.rawText}")
                            Log.d(TAG, "Corrected text: ${result.correctedText}")
                            Log.d(TAG, "==================")

                            scannedStoreName = result.storeName
                            scannedItems = result.items.map {
                                EditableScannedItem(
                                    name = it.name,
                                    price = String.format("%.2f", it.price)
                                )
                            }
                        } else {
                            scanError = "Empty response"
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e(TAG, "OCR error: $errorBody")
                        scanError = "Server error: ${response.code()}"
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "OCR exception", e)
                    scanError = e.message ?: "Scan failed"
                } finally {
                    isScanning = false
                }
            }
        }
    }

    // --- Scanned items review mode ---
    if (scannedItems != null) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)) {
                        Text(
                            strings.scannedItems,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!scannedStoreName.isNullOrBlank()) {
                            Text(
                                scannedStoreName!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            strings.reviewAndEditItems,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val items = scannedItems!!
                        if (items.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        strings.noItemsDetected,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(items) { index, item ->
                                ScannedItemRow(
                                    index = index + 1,
                                    item = item,
                                    onNameChange = { newName ->
                                        scannedItems = scannedItems!!.toMutableList().also {
                                            it[index] = item.copy(name = newName)
                                        }
                                    },
                                    onPriceChange = { newPrice ->
                                        scannedItems = scannedItems!!.toMutableList().also {
                                            it[index] = item.copy(price = newPrice)
                                        }
                                    },
                                    onDelete = {
                                        scannedItems = scannedItems!!.toMutableList().also {
                                            it.removeAt(index)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(modifier = Modifier.padding(24.dp)) {
                        val totalPrice = scannedItems!!.sumOf { it.price.toDoubleOrNull() ?: 0.0 }
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = strings.categoryDisplayName(expenseCategories[selectedCategoryIndex]),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.category) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                strings.categoryList.forEachIndexed { index, (_, displayName) ->
                                    DropdownMenuItem(
                                        text = { Text(displayName) },
                                        onClick = {
                                            selectedCategoryIndex = index
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${scannedItems!!.size} ${strings.itemsCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${strings.total}: $${String.format("%.2f", totalPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { scannedItems = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(strings.back, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = {
                                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                    val expenses = scannedItems!!.mapNotNull { item ->
                                        val price = item.price.toDoubleOrNull()
                                        if (item.name.isNotBlank() && price != null && price > 0) {
                                            CreateExpenseRequest(
                                                title = item.name,
                                                category = expenseCategories[selectedCategoryIndex],
                                                date = today,
                                                amount = price
                                            )
                                        } else null
                                    }
                                    onConfirmMultiple(expenses)
                                },
                                modifier = Modifier.weight(2f),
                                enabled = scannedItems!!.any {
                                    it.name.isNotBlank() && (it.price.toDoubleOrNull() ?: 0.0) > 0
                                },
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(strings.addAllExpenses, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // --- Normal single-expense form ---
    val isSaveEnabled = title.isNotBlank() && amount.isNotBlank()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (expenseToEdit == null) strings.newExpense else strings.editExpense,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (expenseToEdit == null) strings.trackNewSpending else strings.updateSpendingDetails,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (expenseToEdit == null) {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isScanning,
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.scanning, fontWeight = FontWeight.Medium)
                        } else {
                            Icon(
                                Icons.Outlined.DocumentScanner,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.scanReceiptImage, fontWeight = FontWeight.Medium)
                        }
                    }

                    if (scanError != null) {
                        Text(
                            text = scanError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.title) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(strings.amount) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = fieldColors
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = strings.categoryDisplayName(expenseCategories[selectedCategoryIndex]),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.category) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Category,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = MaterialTheme.shapes.small,
                        colors = fieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        strings.categoryList.forEachIndexed { index, (_, displayName) ->
                            DropdownMenuItem(
                                text = { Text(displayName) },
                                onClick = {
                                    selectedCategoryIndex = index
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text(strings.date) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    placeholder = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = fieldColors
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            strings.cancel,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                CreateExpenseRequest(
                                    id = expenseToEdit?.id,
                                    title = title,
                                    category = expenseCategories[selectedCategoryIndex],
                                    date = date,
                                    amount = amount.toDoubleOrNull() ?: 0.0
                                )
                            )
                        },
                        enabled = isSaveEnabled,
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (expenseToEdit == null) strings.saveExpense else strings.updateExpenseButton,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannedItemRow(
    index: Int,
    item: EditableScannedItem,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$index",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = onNameChange,
                    label = { Text(strings.name) },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = item.price,
                    onValueChange = onPriceChange,
                    label = { Text(strings.price) },
                    prefix = { Text("$") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    isError = item.price.isNotEmpty() && item.price.toDoubleOrNull() == null
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = strings.delete,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
