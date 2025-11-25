package com.example.smartfit

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SmartFitViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.activityDao()
    private val settingsManager = SettingsManager(application)
    private val context = application.applicationContext

    // --- STATE ---

    // 1. Activities List
    val activities: StateFlow<List<ActivityLog>> = dao.getAllActivities()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 2. Dark Mode
    val isDarkMode: StateFlow<Boolean> = settingsManager.isDarkMode
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    // 3. User Profile Data (Name, Goals, etc.)
    // Defining DataStore Keys
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val WEIGHT_KEY = stringPreferencesKey("weight")
    private val HEIGHT_KEY = stringPreferencesKey("height")
    private val AGE_KEY = stringPreferencesKey("age")
    private val STEP_GOAL_KEY = stringPreferencesKey("step_goal")
    private val CALORIE_GOAL_KEY = stringPreferencesKey("calorie_goal")

    // Exposing flows for UI to observe
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

    val calorieGoal = context.dataStore.data.map { it[CALORIE_GOAL_KEY] ?: "2000" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "2000")

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

    init {
        fetchTip()
    }

    // --- ACTIONS ---

    fun addActivity(type: String, value: Int, notes: String) {
        viewModelScope.launch {
            val cals = when (type) {
                "steps" -> (value * 0.04).toInt()
                "workout" -> value * 5
                "food" -> value
                else -> 0
            }

            val log = ActivityLog(
                type = type,
                value = value,
                calories = cals,
                date = System.currentTimeMillis(),
                notes = notes
            )
            dao.insert(log)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setDarkMode(enabled)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            dao.clearAll()
        }
    }

    fun fetchTip() {
        // Cycle through tips randomly for "Next Tip" function
        _fitnessTip.value = tipsList.random()
    }

    // Save User Info from Register or Profile
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

    // Save Goals from Profile
    fun saveGoals(steps: String, calories: String) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[STEP_GOAL_KEY] = steps
                prefs[CALORIE_GOAL_KEY] = calories
            }
        }
    }
}