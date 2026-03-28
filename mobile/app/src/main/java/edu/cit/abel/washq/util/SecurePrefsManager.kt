package edu.cit.abel.washq.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import edu.cit.abel.washq.api.ApiConfig

object SecurePrefsManager {

    private fun prefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            ApiConfig.PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveAuthSession(
        context: Context,
        token: String,
        userId: Long,
        userEmail: String,
        userFirstName: String
    ) {
        prefs(context).edit()
            .putString(ApiConfig.AUTH_TOKEN_KEY, token)
            .putLong(ApiConfig.USER_ID_KEY, userId)
            .putString(ApiConfig.USER_EMAIL_KEY, userEmail)
            .putString(ApiConfig.USER_FIRST_NAME_KEY, userFirstName)
            .apply()
    }

    fun getToken(context: Context): String? {
        return prefs(context).getString(ApiConfig.AUTH_TOKEN_KEY, null)
    }

    fun getFirstName(context: Context, fallback: String): String {
        return prefs(context).getString(ApiConfig.USER_FIRST_NAME_KEY, fallback).orEmpty()
    }

    fun clearAuthSession(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
