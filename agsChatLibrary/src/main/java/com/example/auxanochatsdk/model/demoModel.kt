package com.example.auxanochatsdk.model

class demoModel : ArrayList<demoModelItem>()

data class demoModelItem(
    val groupId: String,
    val groupName: String,
    val imagePath: String,
    val isGroup: Boolean,
    val latestTime: LatestTime,
    val msgType: String,
    val recentMsg: String,
    val unreadCount: Int
)

data class LatestTime(
    val nanoseconds: Int,
    var seconds: Int?=null
)