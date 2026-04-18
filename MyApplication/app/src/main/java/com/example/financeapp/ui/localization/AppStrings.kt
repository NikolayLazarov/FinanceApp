package com.example.financeapp.ui.localization

import androidx.compose.runtime.compositionLocalOf
import com.example.financeapp.data.model.TimeGroup

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    BULGARIAN("bg", "Български");

    companion object {
        fun fromCode(code: String): AppLanguage = entries.find { it.code == code } ?: ENGLISH
    }
}

data class AppStrings(
    // Navigation
    val navStatistics: String,
    val navHome: String,
    val navProfile: String,

    // Auth
    val appTagline: String,
    val welcomeBack: String,
    val createAccount: String,
    val signInToContinue: String,
    val fillInYourDetails: String,
    val firstName: String,
    val lastName: String,
    val age: String,
    val gender: String,
    val genderMale: String,
    val genderFemale: String,
    val genderOther: String,
    val email: String,
    val password: String,
    val signIn: String,
    val noAccountSignUp: String,
    val hasAccountSignIn: String,
    val bypassAuth: String,

    // Common
    val cancel: String,
    val save: String,
    val delete: String,
    val back: String,

    // Main
    val welcomeBackGreeting: String,
    val yourFinances: String,
    val dailyBudget: String,
    val savings: String,
    val totalSpent: String,
    val all: String,
    val noExpensesYet: String,
    val deleteExpense: String,
    val deleteConfirmFormat: String,

    // Categories
    val categoryFood: String,
    val categoryTransportation: String,
    val categoryUtilities: String,
    val categoryHealthcare: String,
    val categoryEducation: String,
    val categoryEntertainment: String,
    val categoryShopping: String,
    val categoryTravel: String,
    val categoryFinance: String,
    val categoryOther: String,

    // Time groups
    val timeDay: String,
    val timeWeek: String,
    val timeMonth: String,
    val timeYear: String,
    val weekLabelFormat: String,

    // Profile
    val settings: String,
    val darkMode: String,
    val language: String,
    val personalInformation: String,
    val name: String,
    val notSet: String,
    val changeBudget: String,
    val addSavingsButton: String,
    val changeDailyBudget: String,
    val addToSavings: String,
    val amount: String,
    val signOut: String,
    val user: String,

    // Statistics
    val analytics: String,
    val analyticsSubtitle: String,
    val filters: String,
    val allCategories: String,
    val spendingDistribution: String,
    val breakdownByCategory: String,
    val spendingTrends: String,
    val spendingOverTime: String,
    val forThisDay: String,
    val forThisWeek: String,
    val forThisMonth: String,
    val forThisYear: String,
    val current7DaysVsPrev: String,
    val current4WeeksVsPrev: String,
    val current6MonthsVsPrev: String,
    val current5YearsVsPrev: String,

    // Budget prompt
    val setUpYourBudget: String,
    val budgetPromptMessage: String,
    val goToProfile: String,
    val later: String,

    // Add Expense
    val addExpense: String,
    val chooseHowToAdd: String,
    val scanReceipt: String,
    val addManually: String,
    val newExpense: String,
    val editExpense: String,
    val trackNewSpending: String,
    val updateSpendingDetails: String,
    val scanReceiptImage: String,
    val scanning: String,
    val title: String,
    val date: String,
    val category: String,
    val saveExpense: String,
    val updateExpenseButton: String,
    val scannedItems: String,
    val reviewAndEditItems: String,
    val noItemsDetected: String,
    val itemsCount: String,
    val total: String,
    val addAllExpenses: String,
    val price: String,

    // Scanner
    val processingReceipt: String,
    val ocrInProgress: String,
    val failedToProcess: String,
    val goBack: String,
    val reviewScannedItems: String,
    val editItemsBeforeAdding: String,
    val noItemsOnReceipt: String,
    val addExpensesButton: String,
    val itemName: String,
    val store: String,
) {
    fun categoryDisplayName(apiCategory: String): String = when (apiCategory.lowercase()) {
        "food" -> categoryFood
        "transportation" -> categoryTransportation
        "utilities" -> categoryUtilities
        "healthcare" -> categoryHealthcare
        "education" -> categoryEducation
        "entertainment" -> categoryEntertainment
        "shopping" -> categoryShopping
        "travel" -> categoryTravel
        "finance" -> categoryFinance
        else -> categoryOther
    }

    fun timeGroupDisplayName(group: TimeGroup): String = when (group) {
        TimeGroup.DAY -> timeDay
        TimeGroup.WEEK -> timeWeek
        TimeGroup.MONTH -> timeMonth
        TimeGroup.YEAR -> timeYear
    }

    val categoryList: List<Pair<String, String>>
        get() = listOf(
            "Food" to categoryFood,
            "Transportation" to categoryTransportation,
            "Utilities" to categoryUtilities,
            "Healthcare" to categoryHealthcare,
            "Education" to categoryEducation,
            "Entertainment" to categoryEntertainment,
            "Shopping" to categoryShopping,
            "Travel" to categoryTravel,
            "Finance" to categoryFinance,
            "Other" to categoryOther,
        )

    val genderOptions: List<String>
        get() = listOf(genderMale, genderFemale, genderOther)
}

fun appStrings(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.ENGLISH -> enStrings
    AppLanguage.BULGARIAN -> bgStrings
}

val LocalStrings = compositionLocalOf { enStrings }

private val enStrings = AppStrings(
    navStatistics = "Statistics",
    navHome = "Home",
    navProfile = "Profile",

    appTagline = "Track your expenses smartly",
    welcomeBack = "Welcome Back",
    createAccount = "Create Account",
    signInToContinue = "Sign in to continue",
    fillInYourDetails = "Fill in your details",
    firstName = "First Name",
    lastName = "Last Name",
    age = "Age",
    gender = "Gender",
    genderMale = "Male",
    genderFemale = "Female",
    genderOther = "Other",
    email = "Email",
    password = "Password",
    signIn = "Sign In",
    noAccountSignUp = "Don't have an account? Sign Up",
    hasAccountSignIn = "Already have an account? Sign In",
    bypassAuth = "Bypass Authentication (Developer Mode)",

    cancel = "Cancel",
    save = "Save",
    delete = "Delete",
    back = "Back",

    welcomeBackGreeting = "Welcome back",
    yourFinances = "Your Finances",
    dailyBudget = "Daily Budget",
    savings = "Savings",
    totalSpent = "Total Spent",
    all = "All",
    noExpensesYet = "No expenses yet",
    deleteExpense = "Delete Expense",
    deleteConfirmFormat = "Are you sure you want to delete \"%s\"? This action cannot be undone.",

    categoryFood = "Food",
    categoryTransportation = "Transportation",
    categoryUtilities = "Utilities",
    categoryHealthcare = "Healthcare",
    categoryEducation = "Education",
    categoryEntertainment = "Entertainment",
    categoryShopping = "Shopping",
    categoryTravel = "Travel",
    categoryFinance = "Finance",
    categoryOther = "Other",

    timeDay = "Day",
    timeWeek = "Week",
    timeMonth = "Month",
    timeYear = "Year",
    weekLabelFormat = "Week %d, %d",

    settings = "Settings",
    darkMode = "Dark Mode",
    language = "Language",
    personalInformation = "Personal Information",
    name = "Name",
    notSet = "Not set",
    changeBudget = "Change Budget",
    addSavingsButton = "Add Savings",
    changeDailyBudget = "Change Daily Budget",
    addToSavings = "Add to Savings",
    amount = "Amount",
    signOut = "Sign Out",
    user = "User",

    analytics = "Analytics",
    analyticsSubtitle = "Insights into your spending habits",
    filters = "Filters",
    allCategories = "All Categories",
    spendingDistribution = "Spending Distribution",
    breakdownByCategory = "Breakdown by Category",
    spendingTrends = "Spending Trends",
    spendingOverTime = "Spending over Time",
    forThisDay = "for this Day",
    forThisWeek = "for this Week",
    forThisMonth = "for this Month",
    forThisYear = "for this Year",
    current7DaysVsPrev = "Current 7 Days vs Previous 7 Days",
    current4WeeksVsPrev = "Current 4 Weeks vs Previous 4 Weeks",
    current6MonthsVsPrev = "Current 6 Months vs Previous 6 Months",
    current5YearsVsPrev = "Current 5 Years vs Previous 5 Years",

    setUpYourBudget = "Set Up Your Budget",
    budgetPromptMessage = "Your daily budget and savings are both set to \$0.00. Head to your Profile to set your daily budget and savings goals.",
    goToProfile = "Go to Profile",
    later = "Later",

    addExpense = "Add Expense",
    chooseHowToAdd = "Choose how to add your expense",
    scanReceipt = "Scan Receipt",
    addManually = "Add Manually",
    newExpense = "New Expense",
    editExpense = "Edit Expense",
    trackNewSpending = "Track a new spending",
    updateSpendingDetails = "Update your spending details",
    scanReceiptImage = "Scan Receipt Image",
    scanning = "Scanning...",
    title = "Title",
    date = "Date",
    category = "Category",
    saveExpense = "Save Expense",
    updateExpenseButton = "Update Expense",
    scannedItems = "Scanned Items",
    reviewAndEditItems = "Review and edit items before adding",
    noItemsDetected = "No items detected",
    itemsCount = "items",
    total = "Total",
    addAllExpenses = "Add All Expenses",
    price = "Price",

    processingReceipt = "Processing receipt...",
    ocrInProgress = "OCR & LLM correction in progress",
    failedToProcess = "Failed to process receipt",
    goBack = "Go Back",
    reviewScannedItems = "Review Scanned Items",
    editItemsBeforeAdding = "Edit items before adding as expenses:",
    noItemsOnReceipt = "No items detected on receipt",
    addExpensesButton = "Add Expenses",
    itemName = "Item name",
    store = "Store",
)

private val bgStrings = AppStrings(
    navStatistics = "Статистика",
    navHome = "Начало",
    navProfile = "Профил",

    appTagline = "Следете разходите си интелигентно",
    welcomeBack = "Добре дошли",
    createAccount = "Създай акаунт",
    signInToContinue = "Влезте, за да продължите",
    fillInYourDetails = "Попълнете данните си",
    firstName = "Име",
    lastName = "Фамилия",
    age = "Възраст",
    gender = "Пол",
    genderMale = "Мъж",
    genderFemale = "Жена",
    genderOther = "Друго",
    email = "Имейл",
    password = "Парола",
    signIn = "Вход",
    noAccountSignUp = "Нямате акаунт? Регистрация",
    hasAccountSignIn = "Вече имате акаунт? Вход",
    bypassAuth = "Прескочи автентикацията (Режим за разработчици)",

    cancel = "Отказ",
    save = "Запази",
    delete = "Изтрий",
    back = "Назад",

    welcomeBackGreeting = "Добре дошли",
    yourFinances = "Вашите финанси",
    dailyBudget = "Дневен бюджет",
    savings = "Спестявания",
    totalSpent = "Общо похарчени",
    all = "Всички",
    noExpensesYet = "Все още няма разходи",
    deleteExpense = "Изтрий разход",
    deleteConfirmFormat = "Сигурни ли сте, че искате да изтриете \"%s\"? Това действие е необратимо.",

    categoryFood = "Храна",
    categoryTransportation = "Транспорт",
    categoryUtilities = "Комунални",
    categoryHealthcare = "Здраве",
    categoryEducation = "Образование",
    categoryEntertainment = "Забавление",
    categoryShopping = "Пазаруване",
    categoryTravel = "Пътуване",
    categoryFinance = "Финанси",
    categoryOther = "Друго",

    timeDay = "Ден",
    timeWeek = "Седмица",
    timeMonth = "Месец",
    timeYear = "Година",
    weekLabelFormat = "Седмица %d, %d",

    settings = "Настройки",
    darkMode = "Тъмен режим",
    language = "Език",
    personalInformation = "Лична информация",
    name = "Име",
    notSet = "Не е зададено",
    changeBudget = "Промени бюджет",
    addSavingsButton = "Добави спестяване",
    changeDailyBudget = "Промени дневен бюджет",
    addToSavings = "Добави към спестявания",
    amount = "Сума",
    signOut = "Изход",
    user = "Потребител",

    analytics = "Анализи",
    analyticsSubtitle = "Информация за навиците ви за харчене",
    filters = "Филтри",
    allCategories = "Всички категории",
    spendingDistribution = "Разпределение на разходите",
    breakdownByCategory = "По категории",
    spendingTrends = "Тенденции на разходите",
    spendingOverTime = "Разходи през времето",
    forThisDay = "за днес",
    forThisWeek = "за тази седмица",
    forThisMonth = "за този месец",
    forThisYear = "за тази година",
    current7DaysVsPrev = "Последните 7 дни спрямо предишните 7 дни",
    current4WeeksVsPrev = "Последните 4 седмици спрямо предишните 4 седмици",
    current6MonthsVsPrev = "Последните 6 месеца спрямо предишните 6 месеца",
    current5YearsVsPrev = "Последните 5 години спрямо предишните 5 години",

    setUpYourBudget = "Настройте бюджета си",
    budgetPromptMessage = "Вашият дневен бюджет и спестявания са \$0.00. Отидете в Профил, за да зададете дневен бюджет и цели за спестяване.",
    goToProfile = "Към профила",
    later = "По-късно",

    addExpense = "Добави разход",
    chooseHowToAdd = "Изберете как да добавите разход",
    scanReceipt = "Сканирай бележка",
    addManually = "Добави ръчно",
    newExpense = "Нов разход",
    editExpense = "Редактирай разход",
    trackNewSpending = "Проследяване на нов разход",
    updateSpendingDetails = "Актуализирайте данните за разхода",
    scanReceiptImage = "Сканирай изображение на бележка",
    scanning = "Сканиране...",
    title = "Заглавие",
    date = "Дата",
    category = "Категория",
    saveExpense = "Запази разход",
    updateExpenseButton = "Актуализирай разход",
    scannedItems = "Сканирани артикули",
    reviewAndEditItems = "Прегледайте и редактирайте артикулите преди добавяне",
    noItemsDetected = "Няма открити артикули",
    itemsCount = "артикула",
    total = "Общо",
    addAllExpenses = "Добави всички разходи",
    price = "Цена",

    processingReceipt = "Обработка на бележката...",
    ocrInProgress = "OCR & LLM корекция в процес",
    failedToProcess = "Неуспешна обработка на бележката",
    goBack = "Назад",
    reviewScannedItems = "Преглед на сканирани артикули",
    editItemsBeforeAdding = "Редактирайте артикулите преди да ги добавите като разходи:",
    noItemsOnReceipt = "Няма открити артикули на бележката",
    addExpensesButton = "Добави разходи",
    itemName = "Име на артикул",
    store = "Магазин",
)
