package com.example.auxanochatsdk.Adapter

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.webkit.URLUtil
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auxanochatsdk.Activity.LibraryActivity
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.Utils.Constants
import com.example.auxanochatsdk.custom.DownloadFile
import com.example.auxanochatsdk.model.PreviousListModelItem
import com.example.auxanochatsdk.network.SocketHandler
import com.example.auxanochatsdk.ui.ChatListFragment
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.FilenameUtils
import com.lassi.common.extenstions.hide
import com.lassi.common.extenstions.show
import kotlinx.android.synthetic.main.my_file_message.view.*
import kotlinx.android.synthetic.main.my_image_message.view.*
import kotlinx.android.synthetic.main.my_message.view.*
import kotlinx.android.synthetic.main.my_mp3_fille_message.view.*
import kotlinx.android.synthetic.main.my_video_message_layout.view.*
import kotlinx.android.synthetic.main.other_file_message.view.*
import kotlinx.android.synthetic.main.other_image_message.view.*
import kotlinx.android.synthetic.main.other_message.view.*
import kotlinx.android.synthetic.main.other_mp3_fille_message.view.*
import kotlinx.android.synthetic.main.other_video_message_layout.view.*
import java.net.URL


class MessageAdapter(val context: Context, val isGroup: Boolean) :
    RecyclerView.Adapter<MessageViewHolder>(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    private var messages: ArrayList<PreviousListModelItem>? = ArrayList()
    private lateinit var dateShowArrayList: ArrayList<String>
    lateinit var mediaPlayer: MediaPlayer
    var dateTemp = ""
    private var blinkItem = RecyclerView.NO_POSITION
    private val ITEM = 14
    private val FOOTER = 13
    var isShowLoading: Boolean = false
    var isShowHeader: Boolean = false

    interface QuoteClickListener {
        fun onQuoteClick(position: Int)
    }

    private var mQuoteClickListener: QuoteClickListener? = null

    fun setQuoteClickListener(listener: QuoteClickListener) {
        mQuoteClickListener = listener
    }

    fun addMessage(message: ArrayList<PreviousListModelItem>?, isShowLoading: Boolean) {
        messages!!.clear()
        message?.let { this.messages!!.addAll(it) }
        dateShowArrayList = ArrayList()
        this.isShowLoading = isShowLoading
        Log.e("isShowLoading", ": " + this.isShowLoading)
        notifyDataSetChanged()
    }

    fun insertMessage(message: PreviousListModelItem) {
        messages?.add((messages!!.size), message)
        notifyItemInserted(messages!!.size)
    }

    fun replaceMessage(message: PreviousListModelItem, position: Int) {
        messages?.set((position), message)
        notifyItemChanged(position)
    }

    fun replaceTextMessage(message: PreviousListModelItem, position: Int) {
        messages?.set((position), message)
    }

    fun updateList(message: ArrayList<PreviousListModelItem>?) {
        messages!!.clear()
        message?.let { this.messages!!.addAll(it) }
        notifyDataSetChanged()
    }


    fun clearChat() {
        messages?.clear()
        (context as Activity).runOnUiThread {
            notifyDataSetChanged()
        }

    }


    override fun getItemCount(): Int {
        Log.e("getItemCount", "adapterSize: " + messages!!.size)
        return messages!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return messageViewType(position)
    }

    private fun messageViewType(position: Int): Int {
        val typeOfMessage = messages!![position].sentBy
        Log.e("getSendyId", "footer: " + isPositionFooter(position) + " isloading " + isShowLoading)
        Log.e("checkPosition", "position: " + position + " size " + messages!!.size)
        if (isPositionFooter(position)) {
            isShowHeader = true
            return FOOTER
        } else {
            isShowHeader = false
            return if (messages!![position].type == "text") {
                if (typeOfMessage == SocketHandler.myUserId) Constants.VIEW_TYPE_MY_MESSAGE else Constants.VIEW_TYPE_OTHER_MESSAGE
            } else if (messages!![position].type == "image") {
                if (typeOfMessage == SocketHandler.myUserId) Constants.VIEW_TYPE_MY_IMAGE else Constants.VIEW_TYPE_OTHER_IMAGE
            } else if (messages!![position].type == "video") {
                if (typeOfMessage == SocketHandler.myUserId) Constants.VIEW_TYPE_MY_VIDEO else Constants.VIEW_TYPE_OTHER_VIDEO
            } else if (messages!![position].type == "document") {
                if (typeOfMessage == SocketHandler.myUserId) Constants.VIEW_TYPE_MY_DOCUMENT else Constants.VIEW_TYPE_OTHER_DOCUMENT
            } else if (messages!![position].type == "audio") {
                if (typeOfMessage == SocketHandler.myUserId) Constants.VIEW_TYPE_MY_AUDIO else Constants.VIEW_TYPE_OTHER_AUDIO
            } else {
                if (typeOfMessage == SocketHandler.myUserId) Constants.VIEW_TYPE_MY_MESSAGE else Constants.VIEW_TYPE_OTHER_MESSAGE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        mediaPlayer = MediaPlayer()
        // mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        return getLayoutOfChat(viewType, parent)
    }

    private fun getLayoutOfChat(viewType: Int, parent: ViewGroup): MessageViewHolder {
        return when (viewType) {
            Constants.VIEW_TYPE_MY_MESSAGE -> {
                MyMessageViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.my_message, parent, false)
                )
            }

            Constants.VIEW_TYPE_MY_IMAGE -> {
                MyImageViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.my_image_message, parent, false)
                )

            }
            Constants.VIEW_TYPE_MY_DOCUMENT -> {
                MyDocumentViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.my_file_message, parent, false)
                )

            }
            Constants.VIEW_TYPE_MY_AUDIO -> {
                MyAudioViewHolder(
                    LayoutInflater.from(context)
                        .inflate(R.layout.my_mp3_fille_message, parent, false)
                )

            }
            Constants.VIEW_TYPE_MY_VIDEO -> {
                MyVideoViewHolder(
                    LayoutInflater.from(context)
                        .inflate(R.layout.my_video_message_layout, parent, false)
                )

            }
            Constants.VIEW_TYPE_OTHER_VIDEO -> {
                OtherVideoViewHolder(
                    LayoutInflater.from(context)
                        .inflate(R.layout.other_video_message_layout, parent, false)
                )

            }
            Constants.VIEW_TYPE_OTHER_IMAGE -> {
                OtherImageViewHolder(
                    LayoutInflater.from(context)
                        .inflate(R.layout.other_image_message, parent, false)
                )

            }
            Constants.VIEW_TYPE_OTHER_DOCUMENT -> {
                OtherDocumentViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.other_file_message, parent, false)
                )

            }
            Constants.VIEW_TYPE_OTHER_AUDIO -> {
                OtherAudioViewHolder(
                    LayoutInflater.from(context)
                        .inflate(R.layout.other_mp3_fille_message, parent, false)
                )

            }
            FOOTER -> {
                FooterViewHolder(
                    LayoutInflater.from(context)
                        .inflate(R.layout.pagination_footer_loading_chat, parent, false)
                )
            }
            else -> {
                OtherMessageViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.other_message, parent, false)
                )

            }
        }
    }


    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        var message: PreviousListModelItem? = null
        Log.e("getMessageAadpterSize", "onBindViewHolder: " + messages!!.size)
        if (messages!!.size > 0) {
            message = messages!!.get(position)
        }
        if (blinkItem == position) {
            val anim: Animation = AlphaAnimation(0.0f, 0.5f)
            android.os.Handler().postDelayed({
                anim.duration = 300
                holder.itemView.startAnimation(anim)
                anim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        blinkItem = RecyclerView.NO_POSITION
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }
                })
            }, 300)

        }
        holder?.bind(message!!)

    }

    inner class MyMessageViewHolder(view: View) : MessageViewHolder(view) {
        private var messageText: TextView = view.txtMyMessage
        private var timeText: TextView = view.txtMyMessageTime
        private var centerTimeMessagetxt: TextView = view.centerTimeMessagetxt
        private var dateMessagelayout: RelativeLayout = view.dateMessagelayout
        private var reply_layout_my_message: ConstraintLayout = view.reply_layout_my_message
        private var titleQuoteTvMyMessage: TextView = view.titleQuoteTvMyMessage
        private var textQuotedMessageMyMessage: TextView = view.textQuotedMessageMyMessage
        private var imageQuotedMyMessage: ImageView = view.imageQuotedMyMessage
        private var imageQuotedVideoMyMessage: ImageView = view.imageQuotedVideoMyMessage
        private var myMessageMainLayout: LinearLayout = view.myMessageMainLayout
        override fun bind(message: PreviousListModelItem) {
            val date = CommonUtils.fromMillisToDateString(message.timeMilliSeconds.seconds.toLong())
            Log.e("getDateMessage", ": " + date)
            /* if (dateShowArrayList.size == 0 || !dateShowArrayList.contains(date)) {
                 dateShowArrayList.add(date)
                 dateMessagelayout.visibility = View.VISIBLE
                 centerTimeMessagetxt.text = date
                 *//* if(message.showLoader){
                     dateShowArrayList.removeAt(dateShowArrayList.size-1)
                 }*//*
            } else {
                if (dateShowArrayList.contains(date)) {
                    dateMessagelayout.visibility = View.GONE
                }
            }*/
            CommonUtils.setBackgroundDrawable(context,CommonUtils.selectedTheme,myMessageMainLayout)
            CommonUtils.setTextViewChatStyle(context,CommonUtils.selectedTheme,messageText)
            CommonUtils.setTextViewChatStyle(context,CommonUtils.selectedTheme,timeText)
            if (message.isShowDate) {
                dateMessagelayout.visibility = View.VISIBLE
                centerTimeMessagetxt.text = date
            } else {
                dateMessagelayout.visibility = View.GONE
            }
            if (!message.replyMsgId.equals("")) {
                reply_layout_my_message.visibility = View.VISIBLE
                var fileType = ""
                var userId = ""
                try {
                    fileType = message!!.replyMsgType
                    userId = message!!.replyUserId
                } catch (e: Exception) {

                }

                if (fileType.equals("image") || fileType.equals("video")) {
                    imageQuotedMyMessage.visibility = View.VISIBLE
                    textQuotedMessageMyMessage.visibility = View.INVISIBLE
                    if (fileType.equals("video")) {
                        imageQuotedVideoMyMessage.visibility = View.VISIBLE
                    } else {
                        imageQuotedVideoMyMessage.visibility = View.INVISIBLE
                    }
                    Glide.with(context).load(message.replyMsg).into(imageQuotedMyMessage)
                } else {
                    imageQuotedMyMessage.visibility = View.INVISIBLE
                    textQuotedMessageMyMessage.visibility = View.VISIBLE
                    imageQuotedVideoMyMessage.visibility = View.INVISIBLE
                    if (fileType.equals("audio")) {
                        textQuotedMessageMyMessage.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_audio_msg_black,
                            0,
                            0,
                            0
                        )
                        textQuotedMessageMyMessage.text = " " + message.replyMsg
                    } else {
                        textQuotedMessageMyMessage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            0,
                            0
                        )
                        textQuotedMessageMyMessage.text = message.replyMsg
                    }

                }
                if (userId.equals(SocketHandler.myUserId)) {
                    titleQuoteTvMyMessage.text = "You"
                } else {
                    titleQuoteTvMyMessage.text = message.replyUser
                }

                reply_layout_my_message.setOnClickListener(View.OnClickListener {
                    val navigatePosition =
                        messages!!.mapIndexed { i, b -> if (message.replyMsgId.equals(b.msgId)) i else null }
                            .filterNotNull().toList()
//                    Log.e("getnavigationPos", ": " + navigatePosition.get(0))
                    if (navigatePosition.isNotEmpty()) {
                        mQuoteClickListener?.onQuoteClick(navigatePosition.get(0))
                    }

                })

            } else {
                reply_layout_my_message.visibility = View.GONE
            }
            messageText.text = message.message
            timeText.text = message.timeMilliSeconds.seconds?.let {
                CommonUtils.fromMillisToTimeString(
                    it.toLong()
                )
            }
        }
    }

    inner class FooterViewHolder(view: View) : MessageViewHolder(view) {
        private var mLinearLoading: LinearLayout = view.findViewById(R.id.llLoading)

        override fun bind(message: PreviousListModelItem) {
            if (isShowHeader) {
                mLinearLoading.visibility = View.VISIBLE
            } else {
                mLinearLoading.visibility = View.GONE
            }

        }
    }

    inner class MyVideoViewHolder(view: View) : MessageViewHolder(view) {
        private var prevVideoMyIV: ImageView = view.prevVideoMyIV
        private var txtVideoMyMessageTime: TextView = view.txtVideoMyMessageTime
        private var prevVideoPbarMyIV: ProgressBar = view.prevVideoPbarMyIV
        private var centerTimeVideoMessagetxt: TextView = view.centerTimeVideoMessagetxt
        private var dateVideoMessagelayout: RelativeLayout = view.dateVideoMessagelayout
        private var dateMyVideoMessagelayout: LinearLayout = view.dateMyVideoMessagelayout
        private var myVideoMainLayout: RelativeLayout = view.myVideoMainLayout
        private var playBtnMyVideo: ImageView = view.playBtnMyVideo

        override fun bind(message: PreviousListModelItem) {
            val date = CommonUtils.fromMillisToDateString(message.timeMilliSeconds.seconds.toLong())
            CommonUtils.setBackgroundDrawable(context,CommonUtils.selectedTheme,dateMyVideoMessagelayout)
            CommonUtils.setTextViewChatStyle(context,CommonUtils.selectedTheme,txtVideoMyMessageTime)
            if (message.isShowDate) {
                dateVideoMessagelayout.visibility = View.VISIBLE
                centerTimeVideoMessagetxt.text = date
            } else {
                dateVideoMessagelayout.visibility = View.GONE
            }
            if (message.filePath != "") {
                prevVideoPbarMyIV.hide()
                playBtnMyVideo.visibility = View.VISIBLE
            } else {
                prevVideoPbarMyIV.show()
                playBtnMyVideo.visibility = View.GONE
            }
            if (message.isMultiSelect) {
                myVideoMainLayout.setBackgroundColor(context.resources.getColor(R.color.list_item_selected_state))
            } else {
                myVideoMainLayout.setBackgroundColor(context.resources.getColor(R.color.colorTransparent2))
            }
            if (!message.thumbnailPath.isNullOrEmpty()) {
                /*val bytearray = message.thumbnailPath.split(",")
                if (bytearray.size > 1) {
                    val videoThumbByteArray: ByteArray =
                        Base64.decode(bytearray.get(1), Base64.DEFAULT)
                    Glide.with(context).asBitmap().load(videoThumbByteArray).into(prevVideoMyIV)
                }*/
                Glide.with(context).asBitmap().load(message.thumbnailPath).into(prevVideoMyIV)
            }

            txtVideoMyMessageTime.text = message.timeMilliSeconds.seconds?.let {
                CommonUtils.fromMillisToTimeString(
                    it.toLong()
                )
            }
            dateMyVideoMessagelayout.setOnClickListener(View.OnClickListener {
                if (!message.filePath.isNullOrEmpty() &&
                    !ChatListFragment.isMultiSelect
                ) {
                    val bundle = Bundle()
                    bundle.putString("videoString", message.filePath)
                    bundle.putString("videoType", "myVideo")
                    LibraryActivity.navController?.navigate(R.id.videoPlayFragment, bundle)
                }

            })

        }

    }

    inner class MyImageViewHolder(view: View) : MessageViewHolder(view) {
        private var prevImgMyIV: ImageView = view.prevImgMyIV
        private var txtImgMyMessageTime: TextView = view.txtImgMyMessageTime
        private var prevPbarMyIV: ProgressBar = view.prevPbarMyIV
        private var centerTimeImgMessagetxt: TextView = view.centerTimeImgMessagetxt
        private var dateImgMessagelayout: RelativeLayout = view.dateImgMessagelayout
        private var myImageMainLayout: RelativeLayout = view.myImageMainLayout
        private var MyMessageImageLayout: LinearLayout = view.MyMessageImageLayout

        @RequiresApi(Build.VERSION_CODES.M)
        override fun bind(message: PreviousListModelItem) {
            val date = CommonUtils.fromMillisToDateString(message.timeMilliSeconds.seconds.toLong())

            CommonUtils.setBackgroundDrawable(context,CommonUtils.selectedTheme,MyMessageImageLayout)
            CommonUtils.setTextViewChatStyle(context,CommonUtils.selectedTheme,txtImgMyMessageTime)
            if (message.isShowDate) {
                dateImgMessagelayout.visibility = View.VISIBLE
                centerTimeImgMessagetxt.text = date
            } else {
                dateImgMessagelayout.visibility = View.GONE
            }
            if (message.isMultiSelect) {
                myImageMainLayout.setBackgroundColor(context.resources.getColor(R.color.list_item_selected_state))
            } else {
                myImageMainLayout.setBackgroundColor(context.resources.getColor(R.color.colorTransparent2))
            }
            if (message.filePath != "") {
                prevPbarMyIV.hide()
            } else {
                prevPbarMyIV.show()
            }
            Glide.with(context).load(message.filePath).into(prevImgMyIV)

            txtImgMyMessageTime.text = message.timeMilliSeconds.seconds?.let {
                CommonUtils.fromMillisToTimeString(
                    it.toLong()
                )
            }
            Log.e("getMultiSelectStatus", ": " + message.isMultiSelect)
            MyMessageImageLayout.setOnClickListener(View.OnClickListener {
                if (!ChatListFragment.isMultiSelect &&
                    message.filePath != "") {
                    val dialog = Dialog(context)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setCancelable(false)
                    dialog.setContentView(R.layout.dialogue_image_chat_list)
                    val closeimgshowdialogue =
                        dialog.findViewById(R.id.closeimgshowdialogue) as ImageView
                    val dialogueshowimg = dialog.findViewById(R.id.dialogueshowimg) as ImageView
                    val downloadimgshowdialogue =
                        dialog.findViewById(R.id.downloadimgshowdialogue) as ImageView
                    if (URLUtil.isValidUrl(message.filePath)) {
                        Glide.with(context).load(message.filePath).into(dialogueshowimg)
                        Log.e("getBitmap", "url: " + message.filePath)
                    } else {
                        dialogueshowimg.setImageBitmap(CommonUtils.StringToBitMap(message.filePath))
                    }
                    closeimgshowdialogue.bringToFront()
                    downloadimgshowdialogue.bringToFront()
                    closeimgshowdialogue.setOnClickListener {
                        dialog.dismiss()
                    }
                    downloadimgshowdialogue.setOnClickListener {
                        if (message.filePath != "") {
                            if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                val downloadFile =
                                    DownloadFile(
                                        context,
                                        message.filePath,
                                        message.fileName,
                                        "image"
                                    )
                                downloadFile.execute()
                            }

                        }
                    }
                    val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
                    lp.copyFrom(dialog.getWindow()!!.getAttributes())
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT
                    lp.height = WindowManager.LayoutParams.MATCH_PARENT
                    dialog.show()
                    dialog.getWindow()!!.setAttributes(lp)
                }


            })
        }
    }

    inner class MyDocumentViewHolder(view: View) : MessageViewHolder(view) {
        private var txtMyFileMessage: TextView = view.txtMyFileMessage
        private var txtMyFileMessageTime: TextView = view.txtMyFileMessageTime
        private var centerTimeFiletxt: TextView = view.centerTimeFiletxt
        private var dateFilelayout: RelativeLayout = view.dateFilelayout
        private var myFileMainLayout: RelativeLayout = view.myFileMainLayout
        private var prevPbarMyFileMessage: ProgressBar = view.prevPbarMyFileMessage
        private var MyDocumentMainLayout: LinearLayout = view.MyDocumentMainLayout

        @RequiresApi(Build.VERSION_CODES.M)
        override fun bind(message: PreviousListModelItem) {
            super.bind(message)
            val date = CommonUtils.fromMillisToDateString(message.timeMilliSeconds.seconds.toLong())
            CommonUtils.setBackgroundDrawable(context,CommonUtils.selectedTheme,MyDocumentMainLayout)
            CommonUtils.setTextViewChatStyle(context,CommonUtils.selectedTheme,txtMyFileMessage)
            CommonUtils.setTextViewChatStyle(context,CommonUtils.selectedTheme,txtMyFileMessageTime)
            /* if (dateShowArrayList.size == 0 || !dateShowArrayList.contains(date)) {
                 Log.e("checkDateData", "if: " + date + " size " + dateShowArrayList.size)
                 dateShowArrayList.add(date)
                 dateFilelayout.visibility = View.VISIBLE
                 centerTimeFiletxt.text = date

                 if (dateTemp.equals(date) && message.document == "") {
                     dateFilelayout.visibility = View.GONE
                 }
                 dateTemp = date
                 if (message.document == "") {
                     dateShowArrayList.removeAt(dateShowArrayList.size - 1)
                 }
             } else {
                 //  Log.e("checkDateData", "else : "+date+" size "+dateShowArrayList.size )
                 if (dateShowArrayList.contains(date)) {
                     Log.e(
                         "checkDateData",
                         "else contain: " + date + " size " + dateShowArrayList.size
                     )
                     dateFilelayout.visibility = View.GONE
                 }
             }*/
            if (message.isShowDate) {
                dateFilelayout.visibility = View.VISIBLE
                centerTimeFiletxt.text = date
            } else {
                dateFilelayout.visibility = View.GONE
            }
            if (message.isMultiSelect) {
                myFileMainLayout.setBackgroundColor(context.resources.getColor(R.color.list_item_selected_state))
            } else {
                myFileMainLayout.setBackgroundColor(context.resources.getColor(R.color.colorTransparent2))
            }
            if (message.filePath != "") {
                prevPbarMyFileMessage.hide()
                prevPbarMyFileMessage.visibility = View.GONE
                MyDocumentMainLayout.visibility = View.VISIBLE
            } else {
                MyDocumentMainLayout.visibility = View.INVISIBLE
                prevPbarMyFileMessage.visibility = View.VISIBLE
                prevPbarMyFileMessage.show()
            }
            Log.e("getMyFile", ": " + message.filePath)
            if (URLUtil.isValidUrl(message.filePath)) {
                if (message.fileName != "") {
                    txtMyFileMessage.text = message.fileName
                } else {
                    val url = URL(message.filePath)
                    val fileName = FilenameUtils.getName(url.getPath()).split("%2F")
                    txtMyFileMessage.text = "" + fileName.get(1)
                }

            } else {
                // txtMyFileMessage.text = "Document.txt"
            }

            txtMyFileMessageTime.text = message.timeMilliSeconds.seconds?.let {
                CommonUtils.fromMillisToTimeString(
                    it.toLong()
                )
            }
            MyDocumentMainLayout.setOnClickListener(View.OnClickListener {
                if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    /* val rootPath =
                         File(
                             Environment.getExternalStorageDirectory()
                                 .toString() + "/Download/" + context.resources.getString(R.string.app_name) + "/"
                         )
                     if (!rootPath.exists()) {
                         rootPath.mkdirs()
                     }
                     val localFile = File(rootPath, fileName.get(1))*/
                    Log.e("getFileInChat", "document: " + message.filePath)
                    if (message.filePath != "" && !ChatListFragment.isMultiSelect) {
                        val browserIntent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(message.filePath))
                        context.startActivity(browserIntent)
                        /*  val downloadFile = DownloadFile(context, message.document, message.fileName,"document")
                            downloadFile.execute()
                        */
                    }

                    /*  val downloadmanager =
                          context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
                      val uri2 = Uri.parse(message.document)
                      val request = DownloadManager.Request(uri2)
                      request.setTitle(fileName.get(1))
                      request.setDescription("Downloading")
                      request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                      request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                      request.setVisibleInDownloadsUi(true)
                      request.setDestinationInExternalFilesDir(
                          context,
                          Environment.DIRECTORY_DOCUMENTS, fileName.get(1)
                      )
                      downloadmanager!!.enqueue(request)*/
                }
            })

        }
    }

    inner class MyAudioViewHolder(view: View) : MessageViewHolder(view) {
        private var txtMyMp3FileMessage: TextView = view.txtMyMp3FileMessage
        private var txtMyMp3FileMessageTime: TextView = view.txtMyMp3FileMessageTime
        private var centerTimeMp3Messagetxt: TextView = view.centerTimeMp3Messagetxt
        private var dateMp3Messagelayout: RelativeLayout = view.dateMp3Messagelayout
        private var myMp3MainLayout: RelativeLayout = view.myMp3MainLayout
        private var myMp3Layout: RelativeLayout = view.myMp3Layout
        private var audioFileWidgetLayout: LinearLayout = view.audioFileWidgetLayout
        private var prevPbarMyAudioMessage: ProgressBar = view.prevPbarMyAudioMessage
        override fun bind(message: PreviousListModelItem) {
            super.bind(message)
            val date = CommonUtils.fromMillisToDateString(message.timeMilliSeconds.seconds.toLong())
            CommonUtils.setBackgroundDrawable(context,CommonUtils.selectedTheme,myMp3Layout)
            CommonUtils.setTextViewChatStyle(context,CommonUtils.selectedTheme,txtMyMp3FileMessage)
            CommonUtils.setTextViewChatStyle(context,CommonUtils.selectedTheme,txtMyMp3FileMessageTime)
            if (message.isShowDate) {
                dateMp3Messagelayout.visibility = View.VISIBLE
                centerTimeMp3Messagetxt.text = date
            } else {
                dateMp3Messagelayout.visibility = View.GONE
            }
            if (message.isMultiSelect) {
                myMp3MainLayout.setBackgroundColor(context.resources.getColor(R.color.list_item_selected_state))
            } else {
                myMp3MainLayout.setBackgroundColor(context.resources.getColor(R.color.colorTransparent2))
            }
            if (message.filePath != "") {
                prevPbarMyAudioMessage.hide()
                prevPbarMyAudioMessage.visibility = View.GONE
                audioFileWidgetLayout.visibility = View.VISIBLE
            } else {
                audioFileWidgetLayout.visibility = View.INVISIBLE
                prevPbarMyAudioMessage.visibility = View.VISIBLE
                prevPbarMyAudioMessage.show()
            }
            if (URLUtil.isValidUrl(message.filePath)) {
                txtMyMp3FileMessage.text = " " + message.fileName
                txtMyMp3FileMessageTime.text = message.timeMilliSeconds.seconds?.let {
                    CommonUtils.fromMillisToTimeString(
                        it.toLong()
                    )
                }
                myMp3Layout.setOnClickListener(View.OnClickListener {
                    if (!message.filePath.isNullOrEmpty() &&
                        !ChatListFragment.isMultiSelect
                    ) {
                        /* val browserIntent =
                             Intent(Intent.ACTION_VIEW, Uri.parse(message.filePath))
                         context.startActivity(browserIntent)*/
                        openAudioPlayDialogue(message.filePath)


                    }

                })
            }


        }

    }

    inner class OtherMessageViewHolder(view: View) : MessageViewHolder(view) {
        private var messageText: TextView = view.txtOtherMessage
        private var timeText: TextView = view.txtOtherMessageTime
        private var pervUserNameOtherMessage: TextView = view.pervUserNameOtherMessage
        private var centerTimeOtherMessagetxt: TextView = view.centerTimeOtherMessagetxt
        private var dateOtherMessagelayout: RelativeLayout = view.dateOtherMessagelayout
        private var titleQuoteTvOtherMessage: TextView = view.titleQuoteTvOtherMessage
        private var textQuotedMessageOtherMessage: TextView = view.textQuotedMessageOtherMessage
        private var reply_layout_other_message: ConstraintLayout = view.reply_layout_other_message
        private var imageQuotedOtherMessage: ImageView = view.imageQuotedOtherMessage
        private var imageQuotedVideoOtherMessage: ImageView = view.imageQuotedVideoOtherMessage
        override fun bind(message: PreviousListModelItem) {
            if (isGroup) {
                pervUserNameOtherMessage.visibility = View.VISIBLE
                try {
                    /*  val userName =
                          groupDetailModel.users.filter { it.userId == message.sentBy }.get(0).name*/
                    pervUserNameOtherMessage.setText(message.senderName)
                } catch (e: Exception) {
                    pervUserNameOtherMessage.setText(context.resources.getString(R.string.str_unknown_person))
                }

            } else {
                pervUserNameOtherMessage.visibility = View.GONE
            }
            val date = CommonUtils.fromMillisToDateString(message.timeMilliSeconds.seconds.toLong())
            /* if (dateShowArrayList.size == 0 || !dateShowArrayList.contains(date)) {
                 dateShowArrayList.add(date)
                 dateOtherMessagelayout.visibility = View.VISIBLE
                 centerTimeOtherMessagetxt.text = date
             }*/
            if (message.isShowDate) {
                dateOtherMessagelayout.visibility = View.VISIBLE
                centerTimeOtherMessagetxt.text = date
            } else {
                dateOtherMessagelayout.visibility = View.GONE
            }
            if (!message.replyMsgId.equals("")) {
                reply_layout_other_message.visibility = View.VISIBLE
                var fileType = ""
                var userId = ""
                try {
                    fileType = message!!.replyMsgType
                    userId = message!!.replyUserId
                } catch (e: Exception) {

                }
                /*  try {
                      fileType = messages!!.filter { it.msgId.equals(message.replyMsgId) }.get(0).type
                      userId = messages!!.filter { it.msgId.equals(message.replyMsgId) }.get(0).sentBy
                  } catch (e: Exception) {

                  }*/

                if (fileType.equals("image") || fileType.equals("video")) {
                    imageQuotedOtherMessage.visibility = View.VISIBLE
                    textQuotedMessageOtherMessage.visibility = View.INVISIBLE
                    if (fileType.equals("video")) {
                        imageQuotedVideoOtherMessage.visibility = View.VISIBLE
                    } else {
                        imageQuotedVideoOtherMessage.visibility = View.INVISIBLE
                    }
                    Glide.with(context).load(message.replyMsg).into(imageQuotedOtherMessage)
                } else {
                    imageQuotedOtherMessage.visibility = View.INVISIBLE
                    textQuotedMessageOtherMessage.visibility = View.VISIBLE
                    imageQuotedVideoOtherMessage.visibility = View.INVISIBLE
                    if (fileType.equals("audio")) {
                        textQuotedMessageOtherMessage.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_audio_msg_black,
                            0,
                            0,
                            0
                        )
                        textQuotedMessageOtherMessage.text = " " + message.replyMsg
                    } else {
                        textQuotedMessageOtherMessage.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            0,
                            0
                        )
                        textQuotedMessageOtherMessage.text = message.replyMsg
                    }
                    // textQuotedMessageOtherMessage.text = message.replyMsg
                }
                if (userId.equals(SocketHandler.myUserId)) {
                    titleQuoteTvOtherMessage.text = "You"
                } else {
                    titleQuoteTvOtherMessage.text = message.replyUser
                }

                reply_layout_other_message.setOnClickListener(View.OnClickListener {
                    val navigatePosition =
                        messages!!.mapIndexed { i, b -> if (message.replyMsgId.equals(b.msgId)) i else null }
                            .filterNotNull().toList()
                    if (navigatePosition.isNotEmpty()) {
                        mQuoteClickListener?.onQuoteClick(navigatePosition.get(0))
                    }

                })

            } else {
                reply_layout_other_message.visibility = View.GONE
            }
            messageText.text = message.message
            timeText.text = message.timeMilliSeconds.seconds?.let {
                CommonUtils.fromMillisToTimeString(
                    it.toLong()
                )
            }
        }
    }

    inner class OtherVideoViewHolder(view: View) : MessageViewHolder(view) {
        private var prevVideoOtherIV: ImageView = view.prevVideoOtherIV
        private var txtVideoOtherMessageTime: TextView = view.txtVideoOtherMessageTime
        private var pervUserNameOtherVideo: TextView = view.pervUserNameOtherVideo
        private var centerTimeOtherVideoMessagetxt: TextView = view.centerTimeOtherVideoMessagetxt
        private var dateOtherVideoMessagelayout: RelativeLayout = view.dateOtherVideoMessagelayout
        private var otherVideoMainLayout: RelativeLayout = view.otherVideoMainLayout
        private var VideoOtherChatMainLayout: LinearLayout = view.VideoOtherChatMainLayout
        override fun bind(message: PreviousListModelItem) {
            if (isGroup) {
                pervUserNameOtherVideo.visibility = View.VISIBLE
                try {
                    /*  val userName =
                          groupDetailModel.users.filter { it.userId == message.sentBy }.get(0).name*/
                    pervUserNameOtherVideo.setText(message.senderName)
                } catch (e: Exception) {
                    pervUserNameOtherVideo.setText(context.resources.getString(R.string.str_unknown_person))
                }

            } else {
                pervUserNameOtherVideo.visibility = View.GONE
            }
            val date = CommonUtils.fromMillisToDateString(message.timeMilliSeconds.seconds.toLong())

            if (message.isShowDate) {
                dateOtherVideoMessagelayout.visibility = View.VISIBLE
                centerTimeOtherVideoMessagetxt.text = date
            } else {
                dateOtherVideoMessagelayout.visibility = View.GONE
            }
            if (message.isMultiSelect) {
                otherVideoMainLayout.setBackgroundColor(context.resources.getColor(R.color.list_item_selected_state))
            } else {
                otherVideoMainLayout.setBackgroundColor(context.resources.getColor(R.color.colorTransparent2))
            }
            //   val bytearray = message.base64Thumbnail.split(",")
            //  val videoThumbByteArray: ByteArray = Base64.decode(bytearray.get(1), Base64.DEFAULT)
            //   Glide.with(context).asBitmap().load(videoThumbByteArray).into(prevVideoOtherIV)
            if (!message.thumbnailPath.isNullOrEmpty()) {
                Glide.with(context).asBitmap().load(message.thumbnailPath).into(prevVideoOtherIV)
            }
            txtVideoOtherMessageTime.text = message.timeMilliSeconds.seconds?.let {
                CommonUtils.fromMillisToTimeString(
                    it.toLong()
                )
            }
            VideoOtherChatMainLayout.setOnClickListener(View.OnClickListener {
                if (!message.filePath.isNullOrEmpty() &&
                    !ChatListFragment.isMultiSelect
                ) {
                    val bundle = Bundle()
                    bundle.putString("videoString", message.filePath)
                    bundle.putString("videoType", "otherVideo")
                    LibraryActivity.navController?.navigate(R.id.videoPlayFragment, bundle)
                }

            })
        }
    }

    inner class OtherImageViewHolder(view: View) : MessageViewHolder(view) {
        private var prevImgOtherIV: ImageView = view.prevImgOtherIV
        private var txtImgOtherMessageTime: TextView = view.txtImgOtherMessageTime
        private var pervUserNameOtherIV: TextView = view.pervUserNameOtherIV
        private var centerTimeOtherImgMessagetxt: TextView = view.centerTimeOtherImgMessagetxt
        private var dateOtherImgMessagelayout: RelativeLayout = view.dateOtherImgMessagelayout
        private var otherImageMainLayout: RelativeLayout = view.otherImageMainLayout
        private var OtherMessageImageLayout: LinearLayout = view.OtherMessageImageLayout

        @RequiresApi(Build.VERSION_CODES.M)
        override fun bind(message: PreviousListModelItem) {
            if (isGroup) {
                pervUserNameOtherIV.visibility = View.VISIBLE
                try {
                    /*  val userName =
                          groupDetailModel.users.filter { it.userId == message.sentBy }.get(0).name*/
                    pervUserNameOtherIV.setText(message.senderName)
                } catch (e: Exception) {
                    pervUserNameOtherIV.setText(context.resources.getString(R.string.str_unknown_person))
                }

            } else {
                pervUserNameOtherIV.visibility = View.GONE
            }
            val date = CommonUtils.fromMillisToDateString(message.timeMilliSeconds.seconds.toLong())
            /* if (dateShowArrayList.size == 0 || !dateShowArrayList.contains(date)) {
                 Log.e("getDatefromMessage", ": " + date)
                 dateShowArrayList.add(date)
                 dateOtherImgMessagelayout.visibility = View.VISIBLE
                 centerTimeOtherImgMessagetxt.text = date
             }*/
            if (message.isShowDate) {
                dateOtherImgMessagelayout.visibility = View.VISIBLE
                centerTimeOtherImgMessagetxt.text = date
            } else {
                dateOtherImgMessagelayout.visibility = View.GONE
            }
            Glide.with(context).load(message.filePath).into(prevImgOtherIV)
            if (message.isMultiSelect) {
                otherImageMainLayout.setBackgroundColor(context.resources.getColor(R.color.list_item_selected_state))
            } else {
                otherImageMainLayout.setBackgroundColor(context.resources.getColor(R.color.colorTransparent2))
            }
            txtImgOtherMessageTime.text = message.timeMilliSeconds.seconds?.let {
                CommonUtils.fromMillisToTimeString(
                    it.toLong()
                )
            }
            OtherMessageImageLayout.setOnClickListener(View.OnClickListener {
                if (!ChatListFragment.isMultiSelect) {
                    val dialog = Dialog(context)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setCancelable(false)
                    dialog.setContentView(R.layout.dialogue_image_chat_list)
                    val closeimgshowdialogue =
                        dialog.findViewById(R.id.closeimgshowdialogue) as ImageView
                    val downloadimgshowdialogue =
                        dialog.findViewById(R.id.downloadimgshowdialogue) as ImageView
                    val dialogueshowimg = dialog.findViewById(R.id.dialogueshowimg) as ImageView
                    Glide.with(context).load(message.filePath).into(dialogueshowimg)
                    closeimgshowdialogue.bringToFront()
                    downloadimgshowdialogue.bringToFront()
                    closeimgshowdialogue.setOnClickListener {
                        dialog.dismiss()
                    }
                    downloadimgshowdialogue.setOnClickListener {
                        //  dialog.dismiss()
                        if (message.filePath != "") {
                            if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                val downloadFile =
                                    DownloadFile(
                                        context,
                                        message.filePath,
                                        message.fileName,
                                        "image"
                                    )
                                downloadFile.execute()
                            }

                        }

                    }
                    val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
                    lp.copyFrom(dialog.getWindow()!!.getAttributes())
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT
                    lp.height = WindowManager.LayoutParams.MATCH_PARENT
                    dialog.show()
                    dialog.getWindow()!!.setAttributes(lp)
                }


            })
        }
    }

    inner class OtherDocumentViewHolder(view: View) : MessageViewHolder(view) {
        private var txtOtherFileMessage: TextView = view.txtOtherFileMessage
        private var txtOtherFileMessageTime: TextView = view.txtOtherFileMessageTime
        private var pervUserNameOtherFile: TextView = view.pervUserNameOtherFile
        private var centerTimeOtherFileMessagetxt: TextView = view.centerTimeOtherFileMessagetxt
        private var dateOtherFileMessagelayout: RelativeLayout = view.dateOtherFileMessagelayout
        private var otherFileMainLayout: RelativeLayout = view.otherFileMainLayout
        private var OtherDocumentMainLayout: LinearLayout = view.OtherDocumentMainLayout

        @RequiresApi(Build.VERSION_CODES.M)
        override fun bind(message: PreviousListModelItem) {
            super.bind(message)
            if (isGroup) {
                pervUserNameOtherFile.visibility = View.VISIBLE
                try {
                    /* val userName =
                         groupDetailModel.users.filter { it.userId == message.sentBy }.get(0).name*/
                    pervUserNameOtherFile.setText(message.senderName)
                } catch (e: Exception) {
                    pervUserNameOtherFile.setText(context.resources.getString(R.string.str_unknown_person))
                }

            } else {
                pervUserNameOtherFile.visibility = View.GONE
            }
            val date = CommonUtils.fromMillisToDateString(message.timeMilliSeconds.seconds.toLong())
            /* if (dateShowArrayList.size == 0 || !dateShowArrayList.contains(date)) {
                 dateShowArrayList.add(date)
                 dateOtherFileMessagelayout.visibility = View.VISIBLE
                 centerTimeOtherFileMessagetxt.text = date
             }*/
            if (message.isShowDate) {
                dateOtherFileMessagelayout.visibility = View.VISIBLE
                centerTimeOtherFileMessagetxt.text = date
            } else {
                dateOtherFileMessagelayout.visibility = View.GONE
            }
            if (message.isMultiSelect) {
                otherFileMainLayout.setBackgroundColor(context.resources.getColor(R.color.list_item_selected_state))
            } else {
                otherFileMainLayout.setBackgroundColor(context.resources.getColor(R.color.colorTransparent2))
            }
            val url = URL(message.filePath)
            if (message.fileName != "") {
                txtOtherFileMessage.text = message.fileName
            } else {
                val fileName = FilenameUtils.getName(url.getPath()).split("%2F")
                txtOtherFileMessage.text = "" + fileName.get(1)
            }

            txtOtherFileMessageTime.text = message.timeMilliSeconds.seconds?.let {
                CommonUtils.fromMillisToTimeString(
                    it.toLong()
                )
            }
            OtherDocumentMainLayout.setOnClickListener(View.OnClickListener {
                if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    /* val rootPath =
                         File(
                             Environment.getExternalStorageDirectory()
                                 .toString() + "/Download/" + context.resources.getString(R.string.app_name) + "/"
                         )
                     if (!rootPath.exists()) {
                         rootPath.mkdirs()
                     }
                     val localFile = File(rootPath, fileName.get(1))*/
                    Log.e("getFileInChat", "document: " + message.filePath)
                    if (!ChatListFragment.isMultiSelect) {
                        val browserIntent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(message.filePath))
                        context.startActivity(browserIntent)
                        /* val downloadFile = DownloadFile(context, message.document, message.fileName,"document")
                         downloadFile.execute()*/
                    }

                    /*val downloadmanager =
                        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
                    val uri2 = Uri.parse(message.document)
                    val request = DownloadManager.Request(uri2)
                    request.setTitle(fileName.get(1))
                    request.setDescription("Downloading")
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setVisibleInDownloadsUi(true)
                    request.setDestinationInExternalFilesDir(
                        context,
                        Environment.DIRECTORY_DOCUMENTS, fileName.get(1)
                    )
                    downloadmanager!!.enqueue(request)*/
                }
            })
        }
    }


    inner class OtherAudioViewHolder(view: View) : MessageViewHolder(view) {
        private var txtOtherMp3FileMessage: TextView = view.txtOtherMp3FileMessage
        private var txtOtherMp3FileMessageTime: TextView = view.txtOtherMp3FileMessageTime
        private var pervUserNameOtherMp3FileMessage: TextView = view.pervUserNameOtherMp3FileMessage
        private var centerTimeOtherMp3Messagetxt: TextView = view.centerTimeOtherMp3Messagetxt
        private var dateOtherMp3Messagelayout: RelativeLayout = view.dateOtherMp3Messagelayout
        private var othermp3MainLayoutSelection: RelativeLayout = view.othermp3MainLayoutSelection
        private var OtherMp3FileMainLayout: LinearLayout = view.OtherMp3FileMainLayout
        override fun bind(message: PreviousListModelItem) {
            super.bind(message)
            if (isGroup) {
                pervUserNameOtherMp3FileMessage.visibility = View.VISIBLE
                try {
                    pervUserNameOtherMp3FileMessage.setText(message.senderName)
                } catch (e: Exception) {
                    pervUserNameOtherMp3FileMessage.setText(context.resources.getString(R.string.str_unknown_person))
                }

            } else {
                pervUserNameOtherMp3FileMessage.visibility = View.GONE
            }
            val date = CommonUtils.fromMillisToDateString(message.timeMilliSeconds.seconds.toLong())

            if (message.isShowDate) {
                dateOtherMp3Messagelayout.visibility = View.VISIBLE
                centerTimeOtherMp3Messagetxt.text = date
            } else {
                dateOtherMp3Messagelayout.visibility = View.GONE
            }
            if (message.isMultiSelect) {
                othermp3MainLayoutSelection.setBackgroundColor(context.resources.getColor(R.color.list_item_selected_state))
            } else {
                othermp3MainLayoutSelection.setBackgroundColor(context.resources.getColor(R.color.colorTransparent2))
            }
            txtOtherMp3FileMessage.text = message.fileName
            txtOtherMp3FileMessageTime.text = message.timeMilliSeconds.seconds?.let {
                CommonUtils.fromMillisToTimeString(
                    it.toLong()
                )
            }
            OtherMp3FileMainLayout.setOnClickListener(View.OnClickListener {
                val audioUrl = message.filePath
                /* val mediaPlayer = MediaPlayer()
                 mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)*/
                if (!message.filePath.isNullOrEmpty() &&
                    !ChatListFragment.isMultiSelect) {
                    /* val browserIntent =
                         Intent(Intent.ACTION_VIEW, Uri.parse(message.filePath))
                     context.startActivity(browserIntent)*/
                    openAudioPlayDialogue(message.filePath)

                }

                //First bellow unComment
                /*  if (mediaPlayer.isPlaying) {
                      mediaPlayer.stop()
                      mediaPlayer.reset()
                      mediaPlayer.release()
                      mediaPlayer = MediaPlayer()
                      Toast.makeText(context, "Audio has been paused", Toast.LENGTH_SHORT).show();
                  } else {
                      try {
                          mediaPlayer.setDataSource(audioUrl)
                          mediaPlayer.prepare()
                          mediaPlayer.start()
                      } catch (e: IOException) {
                          e.printStackTrace()
                      }
                      Toast.makeText(context, "Audio started playing..", Toast.LENGTH_SHORT).show()
                  }*/

            })
        }
    }

    private fun openAudioPlayDialogue(filePath: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialogue_audio_chat_list)
        val closeimgshowdialogueaudio =
            dialog.findViewById(R.id.closeimgshowdialogueaudio) as ImageView

        val audioWebView =
            dialog.findViewById(R.id.audioWebView) as WebView
        val webSettings: WebSettings = audioWebView.getSettings()
        webSettings.javaScriptEnabled = true
        closeimgshowdialogueaudio.setOnClickListener {
            audioWebView.destroy()
            dialog.dismiss()
        }
        dialog.setOnDismissListener {
            audioWebView.destroy()
        }
        audioWebView.loadUrl(filePath)
        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(dialog.getWindow()!!.getAttributes())
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.show()
        dialog.getWindow()!!.setAttributes(lp)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestPermission(permission: String): Boolean {
        val isGranted = ContextCompat.checkSelfPermission(
            context!!,
            permission
        ) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            (context as Activity).requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
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
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        permissions[0]
                    )
                )
                    showPermissionDialog(context)
            }
        }
    }

    fun blinkItem(position: Int) {
        blinkItem = position
        notifyItemChanged(position)
    }

    private fun isPositionFooter(position: Int): Boolean {
        /* if(isShowLoading){
             if(position == 0){
                 isShowLoading=false
                 return true
             }else{
                 return false
             }
         }else{
             return false
         }*/
        if (isShowLoading) {
            return position == 0
        } else {
            return false
        }

        /* return if (isShowLoading) {
             position == 0
         } else position == messages!!.size*/
    }
}

fun showPermissionDialog(mContext: Context) {
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

open class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    open fun bind(message: PreviousListModelItem) {}
}