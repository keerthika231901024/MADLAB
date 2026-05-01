package com.yours.ui.chat

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.yours.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupInput()
        setupObservers()
        viewModel.initChat()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupInput() {
        binding.btnSend.setOnClickListener { sendMessage() }
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isNotEmpty()) {
            viewModel.processInput(text)
            binding.etMessage.setText("")
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(this) { messages ->
            chatAdapter.submitList(messages.toList()) {
                if (messages.isNotEmpty()) {
                    binding.rvMessages.smoothScrollToPosition(messages.size - 1)
                }
            }
        }

        viewModel.isTyping.observe(this) { typing ->
            binding.typingIndicator.visibility = if (typing) View.VISIBLE else View.GONE
        }
    }
}
