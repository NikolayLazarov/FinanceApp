package com.example.myapplication.services

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.models.LoginResult
import com.google.gson.Gson

object TokenManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_JWT = "jwt_token"
    private const val KEY_USER_INFO = "user_info"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs.getString(KEY_JWT, null)
        set(value) = prefs.edit().putString(KEY_JWT, value).apply()

    var userInfo: LoginResult?
        get() {
            val json = prefs.getString(KEY_USER_INFO, null) ?: return null
            return try {
                gson.fromJson(json, LoginResult::class.java)
            } catch (_: Exception) {
                null
            }
        }
        set(value) {
            if (value != null) {
                prefs.edit().putString(KEY_USER_INFO, gson.toJson(value)).apply()
            } else {
                prefs.edit().remove(KEY_USER_INFO).apply()
            }
        }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
