package com.example.auxanochatsdk.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auxanochatsdk.Activity.LibraryActivity
import com.example.auxanochatsdk.Adapter.ForwardMessageAdapter
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.Utils.ForwardItemSelectInterface
import com.example.auxanochatsdk.databinding.FragmentForwardBinding
import com.example.auxanochatsdk.network.SocketFields
import com.example.auxanochatsdk.network.SocketHandler
import com.example.auxanochatsdk.ui.UserListFragment.Companion.userName
import com.example.soketdemo.Model.GroupListModelItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject


class ForwardFragment : Fragment() {
    lateinit var forwardBinding: FragmentForwardBinding
    lateinit var mContext: Context
    lateinit var forwardMessageAdapter: ForwardMessageAdapter
    lateinit var conversationArrayList: ArrayList<GroupListModelItem>
    lateinit var selectedMessageJsonArray: JSONArray

    companion object {
        lateinit var groupIDArrayList: ArrayList<String>
    }

    /*var fileType = ""
    var fileUrl = ""*/
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        init(container)
        return forwardBinding.root
    }

    private fun init(container: ViewGroup?) {
        mContext = container!!.context
        forwardBinding = FragmentForwardBinding.inflate(layoutInflater, container, false)
            .apply { executePendingBindings() }

        val listString: String = Gson().toJson(
            CommonUtils.getSelectMessageArray(),
            object : TypeToken<ArrayList<CommonUtils.MultiSelectModel?>?>() {}.getType()
        )

        selectedMessageJsonArray = JSONArray(listString)
        Log.e("getSelectMsgList", "jsonElements: " + selectedMessageJsonArray.toString())
        Log.e("getSelectMsgList", "getSelectMessageArray: " + CommonUtils.getSelectMessageArray())
        forwardBinding.aivBackForward.setOnClickListener(View.OnClickListener {
            LibraryActivity.navController?.navigateUp()
        })
        forwardBinding.searchbtnForwardMessageScreen.setOnClickListener(View.OnClickListener {
            forwardBinding.searchForwardMessageEdt.setText("")
        })
        forwardBinding.sendBtnForwardMessage.setOnClickListener(View.OnClickListener {
           // val groupIdJsonArray = Gson().toJsonTree(groupIDArrayList) as JSONArray
            val groupIdJsonArray = JSONArray(groupIDArrayList)
            val forwardFileObj = JSONObject()
            forwardFileObj.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
            forwardFileObj.put(SocketFields.SENTBY, SocketHandler.myUserId)
            forwardFileObj.put(SocketFields.FILE_ARR, selectedMessageJsonArray)
            forwardFileObj.put(SocketFields.GROUP_ARR, groupIdJsonArray)
            forwardFileObj.put(SocketFields.SENDER_NAME, userName)
            SocketHandler.forwardFile(forwardFileObj)
            requireActivity()!!.onBackPressed()
        })
        forwardBinding.searchForwardMessageEdt.addTextChangedListener(object :
            TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                contactSearch(s.toString())
                forwardBinding.searchbtnForwardMessageScreen.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_cancel
                    )
                )
                if (s.toString().isEmpty()) {
                    forwardBinding.searchbtnForwardMessageScreen.setImageDrawable(
                        resources.getDrawable(
                            R.drawable.ic_baseline_search_24
                        )
                    )
                }
            }
        })
        conversationArrayList = ArrayList()
        groupIDArrayList = ArrayList()
        groupIDArrayList.clear()
        conversationArrayList.clear()
        setConversations()
        CommonUtils.setBackgroundColor(mContext,CommonUtils.selectedTheme,forwardBinding.clToolBar)
       // CommanUtils.setButtonBackgroundDrawable(mContext,CommanUtils.selectedTheme,forwardBinding.sendBtnForwardMessage)
    }

    private fun setConversations() {

        conversationArrayList = UserListFragment.groupListArrayList

        forwardMessageAdapter =
            ForwardMessageAdapter(
                mContext,
                conversationArrayList,
                object : ForwardItemSelectInterface {
                    override fun selectItem(position: Int, isChecked: Boolean, groupId: String) {
                        if (isChecked) {
                            if (!groupIDArrayList.contains(groupId)) {
                                groupIDArrayList.add(groupId)
                                Log.e(
                                    "getGroupId",
                                    "add: " + groupIDArrayList.toString()
                                )
                            }

                        } else {
                            if (groupIDArrayList.contains(groupId)) {
                                groupIDArrayList.removeAll { it.equals(groupId) }
                                Log.e(
                                    "getGroupId",
                                    "remove: " + groupIDArrayList.toString()
                                )
                            }

                        }
                        if (groupIDArrayList.size > 0) {
                            forwardBinding.sendBtnForwardMessage.isEnabled = true
                           /* forwardBinding.sendBtnForwardMessage.setBackgroundDrawable(
                                context!!.resources.getDrawable(
                                    R.drawable.custom_button_red
                                )
                            )*/
                            CommonUtils.setButtonBackgroundDrawable(mContext,CommonUtils.selectedTheme,forwardBinding.sendBtnForwardMessage)
                        } else {
                            forwardBinding.sendBtnForwardMessage.isEnabled = false
                            forwardBinding.sendBtnForwardMessage.setBackgroundDrawable(
                                context!!.resources.getDrawable(
                                    R.drawable.custom_button_disable
                                )
                            )
                        }

                    }

                })
        val layoutManager =
            LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        forwardBinding.rvForwardMessage.layoutManager = layoutManager
        forwardBinding.rvForwardMessage.isNestedScrollingEnabled = false
        forwardBinding.rvForwardMessage.setHasFixedSize(true)
        forwardBinding.rvForwardMessage.adapter = forwardMessageAdapter

    }

    private fun contactSearch(searchString: String) {
        val temp: ArrayList<GroupListModelItem> = ArrayList()
        for (d in conversationArrayList) {
            if (d.groupName.toLowerCase().trim().contains(searchString!!.toLowerCase().trim())) {
                temp.add(d)
            }
           /* if (d.isGroup) {
                if (d.name.toLowerCase().trim().contains(searchString!!.toLowerCase().trim())) {
                    temp.add(d)
                }
            } else {
                for (userName in d.users.indices) {
                    if (d.users.get(userName).userId != SocketHandler.myUserId) {
                        if (d.users.get(userName).name.toLowerCase().trim()
                                .contains(searchString!!.toLowerCase().trim())
                        ) {
                            temp.add(d)
                        }
                    }
                }
            }*/

        }
        forwardMessageAdapter.updateConversationList(temp)
    }

    override fun onDestroy() {
        super.onDestroy()
        UserListFragment.groupListArrayList.map { it.isChecked = false }
    }
}