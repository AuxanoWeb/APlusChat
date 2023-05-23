package com.example.auxanochatsdk.model

data class typingResponseModel(
    val groupId: String,
    val isTyping: String,
    val name: String,
    val secretKey: String,
    val userId: String
)