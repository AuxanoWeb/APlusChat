package com.example.auxanochatsdk.model

data class SendFileModel(
    val contentType: String,
    val file: ByteArray,
    val fileName: String,
    val secretKey: String
)
data class FileUploadResponse(
    val `file`: String,
    val fileName: String,
    val success: Boolean
)