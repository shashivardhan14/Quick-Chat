package com.example.quickchat.models

import java.util.Date

data class ChatMessage (
    val id: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val timestamp: Date,
    val isSentByCurrentUser: Boolean? = null

    )