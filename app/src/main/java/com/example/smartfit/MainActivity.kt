package com.example.smartfit

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.navigation.compose.*
import com.example.smartfit.ui.theme.* // Import your colors
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

        // Hide BottomBar on Login and Register screens
        val showBottomBar = currentRoute !in listOf("login", "register", "add_activity")

        Scaffold(
            containerColor = if (isDark) Color(0xFF111827) else Color(0xFFF3F4F6),
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
                composable("profile") { ProfileScreen(navController, viewModel) } // Pass navController for Logout
                composable("add_activity") { AddActivityScreen(navController, viewModel) }
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

    // 1. Get the stored credentials from ViewModel
    val storedEmail by viewModel.userEmail.collectAsState()
    val storedPassword by viewModel.userPassword.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(Color(0xFF111827), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("SF", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(32.dp))
        Text("Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Text("Sign in to continue", color = Color.Gray)
        Spacer(Modifier.height(32.dp))

        // Input Fields
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = errorMessage != null && (email.isBlank() || errorMessage!!.contains("Email") || errorMessage!!.contains("account")),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
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
            singleLine = true
        )

        // Error Message Display
        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(Modifier.height(32.dp))

        // Login Button
        Button(
            onClick = {

                if (email.isBlank() && password.isBlank()) {
                    errorMessage = "Please enter email and password!"
                }
                else if (email.isBlank()) {
                    errorMessage = "Email is required!"
                }
                else if (password.isBlank()) {
                    errorMessage = "Password is required!"
                }
                else if (!email.contains("@")) {
                    errorMessage = "Invalid Email Format"
                }

                else if (storedEmail.isEmpty() || email != storedEmail) {
                    errorMessage = "Email not found. Please register first."
                }

                else if (password != storedPassword) {
                    errorMessage = "Incorrect Password!"
                }
                // 4. 登录成功
                else {
                    errorMessage = null
                    Toast.makeText(context, "Welcome back to SmartFit!", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
        ) {
            Text("Log In", fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))

        // ... (Register Link Code remains same) ...
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
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Text("Start your fitness journey today", color = Color.Gray)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = errorMessage?.contains("Email") == true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            isError = errorMessage?.contains("Password") == true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
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


                if (emptyCount == 3) {
                    errorMessage = "All fields are required!"
                }
                else if (emptyCount == 2) {
                    errorMessage = "Please fill in the two missing fields!"
                }
                else if (emptyCount == 1) {
                    if (name.isBlank()) errorMessage = "Full name is required!"
                    else if (email.isBlank()) errorMessage = "Email is required!"
                    else errorMessage = "Password is required!"
                }

                else if (!email.contains("@")) {
                    errorMessage = "Invalid Email Format"
                }
                else if (email == storedEmail) {
                    errorMessage = "This email is already registered! Please Log In."
                }
                else if (password.length < 8) {
                    errorMessage = "Password must be at least 8 characters"
                }
                else {
                    // 清除错误信息（可选，但推荐）
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
        ) {
            Text("Register", fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account? ", color = Color.Gray)
            Text(
                "Log In",
                color = BluePrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
        }
    }
}

// --- MAIN APP SCREENS ---

@Composable
fun HomeScreen(navController: NavController, viewModel: SmartFitViewModel) {
    val activities by viewModel.activities.collectAsState()
    val tip by viewModel.fitnessTip.collectAsState()

    // Data from ViewModel
    val storedName by viewModel.userName.collectAsState()
    val stepGoalString by viewModel.stepGoal.collectAsState()
    val calGoalString by viewModel.calorieGoal.collectAsState()

    val todaySteps = activities.filter { it.type == "steps" }.sumOf { it.value }
    val todayCals = activities.sumOf { it.calories }
    val todayWorkouts = activities.count { it.type == "workout" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                Text(
                    "Welcome back,\n$storedName!", // 1. Using stored name
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    )
                )
                Text(
                    "Here's your activity summary for today",
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text("📅", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date()),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Progress Cards
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

        // 1. Added Workout Card below Calories
        item {
            ModernStatCard(
                title = "Workouts",
                current = todayWorkouts,
                total = 1, // Goal could be made dynamic too
                unit = "completed",
                icon = Icons.Default.FitnessCenter,
                accentColor = OrangePrimary,
                bgColor = OrangeBackground
            )
        }

        // Quick Actions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Quick Actions", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { navController.navigate("add_activity") },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
                    ) {
                        Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Log Steps")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { navController.navigate("add_activity") },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Workout", color = Color.Black)
                    }

                    Spacer(Modifier.height(8.dp))

                    // 1. Added View Summary Button
                    OutlinedButton(
                        onClick = { navController.navigate("summary") },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("View Summary", color = Color.Black)
                    }
                }
            }
        }

        // Fitness Tip
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE5E7EB).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp).background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Fitness Tip", fontWeight = FontWeight.SemiBold)
                        }
                        // 1. Functioning Next Tip Button
                        TextButton(onClick = { viewModel.fetchTip() }) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Next Tip", fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("\"$tip\"", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color(0xFF4B5563))
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color.White) {
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
                Text("Activity Log", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("${activities.size} activities logged", color = Color.Gray, fontSize = 14.sp)
            }
            Button(
                onClick = { navController.navigate("add_activity") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add")
            }
        }

        Spacer(Modifier.height(20.dp))

        if (activities.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("No activities yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Start tracking your fitness journey", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(activities) { log ->
                    ModernActivityItem(log)
                }
            }
        }
    }
}

@Composable
fun AddActivityScreen(navController: NavController, viewModel: SmartFitViewModel) {
    var selectedType by remember { mutableStateOf("steps") }
    var value by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("Add New Activity", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        Text("Activity Type", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            listOf("steps", "workout", "food").forEach { type ->
                val isSelected = selectedType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(6.dp))
                        .clickable { selectedType = type },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when(type) {
                            "steps" -> Icons.Default.DirectionsWalk
                            "workout" -> Icons.Default.FitnessCenter
                            else -> Icons.Default.Restaurant
                        }
                        Icon(icon, null, modifier = Modifier.size(14.dp), tint = if(isSelected) Color.Black else Color.Gray)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            type.replaceFirstChar { it.uppercase() },
                            fontSize = 14.sp,
                            fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if(isSelected) Color.Black else Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            if (selectedType == "steps") "Number of Steps" else if (selectedType == "food") "Calories (kcal)" else "Duration (mins)",
            fontWeight = FontWeight.SemiBold, fontSize = 14.sp
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            placeholder = { Text("e.g., 10000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedBorderColor = BluePrimary,
                unfocusedContainerColor = Color(0xFFF9FAFB),
                focusedContainerColor = Color.White
            )
        )

        Spacer(Modifier.height(24.dp))

        Text("Notes (optional)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            placeholder = { Text("Add any additional notes...") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedBorderColor = BluePrimary,
                unfocusedContainerColor = Color(0xFFF9FAFB),
                focusedContainerColor = Color.White
            )
        )

        Spacer(Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Text("Cancel", color = Color.Black)
            }
            Button(
                onClick = {
                    if (value.isNotEmpty()) {
                        viewModel.addActivity(selectedType, value.toIntOrNull() ?: 0, notes)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
            ) {
                Text("Add Activity")
            }
        }
    }
}

@Composable
fun SummaryScreen(viewModel: SmartFitViewModel) {
    // 2. Connected to live data from ViewModel
    val activities by viewModel.activities.collectAsState()

    val totalSteps = activities.filter { it.type == "steps" }.sumOf { it.value }
    val totalCalories = activities.sumOf { it.calories }
    val totalWorkouts = activities.count { it.type == "workout" }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Activity Summary", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Your total fitness progress", color = Color.Gray)
        Spacer(Modifier.height(24.dp))

        // Steps Card
        Card(Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsWalk, null, tint = BluePrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Total Steps", fontWeight = FontWeight.SemiBold)
                }
                Text("$totalSteps", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Calories Card
        Card(Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, null, tint = GreenPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Total Calories", fontWeight = FontWeight.SemiBold)
                }
                Text("$totalCalories kcal", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Workouts Card
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FitnessCenter, null, tint = OrangePrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Workouts Completed", fontWeight = FontWeight.SemiBold)
                }
                Text("$totalWorkouts", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController, viewModel: SmartFitViewModel) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current

    // 3. Connecting inputs to ViewModel state
    val storedName by viewModel.userName.collectAsState()
    val storedWeight by viewModel.userWeight.collectAsState()
    val storedHeight by viewModel.userHeight.collectAsState()
    val storedAge by viewModel.userAge.collectAsState()
    val storedStepGoal by viewModel.stepGoal.collectAsState()
    val storedCalGoal by viewModel.calorieGoal.collectAsState()

    // Local state for editing
    var name by remember { mutableStateOf(storedName) }
    var weight by remember { mutableStateOf(storedWeight) }
    var height by remember { mutableStateOf(storedHeight) }
    var age by remember { mutableStateOf(storedAge) }

    var stepGoal by remember { mutableStateOf(storedStepGoal) }
    var calorieGoal by remember { mutableStateOf(storedCalGoal) }

    // Keep local state in sync if backing store updates
    LaunchedEffect(storedName) { name = storedName }
    LaunchedEffect(storedWeight) { weight = storedWeight }
    // ... similar for others if needed, but for edit fields, initializing once or updating on save is usually enough

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Profile & Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Manage your preferences and goals", color = Color.Gray, fontSize = 14.sp)
            }
        }

        // Personal Info Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Person, null, tint = Color(0xFF111827))
                        Spacer(Modifier.width(8.dp))
                        Text("Personal Information", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
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
                            // 3. Actual Save Logic
                            viewModel.saveUserProfile(name, weight, height, age)
                            Toast.makeText(context, "Changes Saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Changes")
                    }
                }
            }
        }

        // Daily Goals Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.TrackChanges, null, tint = Color(0xFF111827))
                        Spacer(Modifier.width(8.dp))
                        Text("Daily Goals", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                    Spacer(Modifier.height(16.dp))

                    ProfileTextField(label = "Daily Step Goal", value = stepGoal, onValueChange = { stepGoal = it }, isNumber = true)
                    Spacer(Modifier.height(12.dp))
                    ProfileTextField(label = "Daily Calorie Goal", value = calorieGoal, onValueChange = { calorieGoal = it }, isNumber = true)

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            // 3. Actual Save Logic
                            viewModel.saveGoals(stepGoal, calorieGoal)
                            Toast.makeText(context, "Goals Saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Goals")
                    }
                }
            }
        }

        // Appearance Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isDark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode, null, tint = Color(0xFF111827))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Dark Mode", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
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

        // 3. Removed Data Management, Added Log Out
        item {
            OutlinedButton(
                onClick = {
                    // Navigate back to Login
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

// --- HELPER COMPONENTS ---

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
                unfocusedContainerColor = Color(0xFFF9FAFB),
                focusedContainerColor = Color(0xFFF9FAFB),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun ModernStatCard(title: String, current: Int, total: Int, unit: String, icon: ImageVector, accentColor: Color, bgColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                Box(
                    modifier = Modifier
                        .background(bgColor, RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$current", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Spacer(Modifier.width(4.dp))
                Text("/ $total $unit", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
            }
            Spacer(Modifier.height(12.dp))

            val progress = (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE5E7EB))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("${(progress * 100).toInt()}% of daily goal", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ModernActivityItem(log: ActivityLog) {
    val (icon, bg, tint) = when(log.type) {
        "steps" -> Triple(Icons.Default.DirectionsWalk, BlueBackground, BluePrimary)
        "workout" -> Triple(Icons.Default.FitnessCenter, OrangeBackground, OrangePrimary)
        else -> Triple(Icons.Default.Restaurant, GreenBackground, GreenPrimary)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(bg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(log.type.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                Text(
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(log.date)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                val unit = if(log.type == "steps") "steps" else if(log.type == "workout") "mins" else "kcal"
                Text("${log.value} $unit", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
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

    Surface(shadowElevation = 10.dp) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { (route, icon, label) ->
                val selected = currentRoute == route
                NavigationBarItem(
                    icon = {
                        Icon(
                            icon,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            label,
                            fontSize = 10.sp,
                            fontWeight = if(selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selected,
                    onClick = { navController.navigate(route) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    }
}