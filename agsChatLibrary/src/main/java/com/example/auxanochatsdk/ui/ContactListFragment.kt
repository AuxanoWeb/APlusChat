package com.example.auxanochatsdk.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auxanochatsdk.Activity.LibraryActivity
import com.example.auxanochatsdk.Adapter.ContactListAdapter
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.databinding.FragmentContactListBinding
import com.example.auxanochatsdk.model.ContactList
import com.example.auxanochatsdk.model.ContactListModel
import com.example.auxanochatsdk.network.SocketEvent
import com.example.auxanochatsdk.network.SocketFields
import com.example.auxanochatsdk.network.SocketHandler
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

class ContactListFragment : Fragment() {
    private val TAG = ContactListFragment::class.java.name
    lateinit var contactListBinding: FragmentContactListBinding
    lateinit var mContext: Context
    lateinit var mContactList: ArrayList<ContactListModel>
    lateinit var contactList: ContactList
    lateinit var contactAdapter: ContactListAdapter

    lateinit var arrSelectedUserContact: ArrayList<JSONObject>
    lateinit var arrReadCountContact: ArrayList<JSONObject>
    lateinit var arrUserIdsContact: ArrayList<String>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        init(container)
        setContactList()
        return contactListBinding.root
    }

    private fun setContactList() {
        contactListBinding.shimmerLayoutContactList.visibility = View.VISIBLE
        contactListBinding.shimmerLayoutContactList.startShimmer()
        val obj = JSONObject()
        obj.put(SocketFields.USER_ID, SocketHandler.myUserId)
        obj.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
        SocketHandler.getUserList(obj)
        SocketHandler.mSocket.on(SocketEvent.USER_LIST_RES.event) {
            val gson = GsonBuilder().create()
            for (i in it.indices) {
                contactList =
                    gson.fromJson(it.get(i).toString(), ContactList::class.java)
                mContactList.addAll(contactList!!.list)
            }
            for (i in mContactList.indices) {
                if (mContactList.get(i).userId.equals(SocketHandler.myUserId)) {
                    val contactDetailJsonObject = JSONObject()
                    val readCountJsonObject = JSONObject()
                    contactDetailJsonObject.put(SocketFields.USER_ID, mContactList.get(i).userId)
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
                    arrSelectedUserContact.add(contactDetailJsonObject)
                    arrReadCountContact.add(readCountJsonObject)
                    arrUserIdsContact.add(mContactList.get(i).userId)
                    mContactList.removeAt(i)
                    break
                }
            }
            (mContext as Activity).runOnUiThread {
                contactListBinding.shimmerLayoutContactList.visibility = View.GONE
                contactListBinding.shimmerLayoutContactList.stopShimmer()
                contactAdapter = ContactListAdapter(
                    mContext,
                    mContactList,
                    arrSelectedUserContact,
                    arrReadCountContact,
                    arrUserIdsContact
                )
                contactListBinding.rvContactList.layoutManager = LinearLayoutManager(mContext)
                contactListBinding.rvContactList.adapter = contactAdapter
            }

        }
        contactListBinding.searchContactListEdt.addTextChangedListener(object :
            TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                contactSearch(s.toString())
                contactListBinding.searchbtnContactListScreen.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_cancel
                    )
                )
                if (s.toString().isEmpty()) {
                    contactListBinding.searchbtnContactListScreen.setImageDrawable(
                        resources.getDrawable(
                            R.drawable.ic_baseline_search_24
                        )
                    )
                }
            }
        })
    }

    private fun init(container: ViewGroup?) {
        mContext = container!!.context
        contactListBinding = FragmentContactListBinding.inflate(layoutInflater, container, false)
            .apply { executePendingBindings() }
        contactListBinding.aivBackContactList.setOnClickListener(View.OnClickListener {
            LibraryActivity.navController?.navigate(R.id.navigateToUserListFragment)
            //  LibraryActivity.navController?.navigateUp()
        })
        contactListBinding.searchbtnContactListScreen.setOnClickListener(View.OnClickListener {
            contactListBinding.searchContactListEdt.setText("")
        })
        mContactList = ArrayList()
        mContactList.clear()
        arrSelectedUserContact = ArrayList()
        arrSelectedUserContact.clear()
        arrReadCountContact = ArrayList()
        arrReadCountContact.clear()
        arrUserIdsContact = ArrayList()
        arrUserIdsContact.clear()
        CommonUtils.setBackgroundColor(mContext, CommonUtils.selectedTheme,contactListBinding.clToolBar)


    }

    private fun contactSearch(searchText: String) {
        val temp: ArrayList<ContactListModel> = ArrayList()
        for (d in mContactList.indices) {
            if (mContactList.get(d).name.toLowerCase().trim()
                    .contains(searchText!!.toLowerCase().trim())
            ) {
                temp.add(mContactList.get(d))
            }
        }
        contactAdapter.updateContactList(temp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SocketHandler.mSocket.off(SocketEvent.USER_LIST_RES.event)
    }
}