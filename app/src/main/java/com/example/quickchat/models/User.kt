package com.example.quickchat.models

data class User(
    val userId: String,
    val username: String,
    val email: String,
    val profilePicUrl: String? = null
)


