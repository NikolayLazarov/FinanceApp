package com.example.myapplication.pages

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.ScannedDocument
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

@Composable
fun Scanner(onSave: (List<ScannedDocument>) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var scannedDocs by remember { mutableStateOf<List<ScannedDocument>>(emptyList()) }
    var isScannerStarted by remember { mutableStateOf(false) }

    val options = GmsDocumentScannerOptions.Builder()
        .setScannerMode(SCANNER_MODE_FULL)
        .setGalleryImportAllowed(true)
        .setPageLimit(5)
        .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
        .build()

    val scanner = remember { GmsDocumentScanning.getClient(options) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val gmsResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                gmsResult?.let { scanningResult ->
                    val newDocs = mutableListOf<ScannedDocument>()
                    
                    // Add images info
                    scanningResult.pages?.let { pages ->
                        if (pages.isNotEmpty()) {
                            newDocs.add(ScannedDocument("Images Session", pages.size))
                        }
                    }
                    
                    // Add PDF info
                    scanningResult.pdf?.let { pdf ->
                        newDocs.add(ScannedDocument(pdf.uri.lastPathSegment ?: "Scanned.pdf", 1))
                    }
                    
                    scannedDocs = newDocs
                }
            } else {
                if (scannedDocs.isEmpty()) {
                    onCancel()
                }
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

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Scanned Results", modifier = Modifier.padding(bottom = 16.dp))

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(scannedDocs) { doc ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = doc.fileName)
                    Text(text = "Photos: ${doc.photoCount}")
                }
            }
        }

        Button(
            onClick = { onSave(scannedDocs) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("save")
        }
    }
}
