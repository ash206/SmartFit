package com.example.smartfit

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// --- RETROFIT (API) ---

data class FitnessTip(
    // The new API returns "_id" as a String, not "id" as Int
    @SerializedName("_id") val id: String,

    // The new API calls the text "content", so we map it to your existing "quote" variable
    @SerializedName("content") val quote: String,

    val author: String
)

// We switch to Quotable.io which supports tags like 'sports' and 'health'
interface ApiService {
    @GET("random?tags=sports,health")
    suspend fun getRandomQuote(): FitnessTip
}

object RetrofitClient {
    // Update the Base URL
    private const val BASE_URL = "https://api.quotable.io/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}