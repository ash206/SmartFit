package com.example.smartfit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.* // 1. 引入动画核心库
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.smartfit.ui.theme.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFitApp()
        }
    }
}

@Composable
fun SmartFitApp() {
    val viewModel: SmartFitViewModel = viewModel()
    val isDark by viewModel.isDarkMode.collectAsState()

    MaterialTheme(
        colorScheme = if (isDark) darkColorScheme() else lightColorScheme()
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Hide BottomBar on Login, Register, Add Activity and Detail screens
        val showBottomBar = currentRoute !in listOf(
            "login",
            "register",
            "add_activity",
            "add_activity?activityId={activityId}"
        ) && currentRoute?.startsWith("activity_detail") != true

        Scaffold(
            // Scaffold background adapts automatically based on theme
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(navController)
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("login") { LoginScreen(navController) }
                composable("register") { RegisterScreen(navController, viewModel) }
                composable("home") { HomeScreen(navController, viewModel) }
                composable("activities") { ActivitiesScreen(navController, viewModel) }
                composable("summary") { SummaryScreen(viewModel) }
                composable("profile") { ProfileScreen(navController, viewModel) }

                composable(
                    route = "add_activity?activityId={activityId}",
                    arguments = listOf(
                        navArgument("activityId") {
                            type = NavType.IntType
                            defaultValue = -1
                        }
                    )
                ) { backStackEntry ->
                    val activityId = backStackEntry.arguments?.getInt("activityId") ?: -1
                    AddActivityScreen(navController, viewModel, activityId)
                }

                composable(
                    "activity_detail/{activityId}",
                    arguments = listOf(navArgument("activityId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val activityId = backStackEntry.arguments?.getInt("activityId")
                    if (activityId != null) {
                        ActivityDetailScreen(navController, viewModel, activityId)
                    }
                }
            }
        }
    }
}

// --- AUTH SCREENS ---

@Composable
fun LoginScreen(navController: NavController) {
    val viewModel: SmartFitViewModel = viewModel()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val storedEmail by viewModel.userEmail.collectAsState()
    val storedPassword by viewModel.userPassword.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Adapted background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(Color(0xFF111827), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) { Text("SF", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(32.dp))

        Text(
            "Welcome Back",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground // Adapted Text
        )
        Text("Sign in to continue", color = Color.Gray)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = errorMessage != null && (email.isBlank() || errorMessage!!.contains("Email") || errorMessage!!.contains("account")),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            isError = errorMessage != null && (password.isBlank() || errorMessage!!.contains("Password")),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isBlank() && password.isBlank()) errorMessage = "Please enter email and password!"
                else if (email.isBlank()) errorMessage = "Email is required!"
                else if (password.isBlank()) errorMessage = "Password is required!"
                else if (!email.contains("@")) errorMessage = "Invalid Email Format"
                else if (storedEmail.isEmpty() || email != storedEmail) errorMessage = "Email not found. Please register first."
                else if (password != storedPassword) errorMessage = "Incorrect Password!"
                else {
                    errorMessage = null
                    Toast.makeText(context, "Welcome back to SmartFit!", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary) // Use BluePrimary for visibility
        ) { Text("Log In", fontSize = 16.sp, color = Color.White) }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account? ", color = Color.Gray)
            Text(
                "Sign Up",
                color = BluePrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.navigate("register") }
            )
        }
    }
}

@Composable
fun RegisterScreen(navController: NavController, viewModel: SmartFitViewModel) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val storedEmail by viewModel.userEmail.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("Start your fitness journey today", color = Color.Gray)
        Spacer(Modifier.height(32.dp))

        // Input Fields with Theme Colors
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = name, onValueChange = { name = it }, label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
            colors = textFieldColors
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Email") },
            isError = errorMessage?.contains("Email") == true,
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
            colors = textFieldColors
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Password") },
            isError = errorMessage?.contains("Password") == true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
            colors = textFieldColors
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                var emptyCount = 0
                if (name.isBlank()) emptyCount++
                if (email.isBlank()) emptyCount++
                if (password.isBlank()) emptyCount++

                if (emptyCount == 3) errorMessage = "All fields are required!"
                else if (emptyCount == 2) errorMessage = "Please fill in the two missing fields!"
                else if (emptyCount == 1) {
                    if (name.isBlank()) errorMessage = "Full name is required!"
                    else if (email.isBlank()) errorMessage = "Email is required!"
                    else errorMessage = "Password is required!"
                } else if (!email.contains("@")) errorMessage = "Invalid Email Format"
                else if (email == storedEmail) errorMessage = "This email is already registered! Please Log In."
                else if (password.length < 8) errorMessage = "Password must be at least 8 characters"
                else {
                    errorMessage = null
                    viewModel.clearAllData()
                    viewModel.saveUserProfile(name, "", "", "")
                    viewModel.saveUserCredentials(email, password)
                    Toast.makeText(context, "Welcome to SmartFit App! Let's get started.", Toast.LENGTH_LONG).show()
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) { Text("Register", fontSize = 16.sp, color = Color.White) }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account? ", color = Color.Gray)
            Text("Log In", color = BluePrimary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { navController.popBackStack() })
        }
    }
}

// --- MAIN APP SCREENS ---

@Composable
fun HomeScreen(navController: NavController, viewModel: SmartFitViewModel) {
    val activities by viewModel.activities.collectAsState()
    val tip by viewModel.fitnessTip.collectAsState()
    val storedName by viewModel.userName.collectAsState()
    val stepGoalString by viewModel.stepGoal.collectAsState()
    val calGoalString by viewModel.calorieGoal.collectAsState()

    val todaySteps = activities.filter { it.type == "steps" }.sumOf { it.value }
    val todayCals = activities.sumOf { it.calories }
    val todayWorkouts = activities.count { it.type == "workout" }

    val malaysiaTimeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    dateFormat.timeZone = malaysiaTimeZone
    val currentDate = dateFormat.format(Date())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                Text(
                    "Welcome back,\n$storedName!",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 32.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text("Here's your activity summary for today", color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text("📅", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(currentDate, color = Color.Gray, fontSize = 14.sp)
                }
            }
        }

        item {
            ModernStatCard(
                title = "Steps Today",
                current = todaySteps,
                total = stepGoalString.toIntOrNull() ?: 10000,
                unit = "",
                icon = Icons.Default.DirectionsWalk,
                accentColor = BluePrimary,
                bgColor = BlueBackground
            )
        }

        item {
            ModernStatCard(
                title = "Calories",
                current = todayCals,
                total = calGoalString.toIntOrNull() ?: 2000,
                unit = "kcal",
                icon = Icons.Default.LocalFireDepartment,
                accentColor = GreenPrimary,
                bgColor = GreenBackground
            )
        }

        item {
            ModernStatCard(
                title = "Workouts",
                current = todayWorkouts,
                total = 1,
                unit = "completed",
                icon = Icons.Default.FitnessCenter,
                accentColor = OrangePrimary,
                bgColor = OrangeBackground
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Adapted
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Quick Actions", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { navController.navigate("add_activity") },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Log Steps", color = Color.White)
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { navController.navigate("add_activity") },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Workout", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { navController.navigate("summary") },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(8.dp))
                        Text("View Summary", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)) // Adapted
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.background, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Fitness Tip", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { viewModel.fetchTip() }) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Next Tip", fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("\"$tip\"", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.background) {
                            Text("General Fitness", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivitiesScreen(navController: NavController, viewModel: SmartFitViewModel) {
    val activities by viewModel.activities.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Activity Log", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("${activities.size} activities logged", color = Color.Gray, fontSize = 14.sp)
            }
            Button(
                onClick = { navController.navigate("add_activity") },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add", color = Color.White)
            }
        }
        Spacer(Modifier.height(20.dp))

        if (activities.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("No activities yet", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text("Start tracking your fitness journey", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(activities) { log ->
                    ModernActivityItem(log) {
                        navController.navigate("activity_detail/${log.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun AddActivityScreen(navController: NavController, viewModel: SmartFitViewModel, activityId: Int = -1) {
    val context = LocalContext.current
    val isEditMode = activityId != -1

    var selectedType by remember { mutableStateOf("steps") }
    var value by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember {
        mutableStateOf(Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur")).timeInMillis)
    }
    var isLoading by remember { mutableStateOf(isEditMode) }

    val workoutTypes = listOf("Running", "Cycling", "Swimming", "Yoga", "Weightlifting", "HIIT", "Others")
    var selectedWorkoutType by remember { mutableStateOf(workoutTypes.first()) }

    val foodList = viewModel.foodDatabase.keys.toList().sorted()
    var selectedFoodType by remember { mutableStateOf(foodList.first()) }
    var foodQuantity by remember { mutableStateOf("1") }

    // Date Picker Logic (Keep existing logic)
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))
    calendar.timeInMillis = selectedDate

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))
            newDate.set(year, month, dayOfMonth)
            val currentTime = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))
            currentTime.timeInMillis = selectedDate
            newDate.set(Calendar.HOUR_OF_DAY, currentTime.get(Calendar.HOUR_OF_DAY))
            newDate.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE))
            selectedDate = newDate.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val newTime = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))
            newTime.timeInMillis = selectedDate
            newTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            newTime.set(Calendar.MINUTE, minute)
            selectedDate = newTime.timeInMillis
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    if (isEditMode) {
        LaunchedEffect(activityId) {
            try {
                val log = viewModel.getActivity(activityId).first()
                if (log != null) {
                    selectedType = log.type
                    selectedDate = log.date

                    if (log.type == "workout" && log.notes.startsWith("Type: ")) {
                        val parts = log.notes.split(". ", limit = 2)
                        val typePart = parts[0].removePrefix("Type: ")
                        if (typePart in workoutTypes) selectedWorkoutType = typePart
                        notes = if (parts.size > 1) parts[1] else ""
                        value = log.value.toString()
                    }
                    else if (log.type == "food" && log.notes.startsWith("Food: ")) {
                        val parts = log.notes.split(". ", limit = 2)
                        val foodInfo = parts[0].removePrefix("Food: ")
                        if (foodInfo.contains(" x ")) {
                            val fParts = foodInfo.split(" x ")
                            val fName = fParts[0]
                            val fQty = fParts.getOrNull(1) ?: "1"
                            if (fName in foodList) {
                                selectedFoodType = fName
                                foodQuantity = fQty
                            }
                        }
                        notes = if (parts.size > 1) parts[1] else ""
                        value = log.value.toString()
                    }
                    else {
                        value = log.value.toString()
                        notes = log.notes
                    }
                    isLoading = false
                } else {
                    Toast.makeText(context, "Activity not found", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Adapted
            .padding(16.dp)
    ) {
        Text(
            text = if (isEditMode) "Edit Activity" else "Add New Activity",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground // Adapted
        )
        Spacer(Modifier.height(24.dp))

        Text("Activity Type", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)) // Adapted
                .padding(4.dp)
        ) {
            listOf("steps", "workout", "food").forEach { type ->
                val isSelected = selectedType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent, RoundedCornerShape(6.dp))
                        .clickable {
                            selectedType = type
                            value = ""
                            foodQuantity = "1"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when(type) {
                            "steps" -> Icons.Default.DirectionsWalk
                            "workout" -> Icons.Default.FitnessCenter
                            else -> Icons.Default.Restaurant
                        }
                        // Icon tint logic: If selected, use primary color or black (on light). If not, use onSurfaceVariant
                        val tint = if(isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        Icon(icon, null, modifier = Modifier.size(14.dp), tint = tint)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            type.replaceFirstChar { it.uppercase() },
                            fontSize = 14.sp,
                            fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = tint
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- Colors for Inputs ---
        val inputColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BluePrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline
        )

        // --- A. Workout ---
        if (selectedType == "workout") {
            Text("Workout Type", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedWorkoutType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurface) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = inputColors // Use adapted colors
                )
                Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    workoutTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { selectedWorkoutType = type; expanded = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // --- B. Food ---
        if (selectedType == "food") {
            Text("Select Food", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedFoodType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurface) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = inputColors
                )
                Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    foodList.forEach { food ->
                        DropdownMenuItem(
                            text = { Text(food, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { selectedFoodType = food; expanded = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("Quantity / Servings", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = foodQuantity,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) foodQuantity = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = inputColors
            )

            val unitCals = viewModel.foodDatabase[selectedFoodType] ?: 0
            val qty = foodQuantity.toFloatOrNull() ?: 0f
            val totalCals = (unitCals * qty).toInt()

            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = GreenBackground), // GreenBackground is light, make sure text is dark
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Total: $totalCals kcal", fontWeight = FontWeight.Bold, color = GreenPrimary) // Using brand color
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // --- C. Steps / Duration ---
        else {
            val labelText = if (selectedType == "steps") "Number of Steps" else "Duration (mins)"
            Text(labelText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = { if (it.all { c -> c.isDigit() }) value = it },
                placeholder = { Text("e.g., ${if(selectedType == "steps") "10000" else "30"}", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = inputColors
            )
            Spacer(Modifier.height(24.dp))
        }

        // 3. Date & Time
        Text("Date & Time", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeFormat.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")

            OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.width(8.dp))
                Text(dateFormat.format(Date(selectedDate)), color = MaterialTheme.colorScheme.onSurface)
            }
            OutlinedButton(
                onClick = { timePickerDialog.show() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.width(8.dp))
                Text(timeFormat.format(Date(selectedDate)), color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Notes (optional)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            placeholder = { Text("Add any additional notes...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp),
            colors = inputColors
        )

        Spacer(Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
            Button(
                onClick = {
                    var finalValue = 0
                    var finalNotes = notes

                    if (selectedType == "food") {
                        val unitCals = viewModel.foodDatabase[selectedFoodType] ?: 0
                        val qty = foodQuantity.toFloatOrNull() ?: 0f
                        finalValue = (unitCals * qty).toInt()

                        val foodInfo = "Food: $selectedFoodType x $foodQuantity"
                        finalNotes = if (notes.isNotEmpty()) "$foodInfo. $notes" else foodInfo
                    } else {
                        finalValue = value.toIntOrNull() ?: 0
                        if (selectedType == "workout") {
                            val workoutInfo = "Type: $selectedWorkoutType"
                            finalNotes = if (notes.isNotEmpty()) "$workoutInfo. $notes" else workoutInfo
                        }
                    }

                    if (finalValue > 0) {
                        if (isEditMode) {
                            viewModel.updateActivity(activityId, selectedType, finalValue, finalNotes, selectedDate)
                        } else {
                            viewModel.addActivity(selectedType, finalValue, finalNotes, selectedDate)
                        }
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(if (isEditMode) "Update" else "Add Activity", color = Color.White)
            }
        }
    }
}

// --- UPDATED: Summary Screen ---
@Composable
fun SummaryScreen(viewModel: SmartFitViewModel) {
    val dailyStats by viewModel.dailySummary.collectAsState()
    val weeklyStats by viewModel.weeklySummary.collectAsState()
    val activities by viewModel.activities.collectAsState()

    val (daySteps, dayCals, dayWorkouts) = dailyStats
    val (weekSteps, weekCals, weekWorkouts) = weeklyStats

    val totalSteps = activities.filter { it.type == "steps" }.sumOf { it.value }
    val totalCalories = activities.sumOf { it.calories }
    val totalWorkouts = activities.count { it.type == "workout" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column {
                Text("Activity Summary", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Track your daily and weekly progress", color = Color.Gray)
            }
        }

        item {
            SummarySection(title = "Today", steps = daySteps, calories = dayCals, workouts = dayWorkouts, color = BluePrimary)
        }

        item {
            SummarySection(title = "This Week (Last 7 Days)", steps = weekSteps, calories = weekCals, workouts = weekWorkouts, color = OrangePrimary)
        }

        item {
            SummarySection(title = "All Time", steps = totalSteps, calories = totalCalories, workouts = totalWorkouts, color = GreenPrimary)
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
fun SummarySection(title: String, steps: Int, calories: Int, workouts: Int, color: Color) {
    Column {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryStatCard(Modifier.weight(1f), Icons.Default.DirectionsWalk, "$steps", "Steps", color)
            SummaryStatCard(Modifier.weight(1f), Icons.Default.LocalFireDepartment, "$calories", "Kcal", color)
            SummaryStatCard(Modifier.weight(1f), Icons.Default.FitnessCenter, "$workouts", "Workouts", color)
        }
    }
}

@Composable
fun SummaryStatCard(modifier: Modifier = Modifier, icon: ImageVector, value: String, label: String, tint: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController, viewModel: SmartFitViewModel) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current

    val storedName by viewModel.userName.collectAsState()
    val storedWeight by viewModel.userWeight.collectAsState()
    val storedHeight by viewModel.userHeight.collectAsState()
    val storedAge by viewModel.userAge.collectAsState()
    val storedStepGoal by viewModel.stepGoal.collectAsState()
    val storedCalGoal by viewModel.calorieGoal.collectAsState()

    var name by remember { mutableStateOf(storedName) }
    var weight by remember { mutableStateOf(storedWeight) }
    var height by remember { mutableStateOf(storedHeight) }
    var age by remember { mutableStateOf(storedAge) }

    var stepGoal by remember { mutableStateOf(storedStepGoal) }
    var calorieGoal by remember { mutableStateOf(storedCalGoal) }

    LaunchedEffect(storedName) { name = storedName }
    LaunchedEffect(storedWeight) { weight = storedWeight }
    LaunchedEffect(storedHeight) { height = storedHeight }
    LaunchedEffect(storedAge) { age = storedAge }
    LaunchedEffect(storedStepGoal) { stepGoal = storedStepGoal }
    LaunchedEffect(storedCalGoal) { calorieGoal = storedCalGoal }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Profile & Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Manage your preferences and goals", color = Color.Gray, fontSize = 14.sp)
            }
        }

        // Personal Information Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(8.dp))
                        Text("Personal Information", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(Modifier.height(16.dp))

                    ProfileTextField(label = "Name", value = name, onValueChange = { name = it })
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) { ProfileTextField(label = "Weight (kg)", value = weight, onValueChange = { weight = it }, isNumber = true) }
                        Box(Modifier.weight(1f)) { ProfileTextField(label = "Height (cm)", value = height, onValueChange = { height = it }, isNumber = true) }
                    }
                    Spacer(Modifier.height(12.dp))
                    ProfileTextField(label = "Age", value = age, onValueChange = { age = it }, isNumber = true)

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.saveUserProfile(name, weight, height, age)
                            Toast.makeText(context, "Changes Saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Changes", color = Color.White)
                    }
                }
            }
        }

        // Daily Goals Card (Re-added and adapted for Dark Mode)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.TrackChanges, null, tint = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(8.dp))
                        Text("Daily Goals", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(16.dp))

                    ProfileTextField(label = "Daily Step Goal", value = stepGoal, onValueChange = { stepGoal = it }, isNumber = true)
                    Spacer(Modifier.height(12.dp))
                    ProfileTextField(label = "Daily Calorie Goal", value = calorieGoal, onValueChange = { calorieGoal = it }, isNumber = true)

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.saveGoals(stepGoal, calorieGoal)
                            Toast.makeText(context, "Goals Saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Goals", color = Color.White)
                    }
                }
            }
        }

        // Appearance Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isDark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode, null, tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Dark Mode", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Toggle theme", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.toggleDarkMode(it) }
                        )
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log Out")
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
fun ActivityDetailScreen(navController: NavController, viewModel: SmartFitViewModel, activityId: Int) {
    val activityLog by viewModel.getActivity(activityId).collectAsState(initial = null)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Adapted
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(Modifier.width(8.dp))
                Text("Activity Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            if (activityLog != null) {
                Row {
                    IconButton(onClick = { navController.navigate("add_activity?activityId=${activityId}") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BluePrimary)
                    }
                    IconButton(onClick = {
                        viewModel.deleteActivity(activityLog!!)
                        Toast.makeText(context, "Activity Deleted", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (activityLog != null) {
            val log = activityLog!!

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // Adapted
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = when (log.type) {
                            "steps" -> Icons.Default.DirectionsWalk
                            "workout" -> Icons.Default.FitnessCenter
                            else -> Icons.Default.Restaurant
                        },
                        contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "${log.value} ${if (log.type == "steps") "Steps" else if (log.type == "workout") "Mins" else "Kcal"}",
                        fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = log.type.uppercase(), color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
            DetailRow(label = "Date", value = dateFormat.format(Date(log.date)))
            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

            DetailRow(label = "Calories Burned", value = "${log.calories} kcal")
            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

            DetailRow(label = "Notes", value = log.notes.ifEmpty { "No notes provided" })
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun ProfileTextField(label: String, value: String, onValueChange: (String) -> Unit, isNumber: Boolean = false) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun ModernStatCard(title: String, current: Int, total: Int, unit: String, icon: ImageVector, accentColor: Color, bgColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Box(modifier = Modifier.background(bgColor, RoundedCornerShape(8.dp)).padding(6.dp)) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$current", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.width(4.dp))
                Text("/ $total $unit", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
            }
            Spacer(Modifier.height(12.dp))

            // Animation Logic
            var progressState by remember { mutableFloatStateOf(0f) }
            val animatedProgress by animateFloatAsState(
                targetValue = progressState,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                label = "Progress Bar Animation"
            )

            // Trigger animation on composition
            LaunchedEffect(current, total) {
                progressState = (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            }

            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(modifier = Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(accentColor))
            }
            Spacer(Modifier.height(8.dp))
            Text("${(animatedProgress * 100).toInt()}% of daily goal", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ModernActivityItem(log: ActivityLog, onClick: () -> Unit) {
    val (icon, bg, tint) = when(log.type) {
        "steps" -> Triple(Icons.Default.DirectionsWalk, BlueBackground, BluePrimary)
        "workout" -> Triple(Icons.Default.FitnessCenter, OrangeBackground, OrangePrimary)
        else -> Triple(Icons.Default.Restaurant, GreenBackground, GreenPrimary)
    }

    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(bg, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(log.type.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(dateFormat.format(Date(log.date)), fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                val unit = if(log.type == "steps") "steps" else if(log.type == "workout") "mins" else "kcal"
                Text("${log.value} $unit", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (log.calories > 0 && log.type != "food") {
                    Text("${log.calories} kcal", fontSize = 12.sp, color = OrangePrimary)
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        Triple("home", Icons.Outlined.Home, "Home"),
        Triple("activities", Icons.Outlined.List, "Activities"),
        Triple("summary", Icons.Outlined.BarChart, "Summary"),
        Triple("profile", Icons.Outlined.Person, "Profile")
    )

    Surface(shadowElevation = 10.dp, color = MaterialTheme.colorScheme.surface) {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { (route, icon, label) ->
                val selected = currentRoute == route
                NavigationBarItem(
                    icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp)) },
                    label = { Text(label, fontSize = 10.sp, fontWeight = if(selected) FontWeight.Bold else FontWeight.Normal) },
                    selected = selected,
                    onClick = { navController.navigate(route) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    }
}