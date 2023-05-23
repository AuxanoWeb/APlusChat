package com.example.auxanochatsdk.Adapter

import android.app.Activity
import android.app.ProgressDialog
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
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Utils.CommonUtils.hideKeyboard
import com.example.auxanochatsdk.Utils.CommonUtils.internetCheck
import com.example.auxanochatsdk.Utils.CommonUtils.navigateToOffScreen
import com.example.auxanochatsdk.model.ContactListModel
import com.example.auxanochatsdk.model.ExitGroupResponseModel
import com.example.auxanochatsdk.network.*
import com.example.auxanochatsdk.ui.UserListFragment.Companion.groupListArrayList
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class ContactListAdapter(
    private val context: Context,
    private var mContactList: ArrayList<ContactListModel>,
    private val arrSelectedUserContact: java.util.ArrayList<JSONObject>,
    private val arrReadCountContact: java.util.ArrayList<JSONObject>,
    private val arrUserIdsContact: java.util.ArrayList<String>
) : RecyclerView.Adapter<ContactListAdapter.ViewHolder>() {
    lateinit var exitGroupResponseModel: ExitGroupResponseModel
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact_list, parent, false)

        return ContactListAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactListAdapter.ViewHolder, position: Int) {
        holder.tvContactLabel.text = mContactList.get(position).name
        Glide.with(context)
            .load(mContactList.get(position).profilePicture)
            .placeholder(context.resources.getDrawable(R.drawable.ic_placeholder_profile))
            .into(holder.civUserPicture)

        holder.selectContactMainLayout.setOnClickListener(View.OnClickListener {
            if (internetCheck) {
                var isUserAvailable = false
                for (item in groupListArrayList) {
                    if(item.opponentUserId!!.equals(mContactList[position].userId)){
                        val bundle = Bundle()
                        isUserAvailable = true
                        bundle.putString("groupId", item.groupId)
                        bundle.putString("groupName",item.groupName)
                        bundle.putString("groupProfilePic", item.imagePath)
                        bundle.putBoolean("groupType",item.isGroup)
                        try {
                            LibraryActivity.navController?.navigate(
                                R.id.navigateToChatListFragmentFromContactList,
                                bundle
                            )
                        } catch (e: Exception) {
                        }
                        break
                    }
                }

                if (!isUserAvailable) {
                    //passData(position)
                    arrUserIdsContact.add(mContactList.get(position).userId)
                    createContact(position, holder)
                }
            } else {
                navigateToOffScreen(internetCheck, context)
            }


        })
    }

    private fun createContact(position: Int, holder: ViewHolder) {
        val param = JSONObject()
        val groupDetailJsonObj = JSONObject()
        val arrUserIdsJson = JSONArray(arrUserIdsContact)
        val arrSelectedUserJson = JSONArray(arrSelectedUserContact)
        val arrReadCountJson = JSONArray(arrReadCountContact)
        param.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
        param.put(SocketFields.IS_GROUP, false)
        //  param.put(SocketFields.CREATE_BY, SocketHandler.myUserId)
        param.put(SocketFields.USER_ID, SocketHandler.myUserId)
        // param.put(SocketFields.GROUP_ID, "")
        param.put(SocketFields.GROUP_IMAGE, "")
        param.put(SocketFields.MEMBERS, arrUserIdsJson)
        // param.put(SocketFields.IS_DEACTIVATE_USER, false)
        //   param.put(SocketFields.MODIFIED_BY, "")
        param.put(SocketFields.NAME, "")
        // param.put(SocketFields.ONLINE, JSONArray())
        // param.put(SocketFields.PINNED_GROUP, JSONArray())
        //  param.put(SocketFields.READ_COUNT, arrReadCountJson)
        //  param.put(SocketFields.TYPING, JSONArray())
        //  param.put(SocketFields.BLOCK_USER, JSONArray())
        //  param.put(SocketFields.VIEW_BY, arrUserIdsJson)
        //  param.put(SocketFields.RECENT_MESSAGE, JSONArray())
        //  param.put(SocketFields.USERS, arrSelectedUserJson)
        // groupDetailJsonObj.put(SocketFields.GROUP_DETAILS, param)
        Log.e("checkparams", ": "+param )
        callCreateGroupApi(param,holder)
       /* SocketHandler.crateGroup(param)

        SocketHandler.mSocket.on(SocketEvent.CREATE_GROUP_RES.event) {
            try {
                val gson = GsonBuilder().create()
                exitGroupResponseModel =
                    gson.fromJson(it.get(0).toString(), ExitGroupResponseModel::class.java)
                if (exitGroupResponseModel.isSuccess == true) {
                    (context as Activity).runOnUiThread {
                        LibraryActivity.navController?.navigate(R.id.navigateToUserListFragment)
                        holder.selectContactMainLayout.hideKeyboard()
                    }
                }
            } catch (e: Exception) {
            }

        }*/
    }

    private fun callCreateGroupApi(param: JSONObject, holder: ViewHolder) {
        val retrofitAPI: RetrofitAPI =
            RetrofitInterface.apiService().create(RetrofitAPI::class.java)
        val json: JsonObject = JsonParser().parse(param.toString()).getAsJsonObject()
        val callApi: Call<JsonObject>? = retrofitAPI.createGroup(json)
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Please Wait.!")
        progressDialog.setCancelable(false)
        progressDialog.show()
        callApi!!.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>
            ) {
                progressDialog.cancel()
                if (response.isSuccessful) {
                    (context as Activity).runOnUiThread {
                        LibraryActivity.navController?.navigate(R.id.navigateToUserListFragment)
                        holder.selectContactMainLayout.hideKeyboard()
                    }
                }
                Log.e("crateGroupRes", "onResponse: " + response.toString())
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                progressDialog.cancel()
                Log.e("crateGroupRes", "onError: " + t.message)
            }
        })
    }

    private fun passData(position: Int) {
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
        arrSelectedUserContact.add(contactDetailJsonObject)
        arrReadCountContact.add(readCountJsonObject)
        arrUserIdsContact.add(mContactList.get(position).userId)
    }

    override fun getItemCount(): Int {
        return mContactList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val civUserPicture: ImageView = itemView.findViewById(R.id.civUserPicture)
        val tvContactLabel: TextView = itemView.findViewById(R.id.tvContactLabel)
        val selectContactMainLayout: ConstraintLayout =
            itemView.findViewById(R.id.selectContactMainLayout)
    }

    fun updateContactList(temp: ArrayList<ContactListModel>) {
        mContactList = temp
        notifyDataSetChanged()
    }
}