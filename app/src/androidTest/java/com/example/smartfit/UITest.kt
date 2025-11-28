package com.example.smartfit

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test

class SmartFitUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // UI Test 1: Verify Login Error Logic
    @Test
    fun loginScreen_emptyFields_showsErrorMessage() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            LoginScreen(navController)
        }

        // Click login without entering data
        composeTestRule.onNodeWithText("Log In").performClick()

        // Check if error message appears
        composeTestRule.onNodeWithText("Please enter email and password!")
            .assertIsDisplayed()
    }

    // UI Test 2: Verify Navigation to Registration
    @Test
    fun loginScreen_clickSignUp_navigatesToRegister() {
        composeTestRule.setContent {
            // Render the full app to allow navigation
            SmartFitApp()
        }

        // Click "Sign Up"
        composeTestRule.onNodeWithText("Sign Up").performClick()

        // Check if "Create Account" title is visible (Unique to Register Screen)
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    // UI Test 3: Verify Profile Screen Elements
    @Test
    fun profileScreen_showsPersonalInformationHeader() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            ProfileScreen(navController, androidx.lifecycle.viewmodel.compose.viewModel())
        }

        // Check for the "Personal Information" header
        composeTestRule.onNodeWithText("Personal Information").assertIsDisplayed()

        // Check for "Dark Mode" toggle
        composeTestRule.onNodeWithText("Dark Mode")
            .performScrollTo() // Scroll to "Dark Mode" because it is at the bottom of the list
            .assertIsDisplayed()
    }
    // UI Test 4: Verify Add Activity Screen UI
    @Test
    fun addActivityScreen_hasActivityTypeOptions() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            AddActivityScreen(navController, viewModel())
        }

        // Verify the screen title
        composeTestRule.onNodeWithText("Add New Activity").assertIsDisplayed()

        // Verify the three main activity types are selectable
        composeTestRule.onNodeWithText("Steps").assertIsDisplayed()
        composeTestRule.onNodeWithText("Workout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }
}