package com.example.smartfit

object ProgressCalculator {

    // Calculate the percentage, return the value between 0.0 to 1.0
    fun calculateProgress(current: Int, goal: Int): Float {
        if (goal <= 0) {
            return 0f // avoid the app crash when dividing with 0
        }
        val progress = current.toFloat() / goal.toFloat()

        // ensure the result would not over 1.0 (100%), and will not smaller that 0.0
        return if (progress > 1f) 1f else if (progress < 0f) 0f else progress
    }
}