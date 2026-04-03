package com.example.financeapp.data.remote

import android.content.Context
import com.example.financeapp.data.local.PersistentCookieJar
import com.example.financeapp.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5062/"

    lateinit var apiService: MyApiService
        private set

    private lateinit var cookieJar: PersistentCookieJar

    var onSessionExpired: (() -> Unit)? = null

    fun init(context: Context) {
        cookieJar = PersistentCookieJar(context)

        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .addInterceptor(TokenRefreshInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(MyApiService::class.java)
    }

    fun clearCookies() {
        if (::cookieJar.isInitialized) {
            cookieJar.clear()
        }
    }

    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val token = TokenManager.token
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            return chain.proceed(request)
        }
    }

    private class TokenRefreshInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(chain.request())

            if (response.code == 401 && TokenManager.token != null) {
                response.close()

                val refreshed = runBlocking {
                    try {
                        val refreshResponse = apiService.refresh()
                        if (refreshResponse.isSuccessful) {
                            val newToken = refreshResponse.body()?.token
                            if (newToken != null) {
                                TokenManager.token = newToken
                                true
                            } else false
                        } else false
                    } catch (_: Exception) {
                        false
                    }
                }

                if (refreshed) {
                    val newRequest = chain.request().newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer ${TokenManager.token}")
                        .build()
                    return chain.proceed(newRequest)
                } else {
                    TokenManager.clear()
                    onSessionExpired?.invoke()
                }
            }

            return response
        }
    }
}
