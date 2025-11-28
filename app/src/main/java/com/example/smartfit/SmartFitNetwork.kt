package com.example.smartfit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

    // [New] Create a logging interceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Logs headers and body
    }

    // [New] Create an OkHttpClient and add the interceptor
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // [New] Attach client here
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}