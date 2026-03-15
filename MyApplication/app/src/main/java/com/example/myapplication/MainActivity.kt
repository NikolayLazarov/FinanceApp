package com.example.myapplication

import com.example.myapplication.components.BarChart
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.components.BezierCurve
import com.example.myapplication.services.RetrofitClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 1. Създаваме състояние за нашия списък
            var products by remember { mutableStateOf<List<Product>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                try {
                    products = RetrofitClient.apiService.getProducts()
                } catch (e: Exception) {
                    // Тук може да добавите логика за грешки
                } finally {
                    isLoading = false
                }
            }

            // 3. UI Логика
            if (isLoading) {
                CircularProgressIndicator() // Индикатор за зареждане
            } else {
//                MyApplicationTheme {
//                    MyApplicationApp(products)
//                }
                    MainPage(products)

//                MainPage() // Показване на списъка
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun MyApplicationApp(products: List<Product> = emptyList() ) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column() {
                when (currentDestination){
                    AppDestinations.HOME -> MainPage(products = products, modifier = Modifier.padding(innerPadding))
                    AppDestinations.FAVORITES -> GraphsPage(modifier = Modifier.padding(innerPadding))
                    AppDestinations.PROFILE -> Profile(modifier = Modifier.padding(innerPadding) )

                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}



//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    MyApplicationTheme {
//        MainPage()
//    }
//}

@Composable
fun MainPage(products: List<Product>, modifier: Modifier = Modifier){
    Column() {
        Title("Today's Expenses")
        RemainingBlock()
        ProductsBlock(products)
    }
}

@Composable
fun GraphsPage(modifier: Modifier = Modifier){
    val dailyExpensesList = listOf(0f, 70f, 60f, 60f, 50f, 10f,16f,110f)
    val daysList = listOf("","Mon", "Thu", "Wed", "Thur", "Fri", "Sat", "Sun")
    val daysListInt = listOf(0,1,2,3,4,5,6,7)
    val max = dailyExpensesList.max()
    val count = 5
    val step = max/ (count - 1)  // integer division


//    months
    val tags = listOf("Food", "Drinks", "Leisure,", "Friends", "Presents")


    val yValues = List(count) { it *step.toInt() }
    Column() {
        Title("Graphs")
        BarChart(
            xValuesInt = daysListInt,
        xValues = daysList,
        yValues = yValues,
        interval = 10,
        modifier = Modifier.size(300.dp),
        points = dailyExpensesList
    )
        BezierCurve(
            modifier = Modifier.size(300.dp),
//            xValuesInt2 = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,13,14,15,16,17,18,19,20,21),
            xValuesInt = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,13,14,15,16,17,18,19,20,21).map { it.times(10) },
            yValues = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).map { it.times(10) },
            interval = 10,
            points = listOf(
                0f,
                5.4f,
                2f,
                6f,
                9f,
                4f,
                2f,
                4f,
                8f,
                1f,
                11f,
                3f,
                5.4f,
                2f,
                6f,
                9f,
                4f,
                2f,
                4f
            ).map { it.times(10f) },

            points2 = listOf(
                0f,
                6.4f,
                1f,
                5f,
                10f,
                5f,
                1f,
                2f,
                10f,
                7f,
                8f,
                5f,
                5f,
                2.3f,
                6.9f,
                7f,
                5f,
                6f,
                8f
            ).map { it.times(10f) },
        )

    }
}


@Composable
fun Title(title:String, modifier: Modifier = Modifier){
    Text( text = title,
        fontSize = 36.sp,
        color = Color.Red )
}

@Composable
fun RemainingBlock(modifier: Modifier = Modifier){
    var dailyAmount = 10
    var spentAmount = 3.99
    var currentAmount = dailyAmount - spentAmount
    Row() {
        Text( text = "Remaining for today",
            fontSize = 26.sp,
            color = Color.Black)
        Text(text = "$currentAmount E")
    }
}


@Composable
fun ProductsBlock(products: List<Product>){
//    val products  = remember {
//        mutableStateListOf(
//                Product(1, "Cheese", 3.34),
//                Product(2, "Milk", 5.04),
//                Product(3, "Toothpaste", 2.12)          )
//
//    }
    val productsMutable = products.toMutableList()

    LazyColumn {
        items(productsMutable, key = { it.id }) { product ->
            ProductRow(product,
                onUpdate = { updatedProduct ->
                    val index = products.indexOfFirst { it.id == updatedProduct.id }
                    if (index != -1) {
                        productsMutable[index] = updatedProduct
                    }
                }
            )
        }
    }
}

@Composable
fun ProductRow(product: Product, onUpdate: (Product) -> Unit) {

    var title by remember { mutableStateOf(product.title) }
    var amount by remember { mutableStateOf(product.amount.toString()) }

    Row {
        TextField(
            value = title,
            onValueChange = {
                title = it
                onUpdate(product.copy(title = it))
            }
        )

        TextField(
            value = amount,
            onValueChange = {
                amount = it
                onUpdate(product.copy(amount = it.toDoubleOrNull() ?: 0.0))
            }
        )
    }
}



@Composable
fun Profile(modifier: Modifier = Modifier){
    Title("Personal Info")
    PersonalInformation()
}

@Composable
fun PersonalInformation(){
    Column(){
        Avatar()
        PersonalDetails("John Ivanov", 23, "dummy.email@gmail.com", "0987654321")
        Row() {
            CardBlock("Daily Amount", 23)
            CardBlock("Current Savings", 1000)

        }
        EditableTextBlock()
    }
}

@Composable
fun EditableTextBlock() {

    var text by remember { mutableStateOf("This is my profile description.") }
    var isEditing by remember { mutableStateOf(false) }

    Column {

        if (isEditing) {

            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth()
            )

            Row {
                Button(
                    onClick = { isEditing = false }
                ) {
                    Text("Save")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { isEditing = false }
                ) {
                    Text("Cancel")
                }
            }

        } else {

            Text(
                text = text,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                onClick = { isEditing = true }
            ) {
                Text("Edit")
            }
        }
    }
}

@Composable
fun CardBlock(textVal:String, amount: Int){
    Column {
        Text(text = "$textVal: $amount")
    }
}

@Composable
fun PersonalDetails(name: String, age: Int, email: String, mobile: String){
    Column() {
        Text(text = "Name: $name")
        Text(text = "Age: $age")
        Text(text = "Email: $email")
        Text(text = "Mobile: $mobile")

    }
}


@Composable
fun Avatar(){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painter = painterResource(R.drawable.user),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "John Doe",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun UserList(users: List<User>) {
    // LazyColumn е еквивалентът на RecyclerView в Compose
    LazyColumn {
        items(users) { user ->
            Text(text = "Потребител: ${user.name}", modifier = Modifier.padding(16.dp))
        }
    }
}