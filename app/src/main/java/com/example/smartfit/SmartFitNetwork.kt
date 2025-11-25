package com.example.smartfit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// --- RETROFIT (API) ---

data class FitnessTip(
    val id: Int,
    val quote: String,
    val author: String
)

// We will use a dummy JSON API for demonstration
// Base URL: https://dummyjson.com/
interface ApiService {
    @GET("quotes/random")
    suspend fun getRandomQuote(): FitnessTip
}

object RetrofitClient {
    private const val BASE_URL = "https://dummyjson.com/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}