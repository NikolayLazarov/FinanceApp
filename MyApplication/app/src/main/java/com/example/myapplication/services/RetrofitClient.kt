package com.example.myapplication.services

import android.content.Context
import com.example.myapplication.models.RefreshResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5062/"

    private lateinit var cookieJar: PersistentCookieJar
    private lateinit var _apiService: MyApiService

    val apiService: MyApiService get() = _apiService

    /** Called from the authenticator when refresh fails — forces logout in the UI. */
    var onSessionExpired: (() -> Unit)? = null

    fun init(context: Context) {
        cookieJar = PersistentCookieJar(context)

        val client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(AuthInterceptor())
            .authenticator(TokenAuthenticator())
            .build()

        _apiService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyApiService::class.java)
    }

    fun clearCookies() {
        if (::cookieJar.isInitialized) cookieJar.clear()
    }

    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val path = request.url.encodedPath

            if (path.contains("Authentication/")) {
                return chain.proceed(request)
            }

            val token = TokenManager.token
            return if (token != null) {
                val authedRequest = request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(authedRequest)
            } else {
                chain.proceed(request)
            }
        }
    }

    private class TokenAuthenticator : okhttp3.Authenticator {
        @Volatile
        private var isRefreshing = false

        override fun authenticate(route: okhttp3.Route?, response: Response): okhttp3.Request? {
            if (response.request.url.encodedPath.contains("Authentication/")) {
                return null
            }

            synchronized(this) {
                if (isRefreshing) return null
                isRefreshing = true
            }

            return try {
                val refreshClient = OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .build()

                val refreshRequest = okhttp3.Request.Builder()
                    .url("${BASE_URL}Authentication/Refresh")
                    .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                    .build()

                val refreshResponse = refreshClient.newCall(refreshRequest).execute()

                if (refreshResponse.code == HttpURLConnection.HTTP_OK) {
                    val body = refreshResponse.body?.string()
                    val newToken = com.google.gson.Gson()
                        .fromJson(body, RefreshResponse::class.java)?.token

                    if (newToken != null) {
                        TokenManager.token = newToken
                        response.request.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                    } else {
                        forceLogout()
                        null
                    }
                } else {
                    forceLogout()
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                forceLogout()
                null
            } finally {
                synchronized(this) { isRefreshing = false }
            }
        }

        private fun forceLogout() {
            TokenManager.clear()
            onSessionExpired?.invoke()
        }
    }
}
