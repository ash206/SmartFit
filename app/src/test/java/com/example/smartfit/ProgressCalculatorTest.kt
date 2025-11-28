package com.example.smartfit

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {

    // Test 1: Normal progress like we use 5000/10000 = 0.5
    @Test
    fun calculateProgress_HalfWay_ReturnsHalf() {
        val result = ProgressCalculator.calculateProgress(5000, 10000)
        assertEquals(0.5f, result, 0.0f)
    }

    // Test 2: Goal achieved or exceeding such as 12000/10000 -> and the result should be strict in 1.0
    @Test
    fun calculateProgress_ExceedGoal_ReturnsOne() {
        val result = ProgressCalculator.calculateProgress(12000, 10000)
        assertEquals(1.0f, result, 0.0f)
    }

    // Test 3: Situation when the target is 0, test result should return 0 but not crash the app
    @Test
    fun calculateProgress_ZeroGoal_ReturnsZero() {
        val result = ProgressCalculator.calculateProgress(100, 0)
        assertEquals(0.0f, result, 0.0f)
    }
}