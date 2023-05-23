package com.example.soketdemo.Model

import com.example.auxanochatsdk.model.LatestTime
import java.io.Serializable

class GroupListModel(get: Any) : ArrayList<GroupListModelItem>()

data class GroupListModelItem(
    val blockUsers: List<Any>,
    val createAt: CreateAt,
    val createdBy: String,
    val groupId: String,
    val groupImage: String,
    var groupPermission: List<GroupPermission>,
    val isDeactivateUser: Boolean,
    val isGroup: Boolean,
    val lastUpdatedAt: LastUpdatedAt,
    var members: List<String>,
    val modifiedAt: ModifiedAt,
    val modifiedBy: String,
    val name: String,
    val online: List<Any>,
    val pinnedGroup: List<Any>,
    val readCount: List<ReadCount>,
    val recentMessage: RecentMessage? = null,
    val secretKey: String,
    val typing: List<Any>,
    val users: List<User>,
    val viewBy: List<String>,
    val groupName: String,
    val imagePath: String,
    val latestTime: LatestTime?=null,
    var msgType: String? = "",
    val recentMsg: String,
    val opponentUserId: String? = "",
    var unreadCount: Int,
    var isChecked:Boolean=false
) : Serializable

data class GroupPermission(
    var permission: Permission,
    val userId: String
) : Serializable

data class Permission(
    var addMember: Int,
    val addProfilePicture: Int,
    val changeGroupName: Int,
    val clearChat: Int,
    val deleteChat: Int,
    val deleteMessage: Int,
    val exitGroup: Int,
    val removeMember: Int,
    val sendMessage: Int
) : Serializable

data class CreateAt(
    val nanoseconds: Int,
    val seconds: Int
) : Serializable

data class LastUpdatedAt(
    val nanoseconds: Int,
    val seconds: Int
) : Serializable

data class ModifiedAt(
    val nanoseconds: Int,
    val seconds: Int
) : Serializable

data class ReadCount(
    var unreadCount: Int,
    val userId: String
) : Serializable

data class RecentMessage(
    val audio: String,
    val document: String,
    val isRead: Boolean,
    val message: String,
    val msgId: String,
    val readBy: String,
    val sentAt: SentAt,
    val sentBy: String,
    val timeMilliSeconds: TimeMilliSeconds,
    val type: String = "",
    val viewBy: List<String>
) : Serializable

data class User(
    val groups: List<String>,
    val mobile_email: String,
    val name: String,
    val profilePicture: String,
    val profile_picture: String,
    val serverUserId: String,
    val userId: String
) : Serializable

data class SentAt(
    val nanoseconds: Int,
    val seconds: Int
) : Serializable

data class TimeMilliSeconds(
    val nanoseconds: Int,
    val seconds: Int
) : Serializable