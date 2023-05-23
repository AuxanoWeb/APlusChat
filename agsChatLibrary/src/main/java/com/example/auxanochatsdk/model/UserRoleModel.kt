package com.example.auxanochatsdk.model

import java.io.Serializable

data class UserRoleModel(
    var createGroup: Int,
    var createOneToOneChat: Int,
    var deleteChat: Int,
    var deleteMessage: Int,
    var editMessage: Int,
    var sendMessage: Int,
    var updateProfile: Int
):Serializable