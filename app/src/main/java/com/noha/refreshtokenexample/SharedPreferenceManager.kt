package com.noha.refreshtokenexample

import android.content.Context
import android.content.SharedPreferences
import com.noha.refreshtokenexample.SharedPreferenceManager.SharedPreferencesKey.*

object SharedPreferenceManager {

    private const val PREFERENCE_FILE = "com.noha.refreshtoken.example"

    enum class SharedPreferencesKey {
        ACCESS_TOKEN, REFRESH_TOKEN, TOKEN_LAST_UPDATE_TIME
    }

    private val sharedPref: SharedPreferences? =
        MyApplication.applicationContext?.getSharedPreferences(
            PREFERENCE_FILE,
            Context.MODE_PRIVATE
        )

    fun saveToken(token: TokenResponse) {
        sharedPref?.let {
            with(sharedPref.edit()) {
                putString(ACCESS_TOKEN.name, token.accessToken)
                putString(REFRESH_TOKEN.name, token.refreshToken)
                putLong(TOKEN_LAST_UPDATE_TIME.name, token.lastUpdatedTime ?: 0)
                apply()
            }
        }
    }

    fun getToken(): TokenResponse {
        return TokenResponse(
            sharedPref?.getString(ACCESS_TOKEN.name, null),
            sharedPref?.getString(REFRESH_TOKEN.name, null),
            sharedPref?.getLong(TOKEN_LAST_UPDATE_TIME.name, 0) ?: 0
        )
    }
}