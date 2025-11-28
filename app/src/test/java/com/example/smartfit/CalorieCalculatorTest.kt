package com.example.smartfit

import org.junit.Assert.assertEquals
import org.junit.Test

class CalorieCalculatorTest {

    // Test 1: Verify the steps calculation first
    @Test
    fun calculate_Steps_StandardUser_ReturnsCorrectCalories() {
        val result = CalorieCalculator.calculate(
            type = "steps",
            value = 5000,
            notes = "",
            userWeightKg = 70.0,
            userHeightCm = 175.0
        )
        assertEquals(262, result)
    }

    // Test 2: Verify high intensity workout such as running
    @Test
    fun calculate_Workout_Running_ReturnsHighCalorieBurn() {
        val result = CalorieCalculator.calculate(
            type = "workout",
            value = 30,
            notes = "Type: Running",
            userWeightKg = 80.0,
            userHeightCm = 180.0
        )
        assertEquals(462, result)
    }

    // Test 3: Verify low intensity workout such as yoga
    @Test
    fun calculate_Workout_Yoga_ReturnsLowerCalorieBurn() {
        val result = CalorieCalculator.calculate(
            type = "workout",
            value = 60,
            notes = "Type: Yoga",
            userWeightKg = 60.0,
            userHeightCm = 165.0
        )
        assertEquals(189, result)
    }

    // Test 4: Verify the food input
    @Test
    fun calculate_Food_ReturnsInputDirectly() {
        val result = CalorieCalculator.calculate(
            type = "food",
            value = 550,
            notes = "Big Mac",
            userWeightKg = 90.0,
            userHeightCm = 180.0
        )
        assertEquals(550, result)
    }
}