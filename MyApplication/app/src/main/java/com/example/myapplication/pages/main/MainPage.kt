package com.example.myapplication.pages.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Product
import com.example.myapplication.models.ScannedDocument
import com.example.myapplication.pages.main.components.ProductsBlock
import com.example.myapplication.pages.main.components.RemainingBlock
import com.example.myapplication.pages.main.common.components.Title

@Composable
fun MainPage(
    products: List<Product>,
    scannedDocuments: List<ScannedDocument>,
    onNewScanner: () -> Unit,
    modifier: Modifier = Modifier
){
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Title("Today's Expenses")

        }
        RemainingBlock()

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Button(onClick = onNewScanner, modifier = Modifier.padding(horizontal = 16.dp).align(Alignment.CenterHorizontally) ) {
            Text("new")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        ProductsBlock(products)
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text("Scanned Documents", modifier = Modifier.padding(horizontal = 16.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(scannedDocuments) { doc ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text(doc.fileName)
                    Text("${doc.photoCount} photos")
                }
            }
        }
    }
}
