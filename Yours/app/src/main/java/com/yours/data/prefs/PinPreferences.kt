package com.yours.data.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PinPreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "yours_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_PIN = "user_pin"
        private const val KEY_PIN_SET = "pin_set"
    }

    fun savePin(pin: String) {
        prefs.edit()
            .putString(KEY_PIN, pin)
            .putBoolean(KEY_PIN_SET, true)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val saved = prefs.getString(KEY_PIN, null)
        return saved == pin
    }

    fun isPinSet(): Boolean {
        return prefs.getBoolean(KEY_PIN_SET, false)
    }

    fun clearPin() {
        prefs.edit()
            .remove(KEY_PIN)
            .putBoolean(KEY_PIN_SET, false)
            .apply()
    }
}
