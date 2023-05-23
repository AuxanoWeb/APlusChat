package com.example.auxanochatsdk.Repository

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.auxanochatsdk.model.*
import com.example.auxanochatsdk.network.SocketEvent
import com.example.auxanochatsdk.network.SocketHandler
import com.example.auxanochatsdk.ui.ChatListFragment
import com.example.soketdemo.Model.GroupListModelItem
import com.google.gson.Gson

import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_chat_list.*
import java.util.concurrent.TimeUnit

class GroupRepository(private val activity: Activity) {
    var groupListLiveData = MutableLiveData<List<GroupListModelItem>>()
    val groupLiveData: LiveData<List<GroupListModelItem>>
        get() = groupListLiveData
    lateinit var groupRepositoryList: ArrayList<GroupListModelItem>

    var chatListLiveData = MutableLiveData<PreviousListModel>()
    val chatLiveData: LiveData<PreviousListModel>
        get() = chatListLiveData
    lateinit var chatRepositoryList: PreviousListModel
    lateinit var receiveChatList: ArrayList<ReceiveChatModel>

    val receiveMessage = MutableLiveData<PreviousListModelItem>()
    val receiveLiveData: LiveData<PreviousListModelItem>
        get() = receiveMessage
 //   lateinit var receiveChatModel: ReceiveChatModel
    lateinit var receiveChatModel: PreviousListModelItem


    fun receiveGroupListResponce() {
        groupRepositoryList = ArrayList()
        SocketHandler.mSocket.on(SocketEvent.GROUP_LIST.event) { args ->
            Log.e("receiveGroupList", ": " + args)
            try {
                if (!args.isNullOrEmpty()) {
                    for (i in args.indices) {
                        groupRepositoryList = ArrayList(Gson().fromJson( args.get(i).toString(), Array<GroupListModelItem>::class.java).toList())
                    }
                } else {
                    Log.e("receiveGroupList", "empty list: ")
                }
            } catch (e: Exception) {
                Log.e("receiveGroupList", "error: " + e.message)
            }

            activity.runOnUiThread {
                groupListLiveData.value = groupRepositoryList
            }

        }
    }

    fun receiveChatListResponse() {
        // chatRepositoryList = ArrayList()
        SocketHandler.mSocket.on(SocketEvent.GET_PREVIOUS_CHAT.event) {
            Log.e("receiveChatListResponse", ": " + it.toString())
            val gson = GsonBuilder().create()
            chatRepositoryList = gson.fromJson(it.get(0).toString(), PreviousListModel::class.java)
            /*for (i in it.indices) {
                try {
                    chatRepositoryList = ArrayList(
                        gson.fromJson(
                            it.get(i).toString(),
                            Array<PreviousListModelItem>::class.java
                        )
                            .toList()!!
                    )
                    *//* gson.fromJson(it.get(i).toString(), Array<PreviousListModelItem>::class.java)
                         .toList()!! as ArrayList<PreviousListModelItem>*//*
                } catch (e: Exception) {
                    Log.e("receiveChatListResponse", "error: " + e.message.toString())
                }
                Log.e("receiveChatListResponse", "inner: " + chatRepositoryList.size)

            }*/
            activity.runOnUiThread {
                chatListLiveData.value = chatRepositoryList
            }
        }
    }

    fun receiveChatResponse() {
        SocketHandler.mSocket.on(SocketEvent.RECEIVE_MESSAGE.event) { args ->
            val gson = GsonBuilder().create()
            //receiveChatModel = gson.fromJson(args.get(0).toString(), ReceiveChatModel::class.java)
            receiveChatModel = gson.fromJson(args.get(0).toString(), PreviousListModelItem::class.java)
            Log.e("getReciveChatModel", ": " + args.toString())
            ChatListFragment.isFirstTimeCallChat =true
            receiveChatModel.showLoader=false
            receiveChatModel.isMultiSelect=false
            receiveChatModel.isShowDate=false
          /*  val previousListModelItem = PreviousListModelItem(
                "",
                receiveChatModel.name!!,
                true,
                receiveChatModel.msg!!,
                receiveChatModel.msgId!!,
                "",
                "",
                null,
                receiveChatModel.sentBy!!,
                receiveChatModel.thumbnail!!,
                receiveChatModel.contentType!!,
                receiveChatModel.senderName!!,
                PreviousTimeMilliSeconds(
                    TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()),
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
                ),
                receiveChatModel.type!!,
                null,
                false,
                receiveChatModel.replyUser!!,
                receiveChatModel.replyMsg!!,
                receiveChatModel.replyMsgId!!,
                receiveChatModel.replyUserId!!,
                receiveChatModel.replyMsgType!!,
                receiveChatModel.filePath!!,
                false,
                false,
                receiveChatModel.time!!
            )*/
            activity.runOnUiThread {
                receiveMessage.value = receiveChatModel
            }

        }
    }


}