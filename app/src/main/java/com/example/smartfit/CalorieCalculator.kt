package com.example.smartfit

object CalorieCalculator {
    fun calculate(type: String, value: Int, notes: String, userWeightKg: Double?, userHeightCm: Double?): Int {
        return when (type) {
            "steps" -> {
                if (userWeightKg != null && userHeightCm != null) {
                    val strideMeters = (userHeightCm * 0.414) / 100
                    val distanceKm = (value * strideMeters) / 1000
                    (distanceKm * userWeightKg * 1.036).toInt()
                } else {
                    (value * 0.04).toInt() // Fallback
                }
            }
            "workout" -> {
                val met = when {
                    notes.contains("Running") -> 11.0
                    notes.contains("Yoga") -> 3.0
                    else -> 5.0
                }
                if (userWeightKg != null) {
                    ((met * 3.5 * userWeightKg) / 200 * value).toInt()
                } else {
                    value * 5 // Fallback multiplier
                }
            }
            "food" -> value
            else -> 0
        }
    }
}