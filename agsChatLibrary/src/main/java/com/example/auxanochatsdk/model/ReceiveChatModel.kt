package com.example.auxanochatsdk.model

data class ReceiveChatModel(
    var msg: String? = "",

    var thumbnail: String? = "",

    var rid: String? = "",
    var msgId: String? = "",
    var type: String? = "",
    var contentType: String? = "",
    var sentBy: String? = "",

    var name: String? = "",
    var time: Long? = 0,

    var image: String = "",

    var document: String? = "",

    var audio: String? = "",

    var video: String? = "",
    var replyUser: String? = "",
    var replyMsg: String? = "",
    var replyMsgId: String? = "",
    var replyUserId: String? = "",
    var replyMsgType: String? = "",
    var senderName: String? = "",
    var filePath: String? = ""
)