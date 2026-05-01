package com.yours.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yours.data.repository.DataRepository
import com.yours.engine.ChatEngine
import com.yours.model.Message
import com.yours.model.MessageType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DataRepository(application)
    private val chatEngine = ChatEngine(repository)

    private val _messages = MutableLiveData<MutableList<Message>>(mutableListOf())
    val messages: LiveData<MutableList<Message>> = _messages

    private val _isTyping = MutableLiveData(false)
    val isTyping: LiveData<Boolean> = _isTyping

    fun initChat() {
        val list = _messages.value ?: mutableListOf()
        if (list.isEmpty()) {
            list.add(
                Message(
                    text = "Hey! 👋 I'm Yours — your personal memory keeper.\n\nTell me anything you want to remember:\n• \"My Aadhaar is 1234 5678 9012\"\n• \"Ravi's birthday is March 15\"\n\nAnd ask me later:\n• \"What is my Aadhaar?\"\n• \"When is Ravi's birthday?\"",
                    type = MessageType.BOT
                )
            )
            _messages.value = list
        }
    }

    fun processInput(text: String) {
        val list = _messages.value ?: mutableListOf()
        list.add(Message(text = text, type = MessageType.USER))
        _messages.value = list

        _isTyping.value = true

        viewModelScope.launch {
            delay(500L)
            val result = chatEngine.process(text)
            _isTyping.value = false

            val updated = _messages.value ?: mutableListOf()
            updated.add(Message(text = result.response, type = MessageType.BOT))
            _messages.value = updated
        }
    }
}
