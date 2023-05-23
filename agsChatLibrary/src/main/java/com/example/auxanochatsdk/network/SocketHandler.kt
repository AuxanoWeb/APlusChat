package com.example.auxanochatsdk.network

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.auxanochatsdk.Activity.LibraryActivity
import com.example.auxanochatsdk.ui.ChatListFragment
import com.example.auxanochatsdk.ui.UserListFragment
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.concurrent.TimeUnit


@SuppressLint("StaticFieldLeak")
object SocketHandler {
    lateinit var mSocket: Socket

    var secretKey: String =
        "U2FsdGVkX19wmVtaa5bOlZVjpazEyB3tEX/0BAmWufQjL2AscUo+sZ72L19onNWL" // Demo key


    lateinit var activity: Activity
    val urlString = "http://3.139.188.226:5000/user/public/"

    var myUserId: String = "1" // (for parth)
    //  var myUserId: String = "7" // (for pranay)
    //var myUserId: String = "2" // (for milan bhai)
    //var myUserId: String = "10" // (for pranay3 bhai)

    var baseURL: String = "http://3.139.188.226:5000/"  // live


    @Synchronized
    fun setSocket(activity: Activity) {
        try {
            /* if(url.isNotEmpty()){
                 baseURL=url
             }*/

            val options = IO.Options()
            val clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .writeTimeout(10000, TimeUnit.MILLISECONDS)
            options.callFactory = clientBuilder.build()
            /*   this.myUserId = LibraryActivity.chatUserId
               this.secretKey = LibraryActivity.chatSecretKey
               Log.e("getBaseUrl", "Socket myUserId: " + LibraryActivity.chatUserId)
               Log.e("getBaseUrl", "Socket secretKey: " + LibraryActivity.chatSecretKey)*/
            mSocket = IO.socket(baseURL, options)
            mSocket.connect()
            this.activity = activity
            onConnect()
            onConnectError(activity)
        } catch (e: URISyntaxException) {

        }
    }

    @Synchronized
    fun setSocketForCounter(activity: Activity) {
        try {
            val options = IO.Options()
            val clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .writeTimeout(10000, TimeUnit.MILLISECONDS)
            options.callFactory = clientBuilder.build()
            /*  this.myUserId = LibraryActivity.chatUserId
              this.secretKey = LibraryActivity.chatSecretKey
              Log.e("getBaseUrl", "Socket myUserId: " + LibraryActivity.chatUserId)
              Log.e("getBaseUrl", "Socket secretKey: " + LibraryActivity.chatSecretKey)*/
            mSocket = IO.socket(baseURL, options)
            mSocket.connect()
            this.activity = activity
            onConnectForCounter()
            onConnectError(activity)
        } catch (e: URISyntaxException) {

        }
    }

    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }

    @Synchronized
    fun getUserList(obj: JSONObject) {
        Log.e("getUserDetail", ": " + obj.toString())
        mSocket.emit(SocketEvent.USER_LIST.event, obj)
    }

    @Synchronized
    fun getGroupChat(groupId: String, time: Long) {
        // if (mSocket.connected()) {

        val chatJsonObj = JSONObject()
        try {
            chatJsonObj.put(SocketFields.GROUP_ID, groupId)
            chatJsonObj.put(SocketFields.ID, myUserId)
            chatJsonObj.put(SocketFields.START_AT, time)
            chatJsonObj.put(SocketFields.SECRET_KEY, secretKey)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.e("getChatCall", ":req " + chatJsonObj.toString())
        mSocket.emit(SocketEvent.GET_CHAT.event, chatJsonObj)
        // }
    }

    @Synchronized
    fun getProfileEmit() {
        val chatJsonObj = JSONObject()
        try {
            chatJsonObj.put(SocketFields.USER_ID, myUserId)
            chatJsonObj.put(SocketFields.SECRET_KEY, secretKey)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.e("getProfileEmit", ": " + chatJsonObj.toString())
        mSocket.emit(SocketEvent.GET_PROFILE.event, chatJsonObj)
    }

    @Synchronized
    fun updateProfile(obj: JSONObject) {
        Log.e("updateProfileReq", ": " + obj.toString())
        mSocket.emit(SocketEvent.UPDATE_PROFILE.event, obj)
    }

    @Synchronized
    fun unreadCount(obj: JSONObject) {
        mSocket.emit(SocketEvent.UNREAD_COUNT.event, obj)
    }

    @Synchronized
    fun joinGroup(groupId: String) {
        Log.e("joinGroup", "call: " + groupId)
        mSocket.emit(SocketEvent.JOIN.event, groupId)
    }

    @Synchronized
    fun totalUnreadCount() {
        val obj = JSONObject()
        obj.put(SocketFields.USER_ID, myUserId)
        obj.put(SocketFields.SECRET_KEY, secretKey)
        Log.e("unReadCount", "call: " + obj.toString())
        mSocket.emit(SocketEvent.USER_UNREAD_COUNT.event, obj)
    }


    @Synchronized
    fun getGroup(msg: JSONObject) {
        // if (mSocket.connected()) {
        Log.e("getSocketCall", "call: " + msg.toString())
        mSocket.emit(SocketEvent.GET_GROUPS.event, msg)
        /* }else{
             Log.e("getSocketCall", "not call: ")
         }*/
    }

    @Synchronized
    fun getGroupSecond(msg: JSONObject) {
        // if (mSocket.connected()) {
        Log.e("getGroupResponse", ": " + msg.toString())
        mSocket.emit(SocketEvent.GET_GROUP.event, msg)
        /* }else{
             Log.e("getSocketCall", "not call: ")
         }*/
    }

    @Synchronized
    fun setUserDetail(secretKey: String, userId: String) {
        this.secretKey = secretKey
        this.myUserId = userId
    }

    @Synchronized
    fun sendMessage(msg: JSONObject) {
        // if (mSocket.connected()) {
        Log.e("getImage", ": " + msg.toString())
        mSocket.emit(SocketEvent.SEND_MESSAGE.event, msg)
        // }
    }

    @Synchronized
    fun addMember(memberObj: JSONObject) {
        //  if (mSocket.connected()) {
        Log.e("addMember", ": " + memberObj.toString())
        mSocket.emit(SocketEvent.ADD_MEMBER.event, memberObj)
        // }
    }

    @Synchronized
    fun removeMember(memberObj: JSONObject) {
        //if (mSocket.connected()) {
        Log.e("removeMember", ": " + memberObj.toString())
        mSocket.emit(SocketEvent.REMOVE_MEMBER.event, memberObj)
        //}
    }


    @Synchronized
    fun leaveGroup(groupId: String?) {
        // if (mSocket.connected()) {
        mSocket.off(SocketEvent.RECEIVE_MESSAGE.event)
        val obj = JSONObject()
        obj.put(SocketFields.GROUP_ID, groupId)
        obj.put(SocketFields.USER_ID, myUserId)
        obj.put(SocketFields.SECRET_KEY, secretKey)
        Log.e("leaveGroup", ": " + obj.toString())
        mSocket.emit(SocketEvent.LEAVE_GROUP.event, obj)
        // }
    }

    @Synchronized
    fun forwardFile(obj: JSONObject) {
        Log.e("forwardMessageObj", ": " + obj.toString())
        mSocket.emit(SocketEvent.FORWARD_FILE.event, obj)
    }


    @Synchronized
    fun clearChat(obj: JSONObject) {
        if (mSocket.connected()) {
            mSocket.emit(SocketEvent.CLEAR_CHAT.event, obj)
        }
    }

    @Synchronized
    fun getPersonalDetail(obj: JSONObject) {
        if (mSocket.connected()) {
            mSocket.emit(SocketEvent.USER_DETAIL.event, obj)
        }
    }

    @Synchronized
    fun crateGroup(obj: JSONObject) {
        //if (mSocket.connected()) {
        Log.e("checkData", ": " + obj.toString())
        mSocket.emit(SocketEvent.CREATE_GROUP.event, obj)
        // }
    }

    @Synchronized
    fun typingUser(obj: JSONObject) {
        Log.e("typingcall", ": " + obj.toString())
        mSocket.emit(SocketEvent.TYPING.event, obj)

    }

    @Synchronized
    fun onlineUser() {
        val obj = JSONObject()
        obj.put(SocketFields.USER_ID, myUserId)
        obj.put(SocketFields.SECRET_KEY, secretKey)
        Log.e("onlineUser", ": " + obj.toString())
        mSocket.emit(SocketEvent.ONLINE_STATUS.event, obj)
    }

    @Synchronized
    fun getUserRole() {
        val obj = JSONObject()
        obj.put(SocketFields.USER_ID, myUserId)
        obj.put(SocketFields.SECRET_KEY, secretKey)
        Log.e("userRole", ": " + obj.toString())
        mSocket.emit(SocketEvent.USER_ROLE.event, obj)

    }

    @Synchronized
    fun onConnect() {
        mSocket.on(Socket.EVENT_CONNECT) {
            val obj = JSONObject()
            try {
                obj.put(SocketFields.SECRET_KEY, secretKey)
                obj.put(SocketFields.ID, myUserId)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            if (UserListFragment.isFirstTimeCallUserList) {
                UserListFragment.isFirstTimeCallUserList = false
                getGroup(obj)
            }

        }
    }

    @Synchronized
    fun onConnectForCounter() {
        mSocket.on(Socket.EVENT_CONNECT) {
            totalUnreadCount()
            // onlineUser()
        }
    }

    @Synchronized
    fun onConnectError(activity: Activity) {
        mSocket.on(Socket.EVENT_CONNECT_ERROR) {
            activity.runOnUiThread {
                Log.e("getconnection", "connectError: " + it.get(0).toString())
            }
        }

    }

    @Synchronized
    fun exitGroup(obj: JSONObject) {
        mSocket.emit(SocketEvent.EXIT_GROUP.event, obj)
    }

    @Synchronized
    fun deleteChat(obj: JSONObject) {
        mSocket.emit(SocketEvent.DELETE_CHAT.event, obj)
    }


    @Synchronized
    fun deleteGroup(obj: JSONObject) {
        mSocket.emit(SocketEvent.DELETE_GROUP.event, obj)
    }

    @Synchronized
    fun updateGroup(obj: JSONObject) {
        Log.e("updateGroup", ": " + obj.toString())
        mSocket.emit(SocketEvent.UPDATE_GROUP.event, obj)
    }

}