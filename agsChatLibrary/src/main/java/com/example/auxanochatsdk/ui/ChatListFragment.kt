package com.example.auxanochatsdk.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auxanochatsdk.Activity.LibraryActivity
import com.example.auxanochatsdk.Adapter.MessageAdapter
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Repository.GroupRepository
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.Utils.CommonUtils.getFileTypeFromExtension
import com.example.auxanochatsdk.Utils.CommonUtils.hideKeyboard
import com.example.auxanochatsdk.Utils.CommonUtils.internetCheck
import com.example.auxanochatsdk.Utils.CommonUtils.navigateToOffScreen
import com.example.auxanochatsdk.Utils.CommonUtils.selectedTheme
import com.example.auxanochatsdk.Utils.RecyclerItemClickListener
import com.example.auxanochatsdk.databinding.FragmentChatListBinding
import com.example.auxanochatsdk.model.*
import com.example.auxanochatsdk.network.*
import com.example.auxanochatsdk.ui.UserListFragment.Companion.userName
import com.example.auxanochatsdk.ui.viewModel.ChatListViewModel
import com.example.auxanochatsdk.ui.viewModel.ChatListViewModelFactory
import com.google.gson.GsonBuilder
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.MediaType
import com.lassi.presentation.builder.Lassi
import com.shain.messenger.MessageSwipeController
import com.shain.messenger.ResizeAnim
import com.shain.messenger.SwipeControllerActions
import kotlinx.android.synthetic.main.fragment_chat_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit


class ChatListFragment() : Fragment(), MessageAdapter.QuoteClickListener {

    private val TAG = "chatListFragment"
    lateinit var chatListBinding: FragmentChatListBinding
    private lateinit var viewModelChatList: ChatListViewModel
    lateinit var mContext: Context
    private lateinit var callbackOnBackPressedCallback: OnBackPressedCallback
    lateinit var previousChatArrayList: ArrayList<PreviousListModelItem>
    private var firstVisibleInListview: Int = -1
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var animSlideUp: Animation
    private lateinit var animSlideDown: Animation
    lateinit var groupname: String
    lateinit var groupID: String
    var opponentUserID: String = ""
    lateinit var groupProfilePic: String
    lateinit var groupAdmin: String
    var isGroup: Boolean = false
    var userGroup: ArrayList<Parcelable>? = null
    val SELECT_FILE: Int = 777
    lateinit var activitymain: Activity
    var contentType = ""
    var fileName = ""
    lateinit var exitGroupResponseModel: ExitGroupResponseModel
    val delay: Long = 1000 // 1 seconds after user stops typing
    var chatRecyclerViewState: Parcelable? = null
    var last_text_edit: Long = 0
    val handler = Handler()
    var isTyping = false
    private var quotedMessagePos = -1
    var currentMessageHeight = 0
    var replyTitle = ""
    var replyBody = ""
    var replyMessageId = ""
    var replyUserId = ""
    var replyMsgType = ""
    var loading = false
    lateinit var dateList: ArrayList<String>
    lateinit var multiSelectMsgArrayList: ArrayList<CommonUtils.MultiSelectModel>
    var startAtTime: Long = 0
    lateinit var userPermission: Permission


    companion object {
        var isMultiSelect = false
        var isSwipeMessage = false

        //lateinit var groupDetailModel: GroupListModelItem
        const val ANIMATION_DURATION: Long = 300
        var isFirstTimeCallChat = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate: Called")
        isFirstTimeCallChat = true
        animSlideUp = AnimationUtils.loadAnimation(
            activity,
            R.anim.slide_up
        )
        animSlideDown = AnimationUtils.loadAnimation(
            activity,
            R.anim.slide_down
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //  requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        mContext = container!!.context
        activitymain = mContext as Activity

        Log.e(TAG, "onCreateView: ")
        if (isFirstTimeCallChat) {
            isFirstTimeCallChat = false
            initi(mContext, container)
        }

        CommonUtils.setBackgroundColor(mContext, selectedTheme, chatListBinding.clToolBar)

        val chatRepository = GroupRepository(activitymain)
        viewModelChatList = ViewModelProvider(
            this,
            ChatListViewModelFactory(chatRepository)
        ).get(ChatListViewModel::class.java)
        //  chatListBinding.rvUserChatList.visibility = View.VISIBLE
        chatListBinding.aivAttachment.isEnabled = false
        scrollDateChange()
        receiveMessageBind()
        sendMessage()
        attachMeantFile()
        optionsDialogue()
        checkTypingUser()
        previousChatBind()


        return chatListBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.e(TAG, "onActivityCreated: ")

    }

    private fun checkTypingUser() {
        chatListBinding.messageChatEdt.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                handler.removeCallbacks(input_finish_checker)
            }

            override fun afterTextChanged(editable: Editable?) {
                if (editable!!.length > 0) {
                    last_text_edit = System.currentTimeMillis()
                    handler.postDelayed(input_finish_checker, delay)
                    if (!isTyping) {
                        isTyping = true
                        callTypingSocket(isTyping)
                        Log.e("checkTyping", "afterTextChanged: if" + groupname)
                    }

                } else {
                    isTyping = false
                    callTypingSocket(isTyping)
                    Log.e("checkTyping", "afterTextChanged: else")
                }
            }
        })
    }

    private fun callTypingSocket(typing: Boolean) {
        val typingObj = JSONObject()
        typingObj.put(SocketFields.GROUP_ID, groupID)
        typingObj.put(SocketFields.USER_ID, SocketHandler.myUserId)
        typingObj.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
        typingObj.put(SocketFields.NAME, userName)
        typingObj.put(SocketFields.IS_TYPING, typing.toString())
        SocketHandler.typingUser(typingObj)
    }

    private val input_finish_checker = Runnable {
        if (System.currentTimeMillis() > last_text_edit + delay - 500) {
            isTyping = false
            callTypingSocket(isTyping)
            Log.e("checkTyping", "input_finish_checker: if")

        } else {
            Log.e("checkTyping", "input_finish_checker: else")
        }
    }

    private fun optionsDialogue() {

        chatListBinding.aivMore.setOnClickListener(View.OnClickListener {
            val dialog = Dialog(mContext)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialogue_option_chat_layout)
            val contactInfoTV = dialog.findViewById(R.id.contactInfoTV) as TextView
            val deleteChatTV = dialog.findViewById(R.id.deleteChatTV) as TextView
            val clearChatTV = dialog.findViewById(R.id.clearChatTV) as TextView
            if (isGroup) {
                contactInfoTV.text = resources.getString(R.string.group_info)
                deleteChatTV.text = resources.getString(R.string.delete_group)
            }
            if (UserListFragment.userRoleModel.deleteMessage == 0 && !isGroup) {
                clearChatTV.visibility = View.GONE
            }
            if (UserListFragment.userRoleModel.deleteChat == 0 && !isGroup) {
                deleteChatTV.visibility = View.GONE
            }
            if (isGroup) {
                if (userPermission.clearChat == 0L) {
                    clearChatTV.visibility = View.GONE
                }/* if (groupDetailModel.groupPermission.get(0).permission.clearChat == 0) {
                    clearChatTV.visibility = View.GONE
                }*/

            }
            if (isGroup) {
                if (userPermission.deleteChat == 0L) {
                    deleteChatTV.visibility = View.GONE
                }/* if (groupDetailModel.groupPermission.get(0).permission.deleteChat == 0) {
                    deleteChatTV.visibility = View.GONE
                }*/

            }
            contactInfoTV.setOnClickListener(View.OnClickListener {
                dialog.dismiss()
                if (internetCheck) {
                    if (isMultiSelect) {
                        chatListBinding.aivMore.visibility = View.VISIBLE
                        chatListBinding.aivForward.visibility = View.GONE
                        chatListBinding.aTvSelectedMsg.visibility = View.GONE
                        isMultiSelect = false
                        previousChatArrayList.map { it.isMultiSelect = false }
                        messageAdapter.updateList(previousChatArrayList)
                        multiSelectMsgArrayList = arrayListOf()
                        multiSelectMsgArrayList.clear()
                    }
                    val bundle = Bundle()
                    bundle.putString("groupname", groupname)
                    bundle.putString("groupProfiePic", groupProfilePic)
                    bundle.putBoolean("isGroup", isGroup)
                    bundle.putString("groupID", groupID)
                    //bundle.putString("groupAdmin", groupAdmin)
                    //bundle.putParcelableArrayList("userGroup", userGroup)
                    LibraryActivity.navController?.navigate(
                        R.id.action_chatListFragment_to_userDetailFragment,
                        bundle
                    )
                } else {
                    navigateToOffScreen(internetCheck, mContext)
                }

            })
            deleteChatTV.setOnClickListener(View.OnClickListener {
                dialog.dismiss()
                if (internetCheck) {
                    if (deleteChatTV.text.equals(resources.getString(R.string.delete_chat))) {
                        deleteChat()
                    } else if (deleteChatTV.text.equals(resources.getString(R.string.delete_group))) {
                        deleteGroup()
                    }
                } else {
                    navigateToOffScreen(internetCheck, mContext)
                }

            })
            clearChatTV.setOnClickListener(View.OnClickListener {
                dialog.dismiss()
                if (internetCheck) {
                    clearChat()
                } else {
                    navigateToOffScreen(internetCheck, mContext)
                }

            })
            val window: Window? = dialog.getWindow()
            val wlp = window?.attributes

            wlp!!.gravity = Gravity.TOP or Gravity.RIGHT
            wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            wlp.y = 100
            window.attributes = wlp
            dialog.show()
        })

    }

    private fun clearChat() {
        val dialogBuilder = AlertDialog.Builder(mContext)

        dialogBuilder.setMessage("Are you sure you want to Clear Chat?")
            .setCancelable(false)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                val obj = JSONObject()
                obj.put(SocketFields.GROUP_ID, groupID)
                obj.put(SocketFields.USER_ID, SocketHandler.myUserId)
                obj.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
                SocketHandler.clearChat(obj)
                SocketHandler.mSocket.on(SocketEvent.CLEAR_CHAT_RES.event) {
                    val gson = GsonBuilder().create()
                    exitGroupResponseModel =
                        gson.fromJson(it.get(0).toString(), ExitGroupResponseModel::class.java)
                    if (exitGroupResponseModel.isSuccess!!) {
                        messageAdapter.clearChat()
                    }
                }
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

        val alert = dialogBuilder.create()
        alert.setTitle("Alert")
        alert.show()
    }

    private fun deleteChat() {
        val dialogBuilder = AlertDialog.Builder(mContext)

        dialogBuilder.setMessage("Are you sure you want to Delete Chat?")
            .setCancelable(false)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                val obj = JSONObject()
                obj.put(SocketFields.GROUP_ID, groupID)
                obj.put(SocketFields.USER_ID, SocketHandler.myUserId)
                obj.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
                SocketHandler.deleteChat(obj)
                SocketHandler.mSocket.on(SocketEvent.DELETE_CHAT_RES.event) {
                    val gson = GsonBuilder().create()
                    exitGroupResponseModel =
                        gson.fromJson(it.get(0).toString(), ExitGroupResponseModel::class.java)
                    if (exitGroupResponseModel.isSuccess!!) {
                        SocketHandler.leaveGroup(groupID)
                        requireActivity().runOnUiThread {
                            LibraryActivity.navController?.navigate(R.id.navigateToUserListFragment)
                        }

                    }
                }
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

        val alert = dialogBuilder.create()
        alert.setTitle("Alert")
        alert.show()
    }

    private fun deleteGroup() {
        val dialogBuilder = AlertDialog.Builder(mContext)

        dialogBuilder.setMessage("Are you sure you want to Delete Group?")
            .setCancelable(false)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                val obj = JSONObject()
                obj.put(SocketFields.GROUP_ID, groupID)
                obj.put(SocketFields.USER_ID, SocketHandler.myUserId)
                obj.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
                SocketHandler.deleteGroup(obj)
                SocketHandler.mSocket.on(SocketEvent.DELETE_GROUP_RES.event) {
                    Log.e("getExitRes", ": " + it.get(0).toString())
                    val gson = GsonBuilder().create()
                    exitGroupResponseModel =
                        gson.fromJson(it.get(0).toString(), ExitGroupResponseModel::class.java)
                    if (exitGroupResponseModel.isSuccess!!) {
                        SocketHandler.leaveGroup(groupID)
                        requireActivity().runOnUiThread {
                            LibraryActivity.navController?.navigate(R.id.navigateToUserListFragment)
                        }

                    }
                }
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

        val alert = dialogBuilder.create()
        alert.setTitle("Alert")
        alert.show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun attachMeantFile() {
        chatListBinding.aivAttachment.setOnClickListener(View.OnClickListener {
            if (internetCheck) {
                try {
                    if (requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                        val intent = Lassi(mContext)
                            .setMediaType(MediaType.FILE_TYPE_WITH_SYSTEM_VIEW)
                            .disableCrop()
                            .setSupportedFileTypes(
                                "jpg",
                                "jpeg",
                                "png",
                                "webp",
                                "mp4",
                                "mkv",
                                "webm",
                                "avi",
                                "flv",
                                "3gp",
                                "pdf",
                                "odt",
                                "doc",
                                "docs",
                                "docx",
                                "txt",
                                "ppt",
                                "pptx",
                                "rtf",
                                "xlsx",
                                "mp3",
                                "wav",
                                "wam",
                                "aac",
                                "xls"
                            )  // Filter by required media format (Mandatory)
                            .build()
                        receiveData.launch(intent)

                    }
                } catch (e: Exception) {

                }

            } else {
                navigateToOffScreen(internetCheck, mContext)
            }


        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val receiveData =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                val selectedMedia =
                    it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<MiMedia>
                if(selectedMedia.isNullOrEmpty()){
                    return@registerForActivityResult
                }
                val file = File(selectedMedia.get(0).path!!)
                val file_size_kb = (file.length() / 1024)
                val file_size_mb = (file_size_kb / 1024)
                Log.e(
                    "getDataFromFile",
                    "size" + file_size_mb
                )
                if (!selectedMedia.isNullOrEmpty() && file_size_mb < 20) {
                    var fileType = ""
                    val filename: String = selectedMedia.get(0).path!!.substring(
                        selectedMedia.get(0).path!!.lastIndexOf("/") + 1
                    )
                    Log.e(
                        "getDataFromFile",
                        "filename: " + filename + " size " + selectedMedia.get(0).fileSize
                    )
                    fileType = getFileTypeFromExtension(filename.split(".").get(1))
                    Log.e("getDataFromFile", "fileType: " + fileType)

                    Log.e("getFilePath", ": " + selectedMedia.get(0).name!!)
                    if (fileType.equals("image")) {
                        try {
                            contentType =
                                getMimeType(selectedMedia.get(0).path!!)!!
                            // fileName = File(Uri.parse(selectedMedia.get(0).path!!).path).name
                            fileName = selectedMedia.get(0).name!!
                        } catch (e: Exception) {
                            Log.e("getCatchException", ": " + e.message)
                            contentType = "image/jpeg"
                            fileName = "img_" + System.currentTimeMillis()
                        }
                        sendImageFile(fileType, Uri.parse(selectedMedia.get(0).path))
                        // sendImageFile(fileType, fileUri, displayName)
                    } else if (fileType.equals("video")) {
                        try {
                            contentType =
                                getMimeType(selectedMedia.get(0).path!!)!!
                            //   fileName = File(Uri.parse(selectedMedia.get(0).path!!).path).name
                            fileName = selectedMedia.get(0).name!!
                        } catch (e: Exception) {
                            Log.e("getCatchException", ": " + e.message)
                            contentType = "video/mp4"
                            fileName = "video_" + System.currentTimeMillis()
                        }
                        sendVideoFile(Uri.parse(selectedMedia.get(0).path!!), fileType)
                    } else if (fileType.equals("audio")) {
                        try {
                            contentType =
                                getMimeType(selectedMedia.get(0).path!!)!!
                            Log.e("getContentType", ": " + contentType)
                            // fileName = File(Uri.parse(selectedMedia.get(0).path!!).path).name
                            fileName = selectedMedia.get(0).name!!
                        } catch (e: Exception) {
                            Log.e("getCatchException", ": " + e.message)
                            contentType = "audio/mp3"
                            fileName = "audio_" + System.currentTimeMillis()
                        }
                        sendAudioFile(Uri.parse(selectedMedia.get(0).path!!), fileType)
                    } else if (fileType.equals("document")) {
                        try {
                            contentType =
                                getMimeType(selectedMedia.get(0).path!!)!!
                            // fileName = File(selectedMedia.get(0).path!!).name
                            fileName = selectedMedia.get(0).name!!
                        } catch (e: Exception) {
                            Log.e("getCatchException", ": " + e.message)
                            contentType = "document/docx"
                            fileName = "doc_" + System.currentTimeMillis()
                        }
                        Log.e("getFilePath", ": " + selectedMedia.get(0).path!!)
                        sendDocumentFile(Uri.parse(selectedMedia.get(0).path), fileType)
                    }
                    //ivEmpty.isVisible = selectedMedia.isEmpty()
                    // selectedMediaAdapter.setList(selectedMedia)
                } else {
                    if (selectedMedia.get(0).fileSize > 20000) {
                        Toast.makeText(mContext, "select file below 20mb", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

    fun requestPermission(permission: String): Boolean {
        val isGranted = ContextCompat.checkSelfPermission(
            mContext!!,
            permission
        ) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 100
            )
        }
        return isGranted
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.e("CheckPermision", "call: ")
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //   Log.e("CheckPermision", "Granted: ")
                //   val intent = Intent(Intent.ACTION_GET_CONTENT)
                //  intent.type = "*/*"
                //  intent.addCategory(Intent.CATEGORY_OPENABLE)
                //  startActivityForResult(Intent.createChooser(intent, "Select a file"), SELECT_FILE)
                val intent = Lassi(mContext)
                    .setMediaType(MediaType.FILE_TYPE_WITH_SYSTEM_VIEW)
                    .setCompressionRation(10)
                    .disableCrop()
                    .setMaxFileSize(20000)
                    .setSupportedFileTypes(
                        "jpg",
                        "jpeg",
                        "png",
                        "webp",
                        //     "gif",
                        "mp4",
                        "mkv",
                        "webm",
                        "avi",
                        "flv",
                        "3gp",
                        "pdf",
                        "odt",
                        "doc",
                        "docs",
                        "docx",
                        "txt",
                        "ppt",
                        "pptx",
                        "rtf",
                        "xlsx",
                        "mp3",
                        "wav",
                        "wam",
                        "aac",
                        "xls"
                    )  // Filter by required media format (Mandatory)
                    .build()
                receiveData.launch(intent)
            } else {
                Log.e("CheckPermision", "Not Granted: ")
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        permissions[0]
                    )
                ) {
                    Log.e("CheckPermision", "Manual: ")
                    showPermissionDialog(mContext)
                }

            }
        }
    }

    private fun showPermissionDialog(mContext: Context) {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("Warning!!")
        builder.setMessage("Please allow this permission..")
        builder.setPositiveButton("YES") { dialogInterface, i ->
            dialogInterface.dismiss()
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", mContext.packageName, null)
            intent.data = uri
            mContext.startActivity(intent)
        }

        builder.setNegativeButton("NO") { dialogInterface, i -> dialogInterface.dismiss() }
        builder.show()
    }

    private fun sendMessage() {
        chatListBinding.aivSend.setOnClickListener(View.OnClickListener {
            if (internetCheck) {
                if (!messageChatEdt.text.toString().equals("")) {
                    try {
                        val previousListModelItem = PreviousListModelItem(
                            "",
                            "",
                            false,
                            messageChatEdt.text.toString().trim(),
                            "",
                            "",
                            "",
                            null,
                            SocketHandler.myUserId,
                            "",
                            "",
                            "",
                            PreviousTimeMilliSeconds(
                                TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()),
                                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
                            ),
                            "text",
                            null,
                            true,
                            replyTitle,
                            replyBody,
                            replyMessageId,
                            replyUserId,
                            replyMsgType,
                            "",
                            false,
                            false,
                            0
                        )
                        val date =
                            CommonUtils.fromMillisToDateString(previousListModelItem.timeMilliSeconds.seconds.toLong())
                        if (!dateList.contains(date)) {
                            dateList.add(date)
                            previousListModelItem.isShowDate = true
                        }
                        messageAdapter.insertMessage(previousListModelItem)
                        previousChatArrayList.add(
                            (previousChatArrayList.size),
                            previousListModelItem
                        )

                        val obj = JSONObject()
                        val param = JSONObject()

                        try {
                            obj.put("message", messageChatEdt.text.toString())
                            obj.put(SocketFields.TYPE, "text")
                            obj.put(SocketFields.SENTBY, SocketHandler.myUserId)
                            obj.put(SocketFields.SENDER_NAME, userName)
                            obj.put(SocketFields.REPLY_USER, replyTitle)
                            obj.put(SocketFields.REPLY_MSG, replyBody)
                            obj.put(SocketFields.REPLY_MSG_ID, replyMessageId)
                            obj.put(SocketFields.REPLY_USER_ID, replyUserId)
                            obj.put(SocketFields.REPLY_MSG_TYPE, replyMsgType)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        param.put(SocketFields.MESSAGE_OBJ, obj)
                        param.put(SocketFields.GROUP_ID, groupID)
                        param.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
                        param.put(SocketFields.USER_ID, SocketHandler.myUserId)
                        SocketHandler.sendMessage(param)
                        hideReplyLayout()
                        chatListBinding.rvUserChatList.setItemViewCacheSize(previousChatArrayList!!.size)
                        chatListBinding.rvUserChatList.scrollToPosition(previousChatArrayList!!.size - 1)
                        messageChatEdt.setText("")
                    } catch (e: Exception) {
                        Log.e("getError", ": " + e.message)
                    }
                } else {
                    Toast.makeText(mContext, "Please Enter Text", Toast.LENGTH_SHORT).show()
                }
            } else {
                navigateToOffScreen(internetCheck, mContext)
            }

        })
    }

    private fun receiveMessageBind() {
        viewModelChatList.receiveMessage.observe(viewLifecycleOwner, Observer {
            try {
                if (isFirstTimeCallChat) {
                    isFirstTimeCallChat = false
                    Log.e("getFileType", ": " + it.type)
                    val date =
                        CommonUtils.fromMillisToDateString(it.timeMilliSeconds.seconds.toLong())
                    if (!dateList.contains(date)) {
                        dateList.add(date)
                        it.isShowDate = true
                    }
                    var isLoaderTrue = false
                    if (!it.type.equals("text")) {
                        if (!it.showLoader && it.sentBy == SocketHandler.myUserId) {
                            for (i in previousChatArrayList.indices) {
                                if (previousChatArrayList.get(i).showLoader
                                    && previousChatArrayList.get(i).type.equals(it.type)
                                ) {
                                    Log.e("getFileType", ": " + previousChatArrayList.get(i).type)
                                    it.isShowDate = previousChatArrayList[i].isShowDate
                                    messageAdapter.replaceMessage(it, i)
                                    previousChatArrayList.set((i), it)
                                    isLoaderTrue = true
                                    break
                                }
                            }
                        }

                        if (!isLoaderTrue) {
                            messageAdapter.insertMessage(it)
                            previousChatArrayList.add((previousChatArrayList.size), it)
                            chatListBinding.rvUserChatList.setItemViewCacheSize(
                                previousChatArrayList!!.size
                            )
                            chatListBinding.rvUserChatList.scrollToPosition(previousChatArrayList!!.size - 1)
                        }
                    } else {
                        if (!isLoaderTrue && SocketHandler.myUserId != it.sentBy) {
                            messageAdapter.insertMessage(it)
                            previousChatArrayList.add((previousChatArrayList.size), it)
                            chatListBinding.rvUserChatList.setItemViewCacheSize(
                                previousChatArrayList!!.size
                            )
                            chatListBinding.rvUserChatList.scrollToPosition(previousChatArrayList!!.size - 1)
                        } else if (!isLoaderTrue && SocketHandler.myUserId == it.sentBy) {
                            for ((index, i) in previousChatArrayList.withIndex()) {
                                if (i.message.equals(it.message) && i.msgId.equals("")) {
                                    it.isShowDate = i.isShowDate
                                    messageAdapter.replaceTextMessage(
                                        it,
                                        index
                                    )
                                    previousChatArrayList.set((index), it)
                                    break
                                }
                            }

                        }
                    }
                }


            } catch (e: Exception) {
                Log.e("receiveMessageBind", "error: " + e.message)
            }


        })
    }

    private fun previousChatBind() {
        //   chatListBinding.shimmerLayoutChatList.visibility = View.GONE
        //  chatListBinding.shimmerLayoutChatList.startShimmer()
        viewModelChatList.chatLiveData.observe(viewLifecycleOwner, Observer {
            //  chatListBinding.shimmerLayoutChatList.visibility = View.GONE
            //  chatListBinding.shimmerLayoutChatList.stopShimmer()

            loading = it.hasMore
            Log.e("chekHashMore", ": " + it.hasMore)
            chatListBinding.aivAttachment.isEnabled = true
            if (previousChatArrayList.isEmpty()) {
                chatListBinding.shimmerLayoutChatList.visibility = View.GONE
                chatListBinding.shimmerLayoutChatList.stopShimmer()
                previousChatArrayList = it.messages as ArrayList<PreviousListModelItem>
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        chatListBinding.chatTitleLayout.isEnabled = true
                        chatListBinding.civUserPicture.isEnabled = true
                        chatListBinding.aivMore.isEnabled = true
                    }
                }
                userName = it.groupData.userName
                opponentUserID = it.groupData.opponentUserId
                if (isGroup) {
                    userPermission = it.groupData.userPermission.permission
                    if (userPermission.sendMessage == 0L) {
                        GlobalScope.launch {
                            withContext(Dispatchers.Main) {
                                chatListBinding.clBottom.visibility = View.INVISIBLE
                            }
                        }
                    }

                }
                for (item in previousChatArrayList) {
                    val date =
                        CommonUtils.fromMillisToDateString(item.timeMilliSeconds.seconds.toLong())
                    Log.e("checkDate", ":date " + date)
                    if (!dateList.contains(date)) {
                        dateList.add(date)
                        item.isShowDate = true
                    }
                }
                messageAdapter = MessageAdapter(mContext, isGroup)
                messageAdapter.addMessage(previousChatArrayList, false)
                messageAdapter.setQuoteClickListener(this)
                Log.e("getMessageAadpterSize", "previousChatBind: " + it.toString())

                chatListBinding.rvUserChatList.adapter = messageAdapter
                identifyOnlineUser(it.groupData.onlineStatus)

                val messageSwipeController =
                    MessageSwipeController(mContext, object : SwipeControllerActions {
                        override fun showReplyUI(position: Int) {
                            if (!previousChatArrayList[position].showLoader) {
                                isSwipeMessage = true
                                quotedMessagePos = position
                                replyMessageId = previousChatArrayList[position].msgId
                                showQuotedMessage(previousChatArrayList[position])
                            }

                        }
                    })

                val itemTouchHelper = ItemTouchHelper(messageSwipeController)
                itemTouchHelper.attachToRecyclerView(chatListBinding.rvUserChatList)
                multiSelectionProcees()
                if (previousChatArrayList!!.size > 1) {
                    chatListBinding.rvUserChatList.scrollToPosition(previousChatArrayList!!.size - 1)
                }

            } else {
                if (isFirstTimeCallChat) {
                    isFirstTimeCallChat = false
                    it.messages.reverse()
                    dateList.clear()
                    for (item in it.messages) {
                        previousChatArrayList.add(0, item)
                    }
                    previousChatArrayList.map { it.isShowDate = false }
                    for (item in previousChatArrayList) {
                        val date =
                            CommonUtils.fromMillisToDateString(item.timeMilliSeconds.seconds.toLong())
                        Log.e("checkDate", ":date " + date)
                        if (!dateList.contains(date)) {
                            dateList.add(date)
                            item.isShowDate = true
                        }
                    }
                    messageAdapter.addMessage(previousChatArrayList, false)
                    chatListBinding.rvUserChatList.scrollToPosition(it.messages!!.size - 1)
                }

            }
        })
    }

    private fun identifyOnlineUser(onlineStatus: Boolean) {
        if (onlineStatus && !isGroup) {
            activity?.runOnUiThread {
                chatListBinding.tvOnlineUser.visibility = View.VISIBLE
                chatListBinding.tvOnlineUser.text = "online"
            }
        } else {
            if (!isGroup) {
                activity?.runOnUiThread {
                    if (chatListBinding.tvOnlineUser.visibility == View.VISIBLE) {
                        chatListBinding.tvOnlineUser.visibility = View.GONE
                    }


                }
            }

        }
    }

    private fun multiSelectionProcees() {
        chatListBinding.rvUserChatList.addOnItemTouchListener(
            RecyclerItemClickListener(
                mContext,
                chatListBinding.rvUserChatList,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        if (isMultiSelect &&
                            !previousChatArrayList[position].showLoader
                        ) {
                            multi_select(position)
                        }
                        Log.e("checkClickListener", "onItemClick: " + position)
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                        if (!isMultiSelect &&
                            !previousChatArrayList[position].type.equals("text") &&
                            !isSwipeMessage &&
                            !previousChatArrayList[position].showLoader
                        ) {
                            isMultiSelect = true
                            multiSelectMsgArrayList = ArrayList()
                            multiSelectMsgArrayList.clear()
                            multi_select(position)
                        }
                        if (isSwipeMessage) {
                            isSwipeMessage = false
                        }
                        Log.e("checkClickListener", "onItemLongClick: " + position)
                    }
                })
        )
    }

    private fun multi_select(position: Int) {
        val msgId = previousChatArrayList[position].msgId
        val fileType = previousChatArrayList[position].type
        val fileName = previousChatArrayList[position].fileName

        var url = ""
        var fileContentType = ""
        var thumbnailPath = ""
        if (!fileType.equals("text")) {
            if (fileType.equals("image")) {
                url = previousChatArrayList[position].filePath
                fileContentType = previousChatArrayList[position].contentType
            } else if (fileType.equals("document")) {
                url = previousChatArrayList[position].filePath
                fileContentType = previousChatArrayList[position].contentType
            } else if (fileType.equals("video")) {
                url = previousChatArrayList[position].filePath
                fileContentType = previousChatArrayList[position].contentType
                thumbnailPath = previousChatArrayList[position].thumbnailPath
            } else if (fileType.equals("audio")) {
                url = previousChatArrayList[position].filePath
                fileContentType = previousChatArrayList[position].contentType
            }
            var multiSelectModel =
                CommonUtils.MultiSelectModel(
                    msgId,
                    fileType,
                    url,
                    fileContentType,
                    thumbnailPath,
                    fileName
                )
            if (multiSelectMsgArrayList.contains(multiSelectModel)) {
                multiSelectMsgArrayList.remove(multiSelectModel)
                Log.e("checkArraySelect", "Remove: " + multiSelectMsgArrayList.toString())
                previousChatArrayList[position].isMultiSelect = false
                if (multiSelectMsgArrayList.isNotEmpty()) {
                    chatListBinding.aTvSelectedMsg.text = multiSelectMsgArrayList.size.toString()
                }
            } else {
                previousChatArrayList[position].isMultiSelect = true
                multiSelectMsgArrayList.add(multiSelectModel)
                Log.e("checkArraySelect", "add: " + multiSelectMsgArrayList.toString())
                chatListBinding.aTvSelectedMsg.text = multiSelectMsgArrayList.size.toString()
            }
            CommonUtils.setSelectMessageArray(multiSelectMsgArrayList)
            if (multiSelectMsgArrayList.isNotEmpty()) {
                chatListBinding.aivMore.visibility = View.INVISIBLE
                chatListBinding.aivForward.visibility = View.VISIBLE
                chatListBinding.aTvSelectedMsg.visibility = View.VISIBLE
            } else {
                chatListBinding.aivMore.visibility = View.VISIBLE
                chatListBinding.aivForward.visibility = View.GONE
                chatListBinding.aTvSelectedMsg.visibility = View.GONE
                isMultiSelect = false
            }
            messageAdapter.updateList(previousChatArrayList)
        }


    }

    private fun scrollDateChange() {
        chatListBinding.rvUserChatList.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dx > 0) {
                    Log.e(TAG, "onScrolled: :->Scrolled Right")
                } else if (dx < 0) {
                    Log.e(TAG, "onScrolled: :->Scrolled Left")
                } else {
                    Log.e(TAG, "onScrolled: :->No Horizontal Scrolled")
                }
                if (dy > 0) {
                    onScrollUpdateDateTime("Scrolled Downwards")
                    Log.e(TAG, "onScrolled: :->Scrolled Downwards")
                } else if (dy < 0) {
                    onScrollUpdateDateTime("Scrolled Upwards")
                    Log.e(TAG, "onScrolled: :->Scrolled Upwards")
                    Log.e("getYvalue", ": " + dy)
                } else {
                    Log.e(TAG, "onScrolled: :->No Vertical Scrolled")
                }

                if (mLayoutManager.findFirstCompletelyVisibleItemPosition() > 0) {
                    Log.e("scrollCheck", "SCROLL DOWN")
                } else {
                    if (loading && dy < 0) {
                        loading = false
                        messageAdapter.addMessage(previousChatArrayList, true)
                        startAtTime = previousChatArrayList[0].time - 1
                        Log.e("checkTime", "original: " + previousChatArrayList[0].time)
                        Log.e("checkTime", "minus: " + startAtTime)
                        isFirstTimeCallChat = true
                        SocketHandler.getGroupChat(groupID!!, startAtTime)
                    }
                    Log.e("scrollCheck", "SCROLL UP")
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        Log.e(TAG, "onScrollStateChanged: :->SCROLL_STATE_TOUCH_SCROLL")
                        if (chatListBinding.tvCommonDateTime.visibility == View.GONE) {
                            chatListBinding.tvCommonDateTime.visibility = View.VISIBLE
                            chatListBinding.tvCommonDateTime.startAnimation(animSlideDown)
                        }
                        onScrollUpdateDateTime("SCROLL_STATE_TOUCH_SCROLL")
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        Log.e(TAG, "onScrollStateChanged: :->SCROLL_STATE_SETTLING")
                    }
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        start()
                        Log.e(TAG, "onScrollStateChanged: :->SCROLL_STATE_IDLE")
                    }
                    else -> {
                        Log.e(TAG, "onScrollStateChanged: :->Else")
                    }
                }
            }
        })
    }

    private fun showQuotedMessage(previousListModelItem: PreviousListModelItem) {
        chatListBinding.messageChatEdt.requestFocus()
        val inputMethodManager =
            (mContext as Activity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(
            chatListBinding.messageChatEdt,
            InputMethodManager.SHOW_IMPLICIT
        )
        if (previousListModelItem.type.equals("text")) {
            textQuotedMessage.visibility = View.VISIBLE
            imageQuotedMessage.visibility = View.GONE
            imageVideoQuotedMessage.visibility = View.GONE
            textQuotedMessage.text = previousListModelItem.message
            textQuotedMessage.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                0,
                0
            )
            replyBody = previousListModelItem.message
            setTitleQuote(previousListModelItem)
            setHeightOfReplyLayout(previousListModelItem.type)
        } else if (previousListModelItem.type.equals("image")) {
            textQuotedMessage.visibility = View.GONE
            imageQuotedMessage.visibility = View.VISIBLE
            imageVideoQuotedMessage.visibility = View.GONE
            Glide.with(mContext).load(previousListModelItem.filePath).into(imageQuotedMessage)
            replyBody = previousListModelItem.filePath
            setTitleQuote(previousListModelItem)
            setHeightOfReplyLayout(previousListModelItem.type)
        } else if (previousListModelItem.type.equals("document")) {
            textQuotedMessage.visibility = View.VISIBLE
            imageQuotedMessage.visibility = View.GONE
            imageVideoQuotedMessage.visibility = View.GONE
            textQuotedMessage.text = previousListModelItem.fileName
            replyBody = previousListModelItem.fileName
            setTitleQuote(previousListModelItem)
            setHeightOfReplyLayout(previousListModelItem.type)
        } else if (previousListModelItem.type.equals("video")) {
            textQuotedMessage.visibility = View.GONE
            imageQuotedMessage.visibility = View.VISIBLE
            imageVideoQuotedMessage.visibility = View.VISIBLE
            Glide.with(mContext).load(previousListModelItem.thumbnailPath).into(imageQuotedMessage)
            replyBody = previousListModelItem.thumbnailPath
            setTitleQuote(previousListModelItem)
            setHeightOfReplyLayout(previousListModelItem.type)
        } else if (previousListModelItem.type.equals("audio")) {
            textQuotedMessage.visibility = View.VISIBLE
            imageQuotedMessage.visibility = View.GONE
            imageVideoQuotedMessage.visibility = View.GONE
            textQuotedMessage.text = " " + previousListModelItem.fileName
            textQuotedMessage.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_audio_msg_black,
                0,
                0,
                0
            )
            replyBody = previousListModelItem.fileName
            setTitleQuote(previousListModelItem)
            setHeightOfReplyLayout(previousListModelItem.type)
        }

    }

    private fun setTitleQuote(previousListModelItem: PreviousListModelItem) {
        try {
            val userName =
                previousListModelItem.senderName

            titleQuoteTv.text = userName
            replyTitle = userName
            replyUserId = previousListModelItem.sentBy
            replyMsgType = previousListModelItem.type
            if (SocketHandler.myUserId.equals(previousListModelItem.sentBy)) {
                titleQuoteTv.text = "You"
            }

        } catch (e: Exception) {
            titleQuoteTv.setText(mContext.resources.getString(R.string.str_unknown_person))
            replyTitle = mContext.resources.getString(R.string.str_unknown_person)
        }
    }

    private fun setHeightOfReplyLayout(type: String) {
        Handler().postDelayed(Runnable {
            var height = 0
            if (type.equals("image") || type.equals("video")) {
                height = imageQuotedLayout.getActualHeight() * 2
            } else {
                height = textQuotedMessage.getActualHeight() * 2
            }

            val startHeight = currentMessageHeight

            if (height != startHeight) {

                if (reply_layout.visibility == View.GONE)
                    Handler().postDelayed({
                        reply_layout.visibility = View.VISIBLE
                    }, 50)

                val targetHeight = height - startHeight

                val resizeAnim =
                    ResizeAnim(
                        reply_layout,
                        startHeight,
                        targetHeight
                    )

                resizeAnim.duration = ANIMATION_DURATION
                reply_layout.startAnimation(resizeAnim)

                currentMessageHeight = height

            }
        }, 100)
    }

    private fun hideReplyLayout() {

        val resizeAnim = ResizeAnim(reply_layout, currentMessageHeight, 0)
        resizeAnim.duration = ANIMATION_DURATION

        Handler().postDelayed({
            reply_layout.layout(0, -reply_layout.height, reply_layout.width, 0)
            reply_layout.requestLayout()
            reply_layout.forceLayout()
            reply_layout.visibility = View.GONE

        }, ANIMATION_DURATION - 50)

        reply_layout.startAnimation(resizeAnim)
        currentMessageHeight = 0

        resizeAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                val params = reply_layout.layoutParams
                params.height = 0
                reply_layout.layoutParams = params
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        reSetReplyData()
    }

    private fun reSetReplyData() {
        quotedMessagePos = -1
        replyTitle = ""
        replyBody = ""
        replyMessageId = ""
        replyUserId = ""
        replyMsgType = ""
    }

    private fun onScrollUpdateDateTime(getState: String) {
        Log.e(TAG, "onScrollUpdateDateTime: :->$getState")
        if (getState == "SCROLL_STATE_TOUCH_SCROLL") {
            stop()
        }
        activity?.runOnUiThread(Runnable {
            try {
                val currentFirstVisible: Int = mLayoutManager.findFirstVisibleItemPosition()
                if (currentFirstVisible > firstVisibleInListview) Log.e(
                    "RecyclerView scrolled: ",
                    "scroll up!"
                ) else Log.e("RecyclerView scrolled: ", "scroll down!")
                firstVisibleInListview = currentFirstVisible
                chatListBinding.tvCommonDateTime.text =
                    CommonUtils.fromMillisToDateString(previousChatArrayList[firstVisibleInListview].timeMilliSeconds.seconds.toLong())
                Log.e(TAG, "onScrollStateChanged: :->$getState -> " + currentFirstVisible)
            } catch (e: Exception) {
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_FILE && resultCode != 0) {

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun sendVideoFile(myFile: Uri, fileType: String) {
        val file = File(myFile!!.path)
        /*    val videoFileToByteArray = FileUtils.readFileToByteArray(File(myFile.path))
            Log.e("getVideoByteArray", ": " + videoFileToByteArray)*/
        val previousListModelItem = PreviousListModelItem(
            "",
            "",
            false,
            "",
            "",
            "",
            "",
            null,
            SocketHandler.myUserId,
            "",
            contentType,
            "",
            PreviousTimeMilliSeconds(
                TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()),
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
            ),
            "video",
            null,
            true,
            "",
            "",
            "",
            "",
            "",
            "",
            false,
            false,
            0
        )
        val date =
            CommonUtils.fromMillisToDateString(previousListModelItem.timeMilliSeconds.seconds.toLong())
        if (!dateList.contains(date)) {
            dateList.add(date)
            previousListModelItem.isShowDate = true
        }
        messageAdapter.insertMessage(previousListModelItem)
        previousChatArrayList.add((previousChatArrayList.size), previousListModelItem)
        chatListBinding.rvUserChatList.setItemViewCacheSize(previousChatArrayList!!.size)
        chatListBinding.rvUserChatList.scrollToPosition(previousChatArrayList!!.size - 1)
        Log.e("sendVideoFile", ": " + myFile.path)
        callSendImageApi(fileType, file)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun sendAudioFile(myFile: Uri, fileType: String) {
        val file = File(myFile.path)
        val previousListModelItem = PreviousListModelItem(
            "",
            "",
            false,
            "",
            "",
            "",
            "",
            null,
            SocketHandler.myUserId,
            "",
            contentType,
            "",
            PreviousTimeMilliSeconds(
                TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()),
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
            ),
            fileType,
            null,
            true,
            "",
            "",
            "",
            "",
            "",
            "",
            false,
            false,
            0
        )
        val date =
            CommonUtils.fromMillisToDateString(previousListModelItem.timeMilliSeconds.seconds.toLong())
        if (!dateList.contains(date)) {
            dateList.add(date)
            previousListModelItem.isShowDate = true
        }
        messageAdapter.insertMessage(previousListModelItem)
        previousChatArrayList.add((previousChatArrayList.size), previousListModelItem)
        chatListBinding.rvUserChatList.setItemViewCacheSize(previousChatArrayList!!.size)
        chatListBinding.rvUserChatList.scrollToPosition(previousChatArrayList!!.size - 1)
        callSendImageApi(fileType, file)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun sendDocumentFile(myFile: Uri, fileType: String) {
        val file = File(myFile.path)
        val previousListModelItem = PreviousListModelItem(
            "",
            "",
            false,
            "",
            "",
            "",
            "",
            null,
            SocketHandler.myUserId,
            "",
            contentType,
            "",
            PreviousTimeMilliSeconds(
                TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()),
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
            ),
            fileType,
            null,
            true,
            "",
            "",
            "",
            "",
            "",
            "",
            false,
            false,
            0
        )
        val date =
            CommonUtils.fromMillisToDateString(previousListModelItem.timeMilliSeconds.seconds.toLong())
        if (!dateList.contains(date)) {
            dateList.add(date)
            previousListModelItem.isShowDate = true
        }
        messageAdapter.insertMessage(previousListModelItem)
        previousChatArrayList.add((previousChatArrayList.size), previousListModelItem)
        chatListBinding.rvUserChatList.setItemViewCacheSize(previousChatArrayList!!.size)
        chatListBinding.rvUserChatList.scrollToPosition(previousChatArrayList!!.size - 1)
        callSendImageApi(fileType, file)
        /* var task = sendFileAsyncTask(
             this@ChatListFragment,
             fileName,
             contentType,
             documentFileToByteArray,
             fileType
         )

         task.execute()*/

        // callSendImageApi(fileName, contentType, documentFileToByteArray, fileType)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun sendImageFile(filetype: String, uri: Uri?) {
        val file = File(uri!!.path)
        val filePath: String = file.getPath()

        val previousListModelItem = PreviousListModelItem(
            "",
            "",
            false,
            "",
            "",
            "",
            "",
            null,
            SocketHandler.myUserId,
            "",
            contentType,
            "",
            PreviousTimeMilliSeconds(
                TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()),
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
            ),
            filetype,
            null,
            true,
            "",
            "",
            "",
            "",
            "",
            "",
            false,
            false,
            0
        )
        val date =
            CommonUtils.fromMillisToDateString(previousListModelItem.timeMilliSeconds.seconds.toLong())
        if (!dateList.contains(date)) {
            dateList.add(date)
            previousListModelItem.isShowDate = true
        }
        //  messageAdapter = MessageAdapter(mContext, groupDetailModel)
        messageAdapter.insertMessage(previousListModelItem)
        previousChatArrayList.add((previousChatArrayList.size), previousListModelItem)
        Log.e("getItemCount", "ImageSize: " + previousChatArrayList!!.size)
        chatListBinding.rvUserChatList.setItemViewCacheSize(previousChatArrayList!!.size)
        chatListBinding.rvUserChatList.scrollToPosition(previousChatArrayList!!.size - 1)

        callSendImageApi(filetype, file)
        /* var task = sendFileAsyncTask(
             this@ChatListFragment,
             fileName,
             contentType,
             byteArray,
             filetype
         )
         task.execute()*/


        //  callSendImageApi(fileName, contentType, byteArray, filetype)

    }

    private fun sendFile(file: String, fileType: String, fileName: String) {
        val obj = JSONObject()
        val param = JSONObject()

        try {
            obj.put(SocketFields.FILE, file)
            obj.put(SocketFields.CONTENT_TYPE, contentType)
            obj.put(SocketFields.FILE_NAME, fileName)
            obj.put(SocketFields.TYPE, fileType)
            obj.put(SocketFields.SENTBY, SocketHandler.myUserId)
            obj.put(SocketFields.SENDER_NAME, userName)
            obj.put(SocketFields.REPLY_USER, replyTitle)
            obj.put(SocketFields.REPLY_MSG, replyBody)
            obj.put(SocketFields.REPLY_MSG_ID, replyMessageId)
            obj.put(SocketFields.REPLY_USER_ID, replyUserId)
            obj.put(SocketFields.REPLY_MSG_TYPE, replyMsgType)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        param.put(SocketFields.MESSAGE_OBJ, obj)
        param.put(SocketFields.GROUP_ID, groupID)
        param.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
        param.put(SocketFields.USER_ID, SocketHandler.myUserId)
        Log.e("getImageResponse", "callSocket: " + file)
        SocketHandler.sendMessage(param)
    }

    private fun callSendImageApi(
        fileType: String,
        file: File
    ) {
        val urlString = "http://3.139.188.226:5000/user/public/"

        val retrofitAPI: RetrofitAPI =
            RetrofitInterface.apiService().create(RetrofitAPI::class.java)
        Log.e("getName", ": " + file)

        val filePart = MultipartBody.Part.createFormData(
            "selectFile", fileName, file
                .asRequestBody(
                    requireContext().contentResolver.getType(Uri.fromFile(file)).toString()
                        .toMediaTypeOrNull()
                )
        )
        val callApi: Call<FileUploadResponse>? = retrofitAPI.sendMessageInterface(
            SocketHandler.secretKey.toRequestBody(MultipartBody.FORM),
            fileType.toRequestBody(MultipartBody.FORM),
            SocketHandler.myUserId.toRequestBody(MultipartBody.FORM),
            userName.toRequestBody(MultipartBody.FORM),
            groupID.toRequestBody(MultipartBody.FORM),
            1,
            filePart
        )
        callApi!!.enqueue(object : Callback<FileUploadResponse?> {
            override fun onResponse(
                call: Call<FileUploadResponse?>,
                response: Response<FileUploadResponse?>
            ) {
                Log.e("getImageResponse", "onResponse: " + response.toString())
            }

            override fun onFailure(call: Call<FileUploadResponse?>, t: Throwable) {
                Log.e("getImageResponse", "onError: " + t.message)
            }
        })

    }

    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    private fun getRetrofit(url: String?): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor)
            .connectTimeout(30, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.MINUTES)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit
    }

    private fun initi(mContext: Context?, container: ViewGroup) {
        chatListBinding =
            FragmentChatListBinding.inflate(layoutInflater, container, false)
                .apply { executePendingBindings() }
        groupname = requireArguments().getString("groupName").toString()
        groupID = requireArguments().getString("groupId").toString()
        groupProfilePic = requireArguments().getString("groupProfilePic").toString()
        isGroup = requireArguments().getBoolean("groupType")
        Log.e("getUserId", "groupID: " + groupID)

        previousChatArrayList = ArrayList()
        previousChatArrayList.clear()
        chatListBinding.aivCreateGroup.text = groupname
        chatListBinding.aivBack.setOnClickListener(View.OnClickListener {
            callbackOnBackPressedCallback.handleOnBackPressed()
        })

        if (UserListFragment.userRoleModel.sendMessage == 0 && !isGroup) {
            chatListBinding.clBottom.visibility = View.GONE
        }
        quotedMessagePos = -1
        replyMessageId = ""
        replyTitle = ""
        replyBody = ""
        replyUserId = ""
        replyMsgType = ""
        dateList = ArrayList()
        dateList.clear()
        //Handle BackPressed Event
        onBackPressed()
        chatListBinding.chatTitleLayout.isEnabled = false
        chatListBinding.civUserPicture.isEnabled = false
        chatListBinding.aivMore.isEnabled = false
        chatListBinding.civUserPicture.setOnClickListener(View.OnClickListener {
            if (internetCheck) {
                if (isMultiSelect) {
                    chatListBinding.aivMore.visibility = View.VISIBLE
                    chatListBinding.aivForward.visibility = View.GONE
                    chatListBinding.aTvSelectedMsg.visibility = View.GONE
                    isMultiSelect = false
                    previousChatArrayList.map { it.isMultiSelect = false }
                    messageAdapter.updateList(previousChatArrayList)
                    multiSelectMsgArrayList = arrayListOf()
                    multiSelectMsgArrayList.clear()
                }
                val bundle = Bundle()
                bundle.putString("groupname", groupname)
                bundle.putString("groupProfiePic", groupProfilePic)
                bundle.putBoolean("isGroup", isGroup)
                bundle.putString("groupID", groupID)
                LibraryActivity.navController?.navigate(
                    R.id.action_chatListFragment_to_userDetailFragment,
                    bundle
                )
            } else {
                navigateToOffScreen(internetCheck, mContext!!)
            }

        })
        chatListBinding.chatTitleLayout.setOnClickListener(View.OnClickListener {
            if (internetCheck) {
                //  if (groupDetailModel != null) {
                if (isMultiSelect) {
                    chatListBinding.aivMore.visibility = View.VISIBLE
                    chatListBinding.aivForward.visibility = View.GONE
                    chatListBinding.aTvSelectedMsg.visibility = View.GONE
                    isMultiSelect = false
                    previousChatArrayList.map { it.isMultiSelect = false }
                    messageAdapter.updateList(previousChatArrayList)
                    multiSelectMsgArrayList = arrayListOf()
                    multiSelectMsgArrayList.clear()
                }

                val bundle = Bundle()
                bundle.putString("groupname", groupname)
                bundle.putString("groupProfiePic", groupProfilePic)
                bundle.putBoolean("isGroup", isGroup)
                bundle.putString("groupID", groupID)
                LibraryActivity.navController?.navigate(
                    R.id.action_chatListFragment_to_userDetailFragment,
                    bundle
                )
                //  }

            } else {
                navigateToOffScreen(internetCheck, mContext!!)
            }

        })
        if (!isGroup) {
            Glide.with(mContext!!).load(groupProfilePic)
                .placeholder(mContext.resources.getDrawable(R.drawable.ic_placeholder_profile))
                .into(chatListBinding.civUserPicture)
        } else {
            Log.e("getGroupPhoto", ": " + groupProfilePic)
            Glide.with(mContext!!).load(groupProfilePic)
                .placeholder(mContext.resources.getDrawable(R.drawable.group_place_holder))
                .into(chatListBinding.civUserPicture)
        }

        SocketHandler.getGroupChat(groupID!!, 0)
        chatListBinding.shimmerLayoutChatList.visibility = View.VISIBLE
        chatListBinding.shimmerLayoutChatList.startShimmer()
        mLayoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        chatListBinding.rvUserChatList.layoutManager = mLayoutManager
        chatListBinding.rvUserChatList.setItemAnimator(DefaultItemAnimator())
        firstVisibleInListview = mLayoutManager.findFirstVisibleItemPosition()


        /*SocketHandler.mSocket.on(SocketEvent.UNREAD_COUNT_RES.event) {
            Log.e("UNREAD_COUNT_RES", ": " + it.get(0).toString())
        }*/
        Log.e("getOnlineresponse", "eventCall: ")

        SocketHandler.mSocket.on(SocketEvent.ONLINE_STATUS_RES.event) {
            val gson = GsonBuilder().create()
            val onlineUserModel =
                gson.fromJson(it.get(0).toString(), OnlineUserModel::class.java)
            Log.e("getOnlineresponse", "online: " + onlineUserModel.isOnline)
            Log.e("getOnlineresponse", "userId: " + onlineUserModel.userId)
            if (!isGroup && onlineUserModel.isOnline && onlineUserModel.userId == opponentUserID) {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        chatListBinding.tvOnlineUser.visibility = View.VISIBLE
                        chatListBinding.tvOnlineUser.setText("online")
                    }
                }

            } else {
                if (!isGroup || !onlineUserModel.isOnline) {
                    GlobalScope.launch {
                        withContext(Dispatchers.Main) {
                            Log.e(
                                "getOnlineresponse",
                                "hideOnlineSocket: " + chatListBinding.tvOnlineUser.visibility
                            )
                            chatListBinding.tvOnlineUser.visibility = View.INVISIBLE

                        }
                    }

                }
            }


        }
        SocketHandler.mSocket.on(SocketEvent.TYPING_RES.event) {
            Log.e("typingsocket", ": " + it.toString())
            val gson = GsonBuilder().create()
            val typingResponseModel =
                gson.fromJson(it.get(0).toString(), typingResponseModel::class.java)
            if (isGroup) {
                activity?.runOnUiThread {
                    if (typingResponseModel.isTyping.equals("true")) {
                        chatListBinding.tvOnlineUser.visibility = View.VISIBLE
                        chatListBinding.tvOnlineUser.setText(typingResponseModel.name + " typing...")
                    } else {
                        chatListBinding.tvOnlineUser.visibility = View.INVISIBLE
                        chatListBinding.tvOnlineUser.setText("")
                    }
                }

            } else {
                activity?.runOnUiThread {
                    if (typingResponseModel.isTyping.equals("true")) {
                        chatListBinding.tvOnlineUser.visibility = View.VISIBLE
                        chatListBinding.tvOnlineUser.setText("typing...")
                    } else {
                        chatListBinding.tvOnlineUser.visibility = View.VISIBLE
                        chatListBinding.tvOnlineUser.setText("online")
                    }
                }

            }
        }
        chatListBinding.cancelButton.setOnClickListener {
            hideReplyLayout()
        }
        chatListBinding.aivForward.setOnClickListener {
            chatListBinding.aivMore.visibility = View.VISIBLE
            chatListBinding.aivForward.visibility = View.GONE
            chatListBinding.aTvSelectedMsg.visibility = View.GONE
            isMultiSelect = false
            previousChatArrayList.map { it.isMultiSelect = false }
            messageAdapter.updateList(previousChatArrayList)
            if (CommonUtils.isFromActivity) {
                val navControllRefresh =
                    (context as LibraryActivity).refreshNavigationController()
                navControllRefresh?.navigate(
                    R.id.action_chatListFragment_to_forwardFragment,
                )
            } else {
                LibraryActivity.navController?.navigate(
                    R.id.action_chatListFragment_to_forwardFragment,
                )
            }

        }
    }

    private fun onBackPressed() {
        // This callback will only be called when MyFragment is at least Started.
        // This callback will only be called when MyFragment is at least Started.
        callbackOnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    // Handle the back button event
                    if (isMultiSelect) {
                        chatListBinding.aivMore.visibility = View.VISIBLE
                        chatListBinding.aivForward.visibility = View.GONE
                        chatListBinding.aTvSelectedMsg.visibility = View.GONE
                        isMultiSelect = false
                        multiSelectMsgArrayList.clear()
                        multiSelectMsgArrayList = arrayListOf()
                        previousChatArrayList.map { it.isMultiSelect = false }
                        messageAdapter.updateList(previousChatArrayList)
                    } else {
                        SocketHandler.leaveGroup(requireArguments().getString("groupId"))
                        SocketHandler.mSocket.off(SocketEvent.TYPING_RES.event)
                        //  SocketHandler.mSocket.off(SocketEvent.ONLINE_STATUS_RES.event)
                        chatListBinding.messageChatEdt.hideKeyboard()
                        //  LibraryActivity.navController?.navigate(R.id.navigateToUserListFragment)
                        // fragmentManager!!.popBackStack()
                        isFirstTimeCallChat = true
                        LibraryActivity.navController?.navigateUp()
                    }

                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            callbackOnBackPressedCallback
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: Called")
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d(TAG, "onViewStateRestored: Called")
    }


    var myHandler: Handler = Handler(Looper.getMainLooper())
    private val TIME_TO_WAIT = 2000

    val myRunnable = Runnable {
        Log.e(TAG, "Runnable() Hide View")
        //Hide TextView
        activity?.runOnUiThread {
            Log.e(TAG, "runOnUiThread() Hide View")
            try {
                chatListBinding.tvCommonDateTime.startAnimation(animSlideUp)
                tvCommonDateTime.visibility = View.GONE
            } catch (e: Exception) {
                Log.e("getCatchError", ": " + e.message)
            }

        }
    }

    @SuppressLint("StaticFieldLeak")
    class sendFileAsyncTask(
        private var activity: ChatListFragment,
        private var fileName: String,
        private var contentType: String,
        private var byteArray: ByteArray,
        private var filetype: String
    ) : AsyncTask<Any, Int, Any?>() {
        //  val dialog = ProgressDialog(activity.mContext)
        override fun onPreExecute() {
            super.onPreExecute()
            /* dialog.setMessage("Please wait!!!")
             dialog.setCancelable(false)
             dialog.show()*/

            // do pre stuff such show progress bar
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun doInBackground(vararg req: Any?): Any? {

            // here comes your code that will produce the desired result
            try {
                // activity.callSendImageApi(fileName, contentType, byteArray, filetype)

            } catch (e: Exception) {
                // dialog.dismiss()
            }


            return ""

        }

        // it will update your progressbar
        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)

        }


        override fun onPostExecute(result: Any?) {
            super.onPostExecute(result)
            //  dialog.dismiss()
            // do what needed on pos execute, like to hide progress bar
            return
        }

    }

    private fun TextView.getActualHeight(): Int {
        textQuotedMessage.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        return measuredHeight
    }

    private fun RelativeLayout.getActualHeight(): Int {
        imageQuotedLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        return measuredHeight
    }

    override fun onQuoteClick(position: Int) {
        if (position != 0) {
            chatListBinding.rvUserChatList.smoothScrollToPosition(position - 1)
            messageAdapter.blinkItem(position)
        } else {
            chatListBinding.rvUserChatList.smoothScrollToPosition(position)
            messageAdapter.blinkItem(position)
        }

    }

    fun start() {
        Log.e(TAG, "onStart()")
        myHandler.postDelayed(myRunnable, TIME_TO_WAIT.toLong())
    }

    fun stop() {
        Log.e(TAG, "onStop()")
        myHandler.removeCallbacks(myRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        isMultiSelect = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isMultiSelect = false
    }
}
