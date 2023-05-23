package com.example.auxanochatsdk.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Utils.ForwardItemSelectInterface
import com.example.auxanochatsdk.model.ContactListModel
import com.example.auxanochatsdk.network.SocketHandler
import com.example.auxanochatsdk.ui.ForwardFragment.Companion.groupIDArrayList
import com.example.soketdemo.Model.GroupListModelItem

class ForwardMessageAdapter(
    private val mContext: Context,
    private var conversationArrayList: ArrayList<GroupListModelItem>,
    private var listener: ForwardItemSelectInterface
) : RecyclerView.Adapter<ForwardMessageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ForwardMessageAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_create_group_layout, parent, false)
        return ForwardMessageAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForwardMessageAdapter.ViewHolder, position: Int) {
        setUserProfile(mContext, holder, position)
        setName(holder, position)
        holder.chkCreateGroup.isChecked = conversationArrayList.get(position).isChecked
        holder.chkCreateGroup.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            Log.e("getSizeOfList", ": " + groupIDArrayList.size)
            if (isChecked && (holder.selectUserCreateGroupMainLayout.isPressed || buttonView.isPressed)) {
                if (groupIDArrayList.size > 4) {
                    holder.chkCreateGroup.isChecked = false
                    Toast.makeText(mContext, "Maximum Select 5 Conversations.", Toast.LENGTH_SHORT)
                        .show()
                    return@OnCheckedChangeListener
                }
            }
            conversationArrayList[position].isChecked = isChecked
            listener.selectItem(
                position,
                isChecked,
                conversationArrayList[position].groupId
            )

        })
        holder.selectUserCreateGroupMainLayout.setOnClickListener(View.OnClickListener {
            holder.chkCreateGroup.isChecked = !holder.chkCreateGroup.isChecked
        })
    }

    override fun getItemCount(): Int {
        return conversationArrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val chkCreateGroup: CheckBox = itemView.findViewById(R.id.chkCreateGroup)
        val createGroupUserListProfile: ImageView =
            itemView.findViewById(R.id.createGroupUserListProfile)
        val tvCreateGroupMemberName: TextView = itemView.findViewById(R.id.tvCreateGroupMemberName)
        val selectUserCreateGroupMainLayout: RelativeLayout =
            itemView.findViewById(R.id.selectUserCreateGroupMainLayout)
    }

    private fun setName(holder: ForwardMessageAdapter.ViewHolder, position: Int) {
        holder.tvCreateGroupMemberName.text = conversationArrayList.get(position).groupName
        /*if (conversationArrayList.get(position).isGroup) {
            holder.tvCreateGroupMemberName.text = conversationArrayList.get(position).name
        } else {
            for (i in conversationArrayList.get(position).users.indices) {
                if (conversationArrayList.get(position).users.get(i).userId != SocketHandler.myUserId) {
                    holder.tvCreateGroupMemberName.text =
                        conversationArrayList.get(position).users.get(i).name
                }
            }

        }*/

    }

    private fun setUserProfile(
        context: Context,
        holder: ForwardMessageAdapter.ViewHolder,
        position: Int
    ) {
        Glide.with(context)
            .load(conversationArrayList.get(position).imagePath)
            .placeholder(context.resources.getDrawable(R.drawable.group_place_holder))
            .into(holder.createGroupUserListProfile)
       /* if (conversationArrayList.get(position).isGroup) {
            Glide.with(context)
                .load(conversationArrayList.get(position).groupImage)
                .placeholder(context.resources.getDrawable(R.drawable.group_place_holder))
                .into(holder.createGroupUserListProfile)
        } else {
            for (i in conversationArrayList.get(position).users.indices) {
                if (conversationArrayList.get(position).users.get(i).userId != SocketHandler.myUserId) {
                    Glide.with(context)
                        .load(conversationArrayList.get(position).users.get(i).profilePicture)
                        .placeholder(context.resources.getDrawable(R.drawable.ic_placeholder_profile))
                        .into(holder.createGroupUserListProfile)
                }
            }

        }*/

    }

    fun updateConversationList(list: ArrayList<GroupListModelItem>) {
        conversationArrayList = list
        notifyDataSetChanged()
    }
}