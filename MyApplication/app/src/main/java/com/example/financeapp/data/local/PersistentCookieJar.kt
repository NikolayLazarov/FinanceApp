package com.example.financeapp.data.local

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(context: Context) : CookieJar {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val editor = prefs.edit()
        for (cookie in cookies) {
            val key = "${cookie.domain}|${cookie.name}"
            editor.putString(key, cookie.toString())
        }
        editor.apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        for ((_, value) in prefs.all) {
            val cookieStr = value as? String ?: continue
            val cookie = Cookie.parse(url, cookieStr)
            if (cookie != null) {
                cookies.add(cookie)
            }
        }
        return cookies
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
