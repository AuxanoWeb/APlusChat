package com.example.auxanochatsdk.model

import java.io.Serializable

data class ContactList(
    val list: List<ContactListModel>,
    val success: Boolean
):Serializable

data class ContactListModel(
    val groups: List<String>,
    val mobile_email: String,
    val name: String,
    val profilePicture: String,
    val profile_picture: String,
    val serverUserId: String,
    val userId: String,
    var isChecked:Boolean=false
): Serializable