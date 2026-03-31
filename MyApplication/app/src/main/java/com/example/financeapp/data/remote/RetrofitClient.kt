package com.example.financeapp.data.remote

import android.content.Context
import com.example.financeapp.data.local.PersistentCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://financeappapi.azurewebsites.net/api/"
    
    lateinit var apiService: MyApiService
        private set

    var onSessionExpired: (() -> Unit)? = null

    fun init(context: Context) {
        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(PersistentCookieJar(context))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(MyApiService::class.java)
    }

    fun clearCookies() {
        // Implementation depends on PersistentCookieJar, but usually handled there
    }
}
