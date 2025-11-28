package com.example.smartfit

object StatsCalculator {

    // This is the logic based function to receive a lists of ActivityLog, and return the statistical result
    // Able to handle the mixed type calculation for steps, workout, food
    fun calculateStats(logs: List<ActivityLog>): SummaryStats {
        var steps = 0
        var burned = 0
        var intake = 0
        var workouts = 0

        for (log in logs) {
            when (log.type) {
                "steps" -> {
                    steps += log.value
                    burned += log.calories
                }
                "workout" -> {
                    burned += log.calories
                    workouts += 1
                }
                "food" -> {
                    intake += log.calories
                }
            }
        }

        return SummaryStats(steps, burned, intake, workouts)
    }
}