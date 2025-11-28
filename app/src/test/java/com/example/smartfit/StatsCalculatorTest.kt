package com.example.smartfit

import org.junit.Assert.assertEquals
import org.junit.Test

class StatsCalculatorTest {

    // Test 1: Mixed data test
    // Simulate whole day's activity including walking, workout and eating to verify if the totals are correct
    @Test
    fun calculateStats_MixedActivities_ReturnsCorrectTotals() {
        // Mock Data
        val mockLogs = listOf(
            // Walked for 1000 steps will consumes 50 kcal
            ActivityLog(id=1, type = "steps", value = 1000, calories = 50, date = 0L, notes = ""),
            // Running as workout activity will consumes 300 kcal
            ActivityLog(id=2, type = "workout", value = 30, calories = 300, date = 0L, notes = ""),
            // Ate a burger will intake 500 kcal
            ActivityLog(id=3, type = "food", value = 1, calories = 500, date = 0L, notes = "")
        )

        // Execute the calculation
        val result = StatsCalculator.calculateStats(mockLogs)

        // Verify the result
        assertEquals(1000, result.steps)
        assertEquals(350, result.caloriesBurned)
        assertEquals(500, result.caloriesIntake)
        assertEquals(1, result.workouts)
    }

    // Test 2: Empty list
    // Validate if the app can return 0 safely but not crash when there is no data
    @Test
    fun calculateStats_EmptyList_ReturnsZeros() {
        val result = StatsCalculator.calculateStats(emptyList())

        assertEquals(0, result.steps)
        assertEquals(0, result.caloriesBurned)
        assertEquals(0, result.caloriesIntake)
        assertEquals(0, result.workouts)
    }
}