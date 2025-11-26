package com.example.smartfit

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

// Data class to hold separated stats
data class SummaryStats(
    val steps: Int = 0,
    val caloriesBurned: Int = 0,
    val caloriesIntake: Int = 0,
    val workouts: Int = 0
)

class SmartFitViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.activityDao()
    private val settingsManager = SettingsManager(application)
    private val context = application.applicationContext

    // --- STATE ---

    // 1. Activities List
    val activities: StateFlow<List<ActivityLog>> = dao.getAllActivities()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Daily Summary (Today)
    val dailySummary = activities.map { logs ->
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000

        val todayLogs = logs.filter { it.date in startOfDay until endOfDay }

        val steps = todayLogs.filter { it.type == "steps" }.sumOf { it.value }

        // Split Calories
        val burned = todayLogs.filter { it.type == "steps" || it.type == "workout" }.sumOf { it.calories }
        val intake = todayLogs.filter { it.type == "food" }.sumOf { it.calories }

        val workouts = todayLogs.count { it.type == "workout" }

        SummaryStats(steps, burned, intake, workouts)
    }.stateIn(viewModelScope, SharingStarted.Lazily, SummaryStats())

    // Weekly Summary (Last 7 Days)
    val weeklySummary = activities.map { logs ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val sevenDaysAgo = calendar.timeInMillis

        val weeklyLogs = logs.filter { it.date >= sevenDaysAgo }

        val steps = weeklyLogs.filter { it.type == "steps" }.sumOf { it.value }

        // Split Calories
        val burned = weeklyLogs.filter { it.type == "steps" || it.type == "workout" }.sumOf { it.calories }
        val intake = weeklyLogs.filter { it.type == "food" }.sumOf { it.calories }

        val workouts = weeklyLogs.count { it.type == "workout" }

        SummaryStats(steps, burned, intake, workouts)
    }.stateIn(viewModelScope, SharingStarted.Lazily, SummaryStats())


    // 2. Dark Mode
    val isDarkMode: StateFlow<Boolean> = settingsManager.isDarkMode
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    // 3. User Profile Data
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val WEIGHT_KEY = stringPreferencesKey("weight")
    private val HEIGHT_KEY = stringPreferencesKey("height")
    private val AGE_KEY = stringPreferencesKey("age")
    private val EMAIL_KEY = stringPreferencesKey("user_email")
    private val PASSWORD_KEY = stringPreferencesKey("user_password")

    // Goals
    private val STEP_GOAL_KEY = stringPreferencesKey("step_goal")
    private val CALORIE_INTAKE_GOAL_KEY = stringPreferencesKey("calorie_goal") // Existing key usually implies Intake (2000)
    private val CALORIE_BURN_GOAL_KEY = stringPreferencesKey("calorie_burn_goal") // New key for burning

    val userEmail = context.dataStore.data.map { it[EMAIL_KEY] ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val userPassword = context.dataStore.data.map { it[PASSWORD_KEY] ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val userName = context.dataStore.data.map { it[USER_NAME_KEY] ?: "Fitness Enthusiast" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "Fitness Enthusiast")

    val userWeight = context.dataStore.data.map { it[WEIGHT_KEY] ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val userHeight = context.dataStore.data.map { it[HEIGHT_KEY] ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val userAge = context.dataStore.data.map { it[AGE_KEY] ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val stepGoal = context.dataStore.data.map { it[STEP_GOAL_KEY] ?: "10000" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "10000")

    val calorieIntakeGoal = context.dataStore.data.map { it[CALORIE_INTAKE_GOAL_KEY] ?: "2000" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "2000")

    val calorieBurnGoal = context.dataStore.data.map { it[CALORIE_BURN_GOAL_KEY] ?: "500" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "500")

    // 4. Fitness Tips Logic
    private val _fitnessTip = MutableStateFlow("Loading tip...")
    val fitnessTip = _fitnessTip.asStateFlow()

    private val tipsList = listOf(
        "Aim for at least 30 minutes of moderate exercise most days.",
        "Drink water before every meal to stay hydrated.",
        "Sleep is crucial for muscle recovery.",
        "Consistency is key. Even a 10-minute walk helps.",
        "Include protein in every meal to support muscle repair.",
        "Stretch after workouts to improve flexibility."
    )

    // Food Database
    val foodDatabase = mapOf(
        "Rice (1 bowl)" to 200,
        "Noodles (1 bowl)" to 350,
        "Bread (1 slice)" to 80,
        "Chicken Breast (100g)" to 165,
        "Fried Chicken (1 pc)" to 300,
        "Burger (1 pc)" to 500,
        "Pizza (1 slice)" to 285,
        "Salad (1 serving)" to 150,
        "Apple (1 pc)" to 95,
        "Banana (1 pc)" to 105,
        "Egg (1 large)" to 78,
        "Coffee (Black)" to 2,
        "Latte / Cappuccino" to 120,
        "Soft Drink (1 can)" to 140
    )

    init {
        fetchTip()
    }

    // --- ACTIONS ---

    fun getActivity(id: Int): kotlinx.coroutines.flow.Flow<ActivityLog?> {
        return dao.getActivityById(id)
    }

    fun addActivity(type: String, value: Int, notes: String, date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val cals = calculateCalories(type, value, notes)
            val log = ActivityLog(
                type = type,
                value = value,
                calories = cals,
                date = date,
                notes = notes
            )
            dao.insert(log)
        }
    }

    fun updateActivity(id: Int, type: String, value: Int, notes: String, date: Long) {
        viewModelScope.launch {
            val cals = calculateCalories(type, value, notes)
            val log = ActivityLog(
                id = id,
                type = type,
                value = value,
                calories = cals,
                date = date,
                notes = notes
            )
            dao.update(log)
        }
    }

    fun deleteActivity(activity: ActivityLog) {
        viewModelScope.launch {
            dao.delete(activity)
        }
    }

    // --- Calculation Logic ---
    private fun calculateCalories(type: String, value: Int, notes: String): Int {
        val weight = userWeight.value.toDoubleOrNull()
        val height = userHeight.value.toDoubleOrNull()

        return when (type) {
            "steps" -> {
                if (weight != null && height != null) {
                    val strideMeters = (height * 0.414) / 100
                    val distanceKm = (value * strideMeters) / 1000
                    (distanceKm * weight * 1.036).toInt()
                } else if (weight != null) {
                    (value * weight * 0.0005).toInt()
                } else {
                    (value * 0.04).toInt()
                }
            }
            "workout" -> {
                val met = when {
                    notes.contains("Type: Running") -> 11.0
                    notes.contains("Type: HIIT") -> 11.5
                    notes.contains("Type: Cycling") -> 8.0
                    notes.contains("Type: Swimming") -> 9.0
                    notes.contains("Type: Weightlifting") -> 4.5
                    notes.contains("Type: Yoga") -> 3.0
                    else -> 5.0
                }

                if (weight != null) {
                    val calsPerMin = (met * 3.5 * weight) / 200
                    (calsPerMin * value).toInt()
                } else {
                    val multiplier = when {
                        notes.contains("Type: Running") -> 10
                        notes.contains("Type: HIIT") -> 12
                        notes.contains("Type: Cycling") -> 8
                        notes.contains("Type: Swimming") -> 8
                        notes.contains("Type: Weightlifting") -> 6
                        notes.contains("Type: Yoga") -> 3
                        else -> 5
                    }
                    value * multiplier
                }
            }
            "food" -> value
            else -> 0
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setDarkMode(enabled)
        }
    }

    fun saveUserCredentials(email: String, pass: String) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[EMAIL_KEY] = email
                prefs[PASSWORD_KEY] = pass
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            dao.clearAll()
        }
    }

    fun fetchTip() {
        _fitnessTip.value = tipsList.random()
    }

    fun saveUserProfile(name: String, weight: String, height: String, age: String) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                if(name.isNotEmpty()) prefs[USER_NAME_KEY] = name
                prefs[WEIGHT_KEY] = weight
                prefs[HEIGHT_KEY] = height
                prefs[AGE_KEY] = age
            }
        }
    }

    fun saveGoals(steps: String, burn: String, intake: String) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[STEP_GOAL_KEY] = steps
                prefs[CALORIE_BURN_GOAL_KEY] = burn
                prefs[CALORIE_INTAKE_GOAL_KEY] = intake
            }
        }
    }
}