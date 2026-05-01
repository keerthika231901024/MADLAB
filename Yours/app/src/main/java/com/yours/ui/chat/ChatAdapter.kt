package com.yours.ui.chat

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yours.databinding.ItemMessageBotBinding
import com.yours.databinding.ItemMessageUserBinding
import com.yours.model.Message
import com.yours.model.MessageType
import java.util.Date

class ChatAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_BOT = 1
    }

    override fun getItemViewType(position: Int) =
        if (getItem(position).type == MessageType.USER) TYPE_USER else TYPE_BOT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            UserVH(ItemMessageUserBinding.inflate(inflater, parent, false))
        } else {
            BotVH(ItemMessageBotBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is UserVH -> holder.bind(msg)
            is BotVH -> holder.bind(msg)
        }
    }

    inner class UserVH(private val b: ItemMessageUserBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(msg: Message) {
            b.tvMessage.text = msg.text
            b.tvTime.text = DateFormat.format("hh:mm a", Date(msg.timestamp))
        }
    }

    inner class BotVH(private val b: ItemMessageBotBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(msg: Message) {
            b.tvMessage.text = msg.text
            b.tvTime.text = DateFormat.format("hh:mm a", Date(msg.timestamp))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(old: Message, new: Message) = old.id == new.id
        override fun areContentsTheSame(old: Message, new: Message) = old == new
    }
}
