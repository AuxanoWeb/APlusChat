package com.example.auxanochatsdk.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.databinding.FragmentCreateGroupUserListBinding
import com.example.auxanochatsdk.model.ContactListModel
import com.example.auxanochatsdk.network.SocketFields
import com.example.auxanochatsdk.ui.CreateGroupUserListFragment
import org.json.JSONArray
import org.json.JSONObject

class CreateGroupAdapter(
    val context: Context,
    private var mContactList: ArrayList<ContactListModel>,
    val createGroupListBinding2: FragmentCreateGroupUserListBinding,
    private var isAddmember: Boolean,
    val alreadyAddedSize: Int
) : RecyclerView.Adapter<CreateGroupAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CreateGroupAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_create_group_layout, parent, false)

        return CreateGroupAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CreateGroupAdapter.ViewHolder, position: Int) {
        holder.tvCreateGroupMemberName.text = mContactList.get(position).name
        Glide.with(context)
            .load(mContactList.get(position).profilePicture)
            .placeholder(context.resources.getDrawable(R.drawable.ic_placeholder_profile))
            .into(holder.createGroupUserListProfile)
        holder.chkCreateGroup.isChecked = mContactList.get(position).isChecked
        holder.selectUserCreateGroupMainLayout.setOnClickListener(View.OnClickListener {
            holder.chkCreateGroup.isChecked = !holder.chkCreateGroup.isChecked
        })

        holder.chkCreateGroup.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val contactDetailJsonObject = JSONObject()
                val readCountJsonObject = JSONObject()
                contactDetailJsonObject.put(SocketFields.USER_ID, mContactList.get(position).userId)
                contactDetailJsonObject.put(
                    SocketFields.SERVER_USER_ID,
                    mContactList.get(position).serverUserId
                )
                contactDetailJsonObject.put(
                    SocketFields.PROFILE_PICTURE,
                    mContactList.get(position).profilePicture
                )
                contactDetailJsonObject.put(SocketFields.NAME, mContactList.get(position).name)
                contactDetailJsonObject.put(
                    SocketFields.MOBILE_EMAIL,
                    mContactList.get(position).mobile_email
                )
                contactDetailJsonObject.put(
                    SocketFields.GROUPS,
                    JSONArray(mContactList.get(position).groups)
                )
                readCountJsonObject.put(SocketFields.UN_READ_COUNT, 0)
                readCountJsonObject.put(SocketFields.USER_ID, mContactList.get(position).userId)

                if (!CreateGroupUserListFragment.arrUserIds.contains(mContactList.get(position).userId)) {
                    CreateGroupUserListFragment.arrSelectedUser.add(contactDetailJsonObject)
                    CreateGroupUserListFragment.arrUserIds.add(mContactList.get(position).userId)
                    if (!isAddmember) {
                        CreateGroupUserListFragment.arrReadCount.add(readCountJsonObject)
                    }
                }
                createGroupListBinding2.nextBtnCreateGroup.isEnabled = true
                CommonUtils.setButtonBackgroundDrawable(context,
                    CommonUtils.selectedTheme, createGroupListBinding2.nextBtnCreateGroup)
               /* createGroupListBinding2.nextBtnCreateGroup.setBackgroundDrawable(
                    context.resources.getDrawable(
                        R.drawable.custom_button_red
                    )
                )*/
                mContactList.get(position).isChecked = true
            } else {
                for (i in CreateGroupUserListFragment.arrSelectedUser.indices) {
                    if (mContactList.get(position).userId.equals(
                            CreateGroupUserListFragment.arrSelectedUser.get(
                                i
                            ).get(SocketFields.USER_ID).toString()
                        )
                    ) {
                        CreateGroupUserListFragment.arrSelectedUser.removeAt(i)
                        CreateGroupUserListFragment.arrUserIds.removeAt(i)
                        if (!isAddmember) {
                            CreateGroupUserListFragment.arrReadCount.removeAt(i)
                        }
                        break
                    }
                }
                if(isAddmember){
                    if (CreateGroupUserListFragment.arrSelectedUser.size == alreadyAddedSize) {
                        createGroupListBinding2.nextBtnCreateGroup.isEnabled = false
                        createGroupListBinding2.nextBtnCreateGroup.setBackgroundDrawable(
                            context.resources.getDrawable(
                                R.drawable.custom_button_disable
                            )
                        )
                    }
                }else{
                    if (CreateGroupUserListFragment.arrSelectedUser.size == 1) {
                        createGroupListBinding2.nextBtnCreateGroup.isEnabled = false
                        createGroupListBinding2.nextBtnCreateGroup.setBackgroundDrawable(
                            context.resources.getDrawable(
                                R.drawable.custom_button_disable
                            )
                        )
                    }
                }

                mContactList.get(position).isChecked = false
            }
        })

    }

    override fun getItemCount(): Int {
        return mContactList.size
    }

    fun updateContactList(list: ArrayList<ContactListModel>) {
        mContactList = list
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
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
}