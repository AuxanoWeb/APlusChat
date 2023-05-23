package com.example.auxanochatsdk.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auxanochatsdk.Activity.LibraryActivity
import com.example.auxanochatsdk.Adapter.CreateGroupAdapter
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.Utils.CommonUtils.hideKeyboard
import com.example.auxanochatsdk.Utils.CommonUtils.internetCheck
import com.example.auxanochatsdk.databinding.FragmentCreateGroupUserListBinding
import com.example.auxanochatsdk.model.ContactList
import com.example.auxanochatsdk.model.ContactListModel
import com.example.auxanochatsdk.model.ExitGroupResponseModel
import com.example.auxanochatsdk.network.SocketEvent
import com.example.auxanochatsdk.network.SocketFields
import com.example.auxanochatsdk.network.SocketHandler
import com.example.soketdemo.Model.User
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject


class CreateGroupUserListFragment : Fragment() {

    private val TAG = CreateGroupUserListFragment::class.java.name
    lateinit var createGroupListBinding: FragmentCreateGroupUserListBinding
    lateinit var mContext: Context
    lateinit var mContactList: ArrayList<ContactListModel>
    lateinit var contactAdapter: CreateGroupAdapter
    lateinit var alreadyAddedUserList: ArrayList<String>
    var isAddmember: Boolean = false
    lateinit var exitGroupResponseModel: ExitGroupResponseModel

    companion object {
        lateinit var arrSelectedUser: ArrayList<JSONObject>
        lateinit var arrReadCount: ArrayList<JSONObject>
        lateinit var arrUserIds: ArrayList<String>
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createGroupListBinding =
            FragmentCreateGroupUserListBinding.inflate(layoutInflater, container, false)
                .apply { executePendingBindings() }
        init(container)
        createGroupListBinding.aivBack.setOnClickListener(View.OnClickListener {
            LibraryActivity.navController?.navigate(R.id.action_createGroupUserListFragment_to_UserListFragment)
          //  LibraryActivity.navController?.navigateUp()
        })
        createGroupListBinding.searchbtnCreateGroupUserListScreen.setOnClickListener(View.OnClickListener {
            createGroupListBinding.searchCreateGroupEdt.setText("")
        })
        return createGroupListBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createGroupListBinding.shimmerLayoutCreateGroupList.visibility=View.VISIBLE
        createGroupListBinding.shimmerLayoutCreateGroupList.startShimmer()
        setContactList()
    }

    private fun init(container: ViewGroup?) {
        mContext = container!!.context
        mContactList = ArrayList()
        alreadyAddedUserList = ArrayList()
        createGroupListBinding.nextBtnCreateGroup.setOnClickListener(View.OnClickListener {
            if(internetCheck){
                if (isAddmember) {
                    addMember()
                } else {
                    LibraryActivity.navController?.navigate(R.id.navigateToCreateGroupFragment)
                    createGroupListBinding.searchCreateGroupEdt.hideKeyboard()
                }
            }else{
                CommonUtils.navigateToOffScreen(CommonUtils.internetCheck, mContext!!)
            }


        })
        arrSelectedUser = ArrayList()
        arrReadCount = ArrayList()
        arrUserIds = ArrayList()

        Log.e("checkDataFromList", "arrUserIds: "+arrUserIds.toString() )
        Log.e("checkDataFromList", "arrReadCount: "+arrReadCount.toString() )
        Log.e("checkDataFromList", "arrSelectedUser: "+arrSelectedUser.toString() )
        Log.e("checkDataFromList", "mContactList: "+mContactList.toString() )


        if (arguments != null) {
            if (requireArguments().getString("from").equals("add")) {
                isAddmember = true
                createGroupListBinding.nextBtnCreateGroup.text = getString(R.string.str_add)
                createGroupListBinding.aivCreateGroup.text = getString(R.string.str_add_member)
                alreadyAddedUserList =
                    requireArguments().getParcelableArrayList<Parcelable>("userIdList") as ArrayList<String> /* = java.util.ArrayList<kotlin.String> */
            } else {
                isAddmember = false
                createGroupListBinding.nextBtnCreateGroup.text = getString(R.string.str_next)
                createGroupListBinding.aivCreateGroup.text = getString(R.string.str_create_group)
            }
        }
        CommonUtils.setBackgroundColor(mContext,CommonUtils.selectedTheme,createGroupListBinding.clToolBar)
        //CommanUtils.setButtonBackgroundDrawable(mContext,CommanUtils.selectedTheme,createGroupListBinding.nextBtnCreateGroup)
    }

    private fun addMember() {
        val param = JSONObject()
        val arrUserIdsJson = JSONArray(arrUserIds)
        val arrSelectedUserJson = JSONArray(arrSelectedUser)
        param.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
        param.put(SocketFields.GROUP_ID, requireArguments().getString("groupId"))
        param.put(SocketFields.MEMBERS, arrUserIdsJson)
        param.put(SocketFields.VIEW_BY, arrUserIdsJson)
        param.put(SocketFields.USERS, arrSelectedUserJson)
        SocketHandler.addMember(param)
        SocketHandler.mSocket.on(SocketEvent.ADD_MEMBER_RES.event) {
            val gson = GsonBuilder().create()
            exitGroupResponseModel =
                gson.fromJson(it.get(0).toString(), ExitGroupResponseModel::class.java)
            Log.e("getExitResponse", ": "+exitGroupResponseModel.isSuccess )
            if (exitGroupResponseModel.isSuccess == true) {
                (mContext as Activity).runOnUiThread {
                      val mcontactJson = Gson().toJson(mContactList)
                      val userList= gson.fromJson(arrSelectedUser.toString(), Array<User>::class.java).toList() as ArrayList<User>
                      val bundle = Bundle()
                      bundle.putString("groupname", requireArguments().getString("groupname"))
                      bundle.putString("groupProfiePic", requireArguments().getString("groupProfiePic"))
                      bundle.putBoolean("isGroup", requireArguments().getBoolean("isGroup"))
                      bundle.putString("groupID", requireArguments().getString("groupId"))
                      bundle.putString("groupAdmin", requireArguments().getString("groupAdmin"))
                      bundle.putParcelableArrayList("userGroup", userList as java.util.ArrayList<out Parcelable>)
                   try {
                       createGroupListBinding.searchCreateGroupEdt.hideKeyboard()
                       //  LibraryActivity.navController?.navigate(R.id.action_createGroupUserListFragment_to_UserListFragment)
                       if (CommonUtils.isFromActivity) {
                           val navControllRefresh=(mContext as LibraryActivity).refreshNavigationController()
                           navControllRefresh?.navigate(R.id.action_createGroupUserListFragment_to_userDetailFragment,bundle)
                       }else{
                           LibraryActivity.navController.navigate(R.id.action_createGroupUserListFragment_to_userDetailFragment,bundle)
                       }

                   }catch (e : Exception){

                   }

                }
            }
        }
    }

    private fun setContactList() {
        val obj = JSONObject()
        obj.put(SocketFields.USER_ID, SocketHandler.myUserId)
        obj.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
        SocketHandler.getUserList(obj)
        SocketHandler.mSocket.on(SocketEvent.USER_LIST_RES.event) {
            val gson = GsonBuilder().create()
            mContactList= arrayListOf()
            arrSelectedUser = ArrayList()
            arrReadCount = ArrayList()
            arrUserIds = ArrayList()
            for (i in it.indices) {
                val contactList =
                    gson.fromJson(it.get(i).toString(), ContactList::class.java)
                mContactList.addAll(contactList.list)
            }
            if (requireArguments() != null && requireArguments().getString("from").equals("add")) {
                var count = 0
                val listSize = mContactList.size
                while (count < mContactList.size) {
                    if (alreadyAddedUserList.contains(mContactList.get(count).userId)) {
                        val contactDetailJsonObject = JSONObject()
                        contactDetailJsonObject.put(
                            SocketFields.USER_ID,
                            mContactList.get(count).userId
                        )
                        contactDetailJsonObject.put(
                            SocketFields.SERVER_USER_ID,
                            mContactList.get(count).serverUserId
                        )
                        contactDetailJsonObject.put(
                            SocketFields.PROFILE_PICTURE,
                            mContactList.get(count).profilePicture
                        )
                        contactDetailJsonObject.put(SocketFields.NAME, mContactList.get(count).name)
                        contactDetailJsonObject.put(
                            SocketFields.MOBILE_EMAIL,
                            mContactList.get(count).mobile_email
                        )
                        contactDetailJsonObject.put(
                            SocketFields.GROUPS,
                            JSONArray(mContactList.get(count).groups)
                        )
                        arrSelectedUser.add(contactDetailJsonObject)
                        arrUserIds.add(mContactList.get(count).userId)
                        mContactList.removeAt(count)
                        count--
                    }
                    count++
                }
            } else {
                for (i in mContactList.indices) {
                    if (mContactList.get(i).userId.equals(SocketHandler.myUserId)) {
                        val contactDetailJsonObject = JSONObject()
                        val readCountJsonObject = JSONObject()
                        contactDetailJsonObject.put(
                            SocketFields.USER_ID,
                            mContactList.get(i).userId
                        )
                        contactDetailJsonObject.put(
                            SocketFields.SERVER_USER_ID,
                            mContactList.get(i).serverUserId
                        )
                        contactDetailJsonObject.put(
                            SocketFields.PROFILE_PICTURE,
                            mContactList.get(i).profilePicture
                        )
                        contactDetailJsonObject.put(SocketFields.NAME, mContactList.get(i).name)
                        contactDetailJsonObject.put(
                            SocketFields.MOBILE_EMAIL,
                            mContactList.get(i).mobile_email
                        )
                        contactDetailJsonObject.put(
                            SocketFields.GROUPS,
                            JSONArray(mContactList.get(i).groups)
                        )
                        readCountJsonObject.put(SocketFields.UN_READ_COUNT, 0)
                        readCountJsonObject.put(SocketFields.USER_ID, mContactList.get(i).userId)
                        arrSelectedUser.add(contactDetailJsonObject)
                        arrReadCount.add(readCountJsonObject)
                        arrUserIds.add(mContactList.get(i).userId)
                        mContactList.removeAt(i)
                        break
                    }
                }
            }


            (mContext as Activity).runOnUiThread {
                createGroupListBinding.shimmerLayoutCreateGroupList.visibility=View.GONE
                createGroupListBinding.shimmerLayoutCreateGroupList.stopShimmer()
                contactAdapter =
                    CreateGroupAdapter(mContext, mContactList, createGroupListBinding, isAddmember,alreadyAddedUserList.size)
                val layoutManager =
                    LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
                createGroupListBinding.rvUserListCreateGroup.layoutManager = layoutManager
                createGroupListBinding.rvUserListCreateGroup.adapter = contactAdapter
            }

        }
        createGroupListBinding.searchCreateGroupEdt.addTextChangedListener(object :
            TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                contactSearch(s.toString())
                createGroupListBinding.searchbtnCreateGroupUserListScreen.setImageDrawable(resources.getDrawable(R.drawable.ic_cancel))
                if(s.toString().isEmpty()){
                    createGroupListBinding.searchbtnCreateGroupUserListScreen.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_search_24))
                }
            }
        })
    }
    fun contains(list: ArrayList<ContactListModel>, name: String?): Boolean {
        for (item in list) {
            if (!item.userId.equals(name)) {
                return true
            }
        }
        return false
    }
    private fun contactSearch(searchString: String) {
        val temp: ArrayList<ContactListModel> = ArrayList()
        for (d in mContactList.indices) {
            if (mContactList.get(d).name.toLowerCase().trim()
                    .contains(searchString!!.toLowerCase().trim())
            ) {
                temp.add(mContactList.get(d))
            }
        }
        contactAdapter.updateContactList(temp)
    }

}