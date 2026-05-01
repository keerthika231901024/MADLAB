package com.yours.ui.pin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yours.data.prefs.PinPreferences

enum class PinResult { SUCCESS, WRONG_PIN, PIN_SET, PIN_MISMATCH }

class PinViewModel(application: Application) : AndroidViewModel(application) {

    private val pinPrefs = PinPreferences(application)

    private val _pinResult = MutableLiveData<PinResult>()
    val pinResult: LiveData<PinResult> = _pinResult

    fun isPinSet(): Boolean = pinPrefs.isPinSet()

    fun setupPin(pin: String, confirmPin: String) {
        if (pin == confirmPin) {
            pinPrefs.savePin(pin)
            _pinResult.value = PinResult.PIN_SET
        } else {
            _pinResult.value = PinResult.PIN_MISMATCH
        }
    }

    fun verifyPin(pin: String) {
        _pinResult.value = if (pinPrefs.verifyPin(pin)) PinResult.SUCCESS else PinResult.WRONG_PIN
    }
}
