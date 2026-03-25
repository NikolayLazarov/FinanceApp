package com.example.myapplication.services.scannerService

import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning

class Scanner {
//    // 1. Конфигуриране на опциите на скенера
//    val options = GmsDocumentScannerOptions.Builder()
//        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
//        .setGalleryImportAllowed(true)
//        .setPageLimit(1) // Обикновено една бележка е една снимка
//        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
//        .build()
//
//    val scanner = GmsDocumentScanning.getClient(options)
//
//    // 2. Регистриране на лаунчера за резултата
//    val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
//        if (result.resultCode == RESULT_OK) {
//            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
//            scanResult?.pages?.get(0)?.let { page ->
//                val imageUri = page.imageUri // Пътят до снимката на бележката
//                // Тук можете да покажете снимката или да я изпратите за OCR обработка
//            }
//        }
//    }
//
//    // 3. Функция, която да извикате при клик на бутон
//    fun startReceiptScan() {
//        scanner.getStartScanIntent(this)
//            .addOnSuccessListener { intentSender ->
//                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
//            }
//            .addOnFailureListener { e ->
//                // Обработка на грешка
//            }
//    }

}