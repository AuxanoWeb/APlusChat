package com.example.auxanochatsdk.Adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.Utils.CommonUtils.internetCheck
import com.example.auxanochatsdk.databinding.FragmentUserDetailBinding
import com.example.auxanochatsdk.model.ExitGroupResponseModel
import com.example.auxanochatsdk.network.SocketEvent
import com.example.auxanochatsdk.network.SocketFields
import com.example.auxanochatsdk.network.SocketHandler
import com.example.auxanochatsdk.ui.UserDetailFragment
import com.example.soketdemo.Model.GroupListModelItem
import com.example.soketdemo.Model.User
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList

class GroupUserDetailAdapter(
    private val context: Context,
    private var mGroupList: ArrayList<User>,
    private val groupAdmin: String,
    private val userDetailBinding: FragmentUserDetailBinding,
    private val groupId: String,
    private val groupDetailModel: GroupListModelItem
) : RecyclerView.Adapter<GroupUserDetailAdapter.ViewHolder>() {
    lateinit var exitGroupResponseModel: ExitGroupResponseModel
    var removeUserID = ""
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupUserDetailAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_user_detail, parent, false)

        return GroupUserDetailAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupUserDetailAdapter.ViewHolder, position: Int) {
        Glide.with(context).load(mGroupList.get(position).profilePicture)
            .placeholder(context.resources.getDrawable(R.drawable.ic_placeholder_profile))
            .into(holder.groupUserListProfile)
        if (mGroupList.get(position).userId.equals(SocketHandler.myUserId)) {
            holder.tvGroupMemberName.text = mGroupList.get(position).name + " (You)"
        } else {
            holder.tvGroupMemberName.text = mGroupList.get(position).name
        }
        if (groupAdmin.equals(mGroupList.get(position).userId)) {
            holder.tvAdminLable.visibility = View.VISIBLE
        } else {
            holder.tvAdminLable.visibility = View.GONE
        }
        Log.e(
            "getRemoveMember",
            ": " + mGroupList.get(position).name
        )
        if (groupDetailModel.groupPermission[0].permission.removeMember == 1 &&
            !SocketHandler.myUserId.equals(mGroupList.get(position).userId)
            && !groupAdmin.equals(mGroupList.get(position).userId)
        ) {
            holder.tvRemoveLable.visibility = View.VISIBLE
        } else {
            holder.tvRemoveLable.visibility = View.GONE
        }
        /* if(UserDetailFragment.isGroupAdminDetail){
             if(!groupAdmin.equals(mGroupList.get(position).userId)){
                 holder.tvRemoveLable.visibility=View.VISIBLE
             }else{
                 holder.tvRemoveLable.visibility=View.GONE
             }

         }*/
        holder.tvRemoveLable.setOnClickListener(View.OnClickListener {
            if (internetCheck) {
                val dialogBuilder = AlertDialog.Builder(context)

                dialogBuilder.setMessage("Are you sure you want to remove this member?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                        for (i in mGroupList.indices) {
                            Log.e("savedUserName", ": " + mGroupList.get(i).name)
                            if (!mGroupList.get(position).userId.equals(mGroupList.get(i).userId)) {
                                getContactDetail(i, position)
                            } else {
                                removeUserID = mGroupList.get(position).userId
                            }
                        }
                        removeMember()
                    })
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Alert")
                alert.show()

            } else {
                CommonUtils.navigateToOffScreen(internetCheck, context)
            }

        })

    }

    private fun getContactDetail(i: Int, position: Int) {
        val contactDetailJsonObject = JSONObject()
        contactDetailJsonObject.put(SocketFields.USER_ID, mGroupList.get(i).userId)
        contactDetailJsonObject.put(
            SocketFields.SERVER_USER_ID,
            mGroupList.get(i).serverUserId
        )
        contactDetailJsonObject.put(
            SocketFields.PROFILE_PICTURE,
            mGroupList.get(i).profilePicture
        )
        contactDetailJsonObject.put(SocketFields.NAME, mGroupList.get(i).name)
        contactDetailJsonObject.put(
            SocketFields.MOBILE_EMAIL,
            mGroupList.get(i).mobile_email
        )
        contactDetailJsonObject.put(
            SocketFields.GROUPS,
            JSONArray(mGroupList.get(i).groups)
        )

        UserDetailFragment.arrSelectedUserRemove.add(contactDetailJsonObject)
        UserDetailFragment.arrUserIdsRemove.add(mGroupList.get(i).userId)

    }

    private fun removeMember() {
        val param = JSONObject()
        val arrUserIdsJson = JSONArray(UserDetailFragment.arrUserIdsRemove)
        val arrSelectedUserJson = JSONArray(UserDetailFragment.arrSelectedUserRemove)
        param.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
        param.put(SocketFields.GROUP_ID, groupId)
        param.put(SocketFields.MEMBERS, arrUserIdsJson)
        param.put(SocketFields.VIEW_BY, arrUserIdsJson)
        param.put(SocketFields.USERS, arrSelectedUserJson)
        SocketHandler.removeMember(param)
        SocketHandler.mSocket.on(SocketEvent.REMOVE_MEMBER_RES.event) {
            val gson = GsonBuilder().create()
            exitGroupResponseModel =
                gson.fromJson(it.get(0).toString(), ExitGroupResponseModel::class.java)

            if (exitGroupResponseModel.isSuccess == true) {
                (context as Activity).runOnUiThread {
                    for (i in mGroupList.indices) {
                        if (mGroupList.get(i).userId.equals(removeUserID)) {
                            Log.e("removeMember", "group: " + mGroupList.get(i).name)
                            mGroupList.removeAt(i)
                            break
                        }
                    }
                    for (i in UserDetailFragment.userModel.indices) {
                        if (UserDetailFragment.userModel.get(i).userId.equals(removeUserID)) {
                            Log.e("removeMember", "model: " + i)
                            UserDetailFragment.userModel.removeAt(i)
                            break
                        }
                    }
                    UserDetailFragment.arrSelectedUserRemove = ArrayList()
                    UserDetailFragment.arrUserIdsRemove = ArrayList()
                    userDetailBinding.tvCountParticipant.text =
                        UserDetailFragment.userModel.size.toString() + " participants"
                    userDetailBinding.tvMailIdUserDetail.text =
                        "Group . " + UserDetailFragment.userModel.size.toString() + " participants"
                    notifyDataSetChanged()

                }

            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        Log.e("getUserCount", "size: " + mGroupList.size)
        return mGroupList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val groupUserListProfile: ImageView = itemView.findViewById(R.id.groupUserListProfile)
        val tvGroupMemberName: TextView = itemView.findViewById(R.id.tvGroupMemberName)
        val tvAdminLable: TextView = itemView.findViewById(R.id.tvAdminLable)
        val tvRemoveLable: TextView = itemView.findViewById(R.id.tvRemoveLable)
    }
}