package com.example.auxanochatsdk.model

import java.io.Serializable

//class PreviousListModel : ArrayList<PreviousListModelItem>()
data class PreviousListModel(
    val hasMore: Boolean,
    val messages: ArrayList<PreviousListModelItem>,
    val groupData: GroupData
) : Serializable

data class GroupData(
    val userPermission: UserPermission,
    val userName: String,
    val opponentUserId: String,
    val onlineStatus: Boolean
) : Serializable

data class UserPermission(
    val permission: Permission,
    val userId: String
) : Serializable

data class Permission(
    val removeMember: Long,
    val addMember: Long,
    val clearChat: Long,
    val deleteChat: Long,
    val sendMessage: Long,
    val deleteMessage: Long,
    val exitGroup: Long,
    val changeGroupName: Long,
    val addProfilePicture: Long
) : Serializable

data class PreviousListModelItem(
    // val audio: String,
    val thumbnailPath: String,
    //  val document: String,
    val fileName: String,
    // val image: String,
    val isRead: Boolean,
    val message: String,
    val msgId: String,
    val readBy: String,
    val secretKey: String,
    val sentAt: PreviousSentAt?,
    val sentBy: String,
    val thumbnail: String,
    val contentType: String,
    val senderName: String,
    val timeMilliSeconds: PreviousTimeMilliSeconds,
    val type: String,
    // val video: String,
    val viewBy: List<String>?,
    var showLoader: Boolean = false,
    var replyUser: String = "",
    var replyMsg: String = "",
    var replyMsgId: String = "",
    var replyUserId: String = "",
    var replyMsgType: String = "",
    var filePath: String = "",
    var isMultiSelect: Boolean = false,
    var isShowDate: Boolean = false,
    val time: Long
) : Serializable

data class PreviousSentAt(
    val nanoseconds: Int,
    val seconds: Int
)

data class PreviousTimeMilliSeconds(
    val nanoseconds: Long,
    val seconds: Long
)