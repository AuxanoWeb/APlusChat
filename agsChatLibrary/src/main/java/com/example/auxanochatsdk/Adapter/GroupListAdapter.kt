package com.example.auxanochatsdk.Adapter

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auxanochatsdk.Activity.LibraryActivity
import com.example.auxanochatsdk.Activity.MainFragment
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.Utils.CommonUtils.hideKeyboard
import com.example.auxanochatsdk.Utils.CommonUtils.internetCheck
import com.example.auxanochatsdk.Utils.CommonUtils.navigateToOffScreen
import com.example.auxanochatsdk.Utils.CommonUtils.selectedTheme
import com.example.auxanochatsdk.Utils.TimeAgo
import com.example.auxanochatsdk.network.SocketEvent
import com.example.auxanochatsdk.network.SocketHandler
import com.example.auxanochatsdk.ui.UserListFragment
import com.example.soketdemo.Model.GroupListModelItem
import java.text.SimpleDateFormat
import java.util.*


class GroupListAdapter(
    private val context: Context,
    private var mGroupList: ArrayList<GroupListModelItem>
) : RecyclerView.Adapter<GroupListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_chat, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setUserProfile(context, holder, position)

        setName(context, holder, position)

        setMessageandType(context, holder, position)

        setDateAndTime(context, holder, position)

        setUnreadCount(context, holder, position)

        holder.groupListLayout.setOnClickListener(View.OnClickListener {
            if (internetCheck) {
                holder.tvChatHint.hideKeyboard()
                val bundle = Bundle()
                bundle.putString("groupId", mGroupList.get(position).groupId)
                Log.e("groupId", ": " + mGroupList.get(position).groupId)
                bundle.putString("groupName", mGroupList.get(position).groupName)
                bundle.putString("groupProfilePic", mGroupList.get(position).imagePath)
                /* if (mGroupList.get(position).isGroup) {
                     bundle.putString("groupName", mGroupList.get(position).name)
                     bundle.putString("groupProfilePic", mGroupList.get(position).groupImage)
                 } else {
                     for (i in mGroupList.get(position).users.indices) {
                         if (mGroupList.get(position).users.get(i).userId != SocketHandler.myUserId) {
                             holder.tvChatLabel.text = mGroupList.get(position).users.get(i).name
                             bundle.putString(
                                 "groupName",
                                 mGroupList.get(position).users.get(i).name
                             )
                             bundle.putString(
                                 "groupProfilePic",
                                 mGroupList.get(position).users.get(i).profilePicture
                             )
                         }
                     }
                     // bundle.putString("groupName", mGroupList.get(position).users.get(1).name)
                     // bundle.putString("groupProfilePic", mGroupList.get(position).users.get(1).profilePicture)
                 }*/
                /*  bundle.putParcelableArrayList(
                      "userGroup",
                      mGroupList.get(position).users as ArrayList<out Parcelable>
                  )*/
                bundle.putBoolean("groupType", mGroupList.get(position).isGroup)
                // bundle.putString("groupAdmin", mGroupList.get(position).createdBy)
                if (holder.tvChatCount.visibility == View.VISIBLE) {
                    holder.tvChatCount.visibility = View.GONE
                    if (CommonUtils.getGroupArray().isNotEmpty()) {
                        mGroupList.get(position).unreadCount = 0
                        CommonUtils.setGroupArray(mGroupList)
                        /* for (item in mGroupList.get(position).readCount) {
                             if (item.userId.equals(SocketHandler.myUserId)) {
                                 item.unreadCount = 0
                             }
                             CommanUtils.setGroupArray(mGroupList)
                             break
                         }*/
                    } else {
                        mGroupList.get(position).unreadCount = 0
                        UserListFragment.groupListArrayList = mGroupList
                        /* for (item in mGroupList.get(position).readCount) {
                             if (item.userId.equals(SocketHandler.myUserId)) {
                                 item.unreadCount = 0
                             }
                             UserListFragment.groupListArrayList = mGroupList
                             break
                         }*/
                    }
                }

                SocketHandler.mSocket.off(SocketEvent.GROUP_LIST.event)
                if (CommonUtils.isFromActivity) {
                    val navControllRefresh =
                        (context as LibraryActivity).refreshNavigationController()
                    navControllRefresh?.navigate(R.id.navigateToChatListFragment, bundle)
                } else {
                   // val navControllRefresh = MainFragment().refreshNavigationController()
                    LibraryActivity.navController.navigate(R.id.navigateToChatListFragment, bundle)
                }


            } else {
                navigateToOffScreen(internetCheck, context)
            }

        })

    }

    private fun setName(context: Context, holder: GroupListAdapter.ViewHolder, position: Int) {
        /* if (mGroupList.get(position).isGroup) {
             holder.tvChatLabel.text = mGroupList.get(position).name
         } else {
             for (i in mGroupList.get(position).users.indices) {
                 if (mGroupList.get(position).users.get(i).userId != SocketHandler.myUserId) {
                     holder.tvChatLabel.text = mGroupList.get(position).users.get(i).name
                 }
             }

         }*/
        holder.tvChatLabel.text = mGroupList.get(position).groupName
        CommonUtils.setTextViewTitleStyle(context, CommonUtils.selectedTheme, holder.tvChatLabel)

    }

    private fun setUnreadCount(
        context: Context,
        holder: GroupListAdapter.ViewHolder,
        position: Int
    ) {
        if (mGroupList.get(position).unreadCount.toString().equals("0")) {
            holder.tvChatCount.visibility = View.GONE
            Log.e(
                "getUnreadCount",
                "if: " + mGroupList.get(position).unreadCount.toString()
            )
        } else {
            Log.e(
                "getUnreadCount",
                "inner else: " + mGroupList.get(position).unreadCount.toString()
            )
            holder.tvChatCount.visibility = View.VISIBLE
            holder.tvChatCount.text =
                mGroupList.get(position).unreadCount.toString()
        }
        /* for (i in mGroupList.get(position).readCount.indices) {
             if (SocketHandler.myUserId.equals(mGroupList.get(position).readCount.get(i).userId)) {
                 Log.e(
                     "getUnreadCount",
                     ": " + mGroupList.get(position).readCount.get(i).unreadCount.toString()
                 )
                 if (mGroupList.get(position).readCount.get(i).unreadCount.toString().equals("0")) {
                     holder.tvChatCount.visibility = View.GONE
                     Log.e(
                         "getUnreadCount",
                         "if: " + mGroupList.get(position).readCount.get(i).unreadCount.toString()
                     )
                 } else {
                     Log.e(
                         "getUnreadCount",
                         "inner else: " + mGroupList.get(position).readCount.get(i).unreadCount.toString()
                     )
                     holder.tvChatCount.visibility = View.VISIBLE
                     holder.tvChatCount.text =
                         mGroupList.get(position).readCount.get(i).unreadCount.toString()
                 }
             } else {
                 Log.e("getUnreadCount", "else: ")
             }
         }*/
    }

    private fun setDateAndTime(
        context: Context,
        holder: GroupListAdapter.ViewHolder,
        position: Int
    ) {
        CommonUtils.setTextViewNormalStyle(context, selectedTheme, holder.tvChatDate)
        val sdf = SimpleDateFormat("MM/dd/yy hh:mma")
        val timeAgo = TimeAgo().locale(context).with(sdf)
        if (mGroupList.get(position).latestTime != null && mGroupList.get(position).latestTime!!.seconds != null) {
            val result =
                timeAgo.getTimeAgo(Date(mGroupList.get(position).latestTime!!.seconds?.toLong()!! * 1000))
            holder.tvChatDate.text = result
        } else {
            holder.tvChatDate.text = ""
        }

        /* val currentDate = sdf.format(Date())
         if (currentDate == mGroupList.get(position).recentMessage?.sentAt?.seconds?.let {
                 convertLongToDate(
                     it?.toLong())
             }) {
             holder.tvChatDate.text =
                 mGroupList.get(position).recentMessage?.sentAt?.seconds?.let { convertLongToTime(it?.toLong()) }
         } else {
             if(getYesterdayDate()== mGroupList.get(position).recentMessage?.sentAt?.seconds?.let {
                     convertLongToDate(
                         it?.toLong())
                 }){
                 holder.tvChatDate.text ="Yesterday"
             }else{
                 holder.tvChatDate.text =
                     mGroupList.get(position).recentMessage?.sentAt?.seconds?.let { convertLongToDate(it?.toLong()) }
             }

         }*/
    }

    private fun setMessageandType(
        context: Context,
        holder: GroupListAdapter.ViewHolder,
        position: Int
    ) {
        Log.e("checkType", ": " + mGroupList.get(position).msgType)
        CommonUtils.setTextViewSubTitleStyle(context, selectedTheme, holder.tvChatHint)
        if (mGroupList.get(position).msgType.equals("text")) {
            holder.tvChatHint.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            holder.tvChatHint.text = mGroupList.get(position).recentMsg
        } else if (mGroupList.get(position).msgType.equals("image")) {
            holder.tvChatHint.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_image_msg,
                0,
                0,
                0
            )
            holder.tvChatHint.text = " Image"
        } else if (mGroupList.get(position).msgType.equals("document")) {
            holder.tvChatHint.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_document_msg,
                0,
                0,
                0
            )
            holder.tvChatHint.text = " document"
        } else if (mGroupList.get(position).msgType.equals("audio")) {
            holder.tvChatHint.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_audio_msg,
                0,
                0,
                0
            )
            holder.tvChatHint.text = " audio"
        } else if (mGroupList.get(position).msgType.equals("video")) {
            holder.tvChatHint.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_video_msg,
                0,
                0,
                0
            )
            holder.tvChatHint.text = " video"
        } else {
            holder.tvChatHint.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            holder.tvChatHint.text = " Start your conversation."
        }
    }

    private fun setUserProfile(
        context: Context,
        holder: GroupListAdapter.ViewHolder,
        position: Int
    ) {
        /* if (mGroupList.get(position).isGroup) {
             Glide.with(context)
                 .load(mGroupList.get(position).groupImage)
                 .placeholder(context.resources.getDrawable(R.drawable.group_place_holder))
                 .into(holder.civUserPicture)
         } else {
             for (i in mGroupList.get(position).users.indices) {
                 Log.e(
                     "getchatListProfile",
                     ": " + mGroupList.get(position).users.get(i).profilePicture
                 )
                 if (mGroupList.get(position).users.get(i).userId != SocketHandler.myUserId) {
                     Glide.with(context)
                         .load(mGroupList.get(position).users.get(i).profilePicture)
                         .placeholder(context.resources.getDrawable(R.drawable.ic_placeholder_profile))
                         .into(holder.civUserPicture)
                 }
             }

         }*/
        Glide.with(context)
            .load(mGroupList.get(position).imagePath)
            .placeholder(context.resources.getDrawable(R.drawable.group_place_holder))
            .into(holder.civUserPicture)

    }

    fun convertLongToDate(time: Long): String {
        val time = Date(time * 1000)
        val formatter = SimpleDateFormat("MM/dd/yy", Locale.US)
        val date: String = formatter.format(time).toString()
        return date + ""

    }

    fun convertLongToTime(time: Long): String {
        val time = Date(time * 1000)
        val formatter = SimpleDateFormat("hh:mm a", Locale.US)
        val date: String = formatter.format(time).toString()
        return date + ""
    }


    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mGroupList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val civUserPicture: ImageView = itemView.findViewById(R.id.civUserPicture)
        val tvChatLabel: TextView = itemView.findViewById(R.id.tvChatLabel)
        val tvChatHint: TextView = itemView.findViewById(R.id.tvChatHint)
        val tvChatDate: TextView = itemView.findViewById(R.id.tvChatDate)
        val tvChatCount: TextView = itemView.findViewById(R.id.tvChatCount)
        val groupListLayout: ConstraintLayout = itemView.findViewById(R.id.groupListLayout)
    }

    fun updateGroupList(list: ArrayList<GroupListModelItem>) {
        mGroupList = list
        notifyDataSetChanged()
    }
}