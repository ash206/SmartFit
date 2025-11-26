package com.example.smartfit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

// --- RETROFIT (API) ---

// 1. Updated Data Class to match the Exercises API JSON
data class Exercise(
    val name: String,
    val type: String,
    val difficulty: String,
    val instructions: String
)

interface ApiService {
    @Headers("X-Api-Key: XOadxc0boozLTF/JtfCLDw==QUNlS4Ccs7c3a3rm")
    @GET("v1/exercises")
    suspend fun getExercises(
        @Query("type") type: String // We will filter by 'cardio', 'strength', etc.
    ): List<Exercise>
}

object RetrofitClient {
    private const val BASE_URL = "https://api.api-ninjas.com/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}