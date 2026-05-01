package com.yours.ui.pin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yours.R
import com.yours.databinding.ActivityPinBinding
import com.yours.ui.chat.ChatActivity

class PinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinBinding
    private val viewModel: PinViewModel by viewModels()

    private val firstPin = StringBuilder()
    private val confirmPin = StringBuilder()
    private var isConfirmStep = false
    private var isSetupMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isSetupMode = !viewModel.isPinSet()
        updateLabels()
        setupNumberPad()
        setupObservers()
    }

    private fun updateLabels() {
        when {
            isSetupMode && !isConfirmStep -> {
                binding.tvTitle.text = "Set Your PIN"
                binding.tvSubtitle.text = "Create a 4-digit PIN to secure Yours"
            }
            isSetupMode && isConfirmStep -> {
                binding.tvTitle.text = "Confirm PIN"
                binding.tvSubtitle.text = "Enter the same PIN again to confirm"
            }
            else -> {
                binding.tvTitle.text = "Welcome Back"
                binding.tvSubtitle.text = "Enter your PIN to unlock Yours"
            }
        }
    }

    private fun setupNumberPad() {
        val numButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3,
            binding.btn4, binding.btn5, binding.btn6, binding.btn7,
            binding.btn8, binding.btn9
        )
        numButtons.forEachIndexed { digit, btn ->
            btn.setOnClickListener { onDigitPressed(digit.toString()) }
        }
        binding.btnBackspace.setOnClickListener { onBackspace() }
    }

    private fun currentPin(): StringBuilder = if (isConfirmStep) confirmPin else firstPin

    private fun onDigitPressed(digit: String) {
        val pin = currentPin()
        if (pin.length < 4) {
            pin.append(digit)
            updateDots(pin.length)
            if (pin.length == 4) handlePinComplete()
        }
    }

    private fun onBackspace() {
        val pin = currentPin()
        if (pin.isNotEmpty()) {
            pin.deleteCharAt(pin.length - 1)
            updateDots(pin.length)
        }
    }

    private fun updateDots(filled: Int) {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        dots.forEachIndexed { i, dot -> dot.isSelected = i < filled }
    }

    private fun handlePinComplete() {
        if (isSetupMode) {
            if (!isConfirmStep) {
                // Move to confirm step
                isConfirmStep = true
                updateLabels()
                updateDots(0)
            } else {
                viewModel.setupPin(firstPin.toString(), confirmPin.toString())
            }
        } else {
            viewModel.verifyPin(firstPin.toString())
        }
    }

    private fun setupObservers() {
        viewModel.pinResult.observe(this) { result ->
            when (result) {
                PinResult.SUCCESS, PinResult.PIN_SET -> goToChat()
                PinResult.WRONG_PIN -> showError("Wrong PIN. Try again.")
                PinResult.PIN_MISMATCH -> {
                    showError("PINs don't match. Start over.")
                    resetSetup()
                }
            }
        }
    }

    private fun goToChat() {
        startActivity(Intent(this, ChatActivity::class.java))
        finish()
    }

    private fun showError(msg: String) {
        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        binding.pinDotsContainer.startAnimation(shake)
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
        currentPin().clear()
        updateDots(0)
        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvError.visibility = View.GONE
        }, 2000)
    }

    private fun resetSetup() {
        firstPin.clear()
        confirmPin.clear()
        isConfirmStep = false
        updateLabels()
        updateDots(0)
    }
}
