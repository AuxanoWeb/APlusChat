package com.example.auxanochatsdk.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.auxanochatsdk.Activity.LibraryActivity
import com.example.auxanochatsdk.Activity.LibraryActivity.Companion.navController
import com.example.auxanochatsdk.Adapter.GroupListAdapter
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Repository.GroupRepository
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.Utils.CommonUtils.internetCheck
import com.example.auxanochatsdk.Utils.CommonUtils.navigateToOffScreen
import com.example.auxanochatsdk.Utils.CommonUtils.selectedTheme
import com.example.auxanochatsdk.databinding.FragmentUserListBinding
import com.example.auxanochatsdk.model.ReceiveProfileInfoModel
import com.example.auxanochatsdk.model.UserRoleModel
import com.example.auxanochatsdk.network.SocketEvent
import com.example.auxanochatsdk.network.SocketHandler
import com.example.auxanochatsdk.ui.viewModel.GroupListViewModel
import com.example.auxanochatsdk.ui.viewModel.GroupListViewModelFactory
import com.example.soketdemo.Model.GroupListModelItem
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_user_list.*

class UserListFragment : Fragment(), View.OnClickListener {

    private val TAG = "UserListFragment"
    lateinit var userListFragmentUserListBinding: FragmentUserListBinding

    var groupListAdapter: GroupListAdapter? = null
    lateinit var groupListViewModel: GroupListViewModel
    lateinit var receiveProfileInfoModel: ReceiveProfileInfoModel
    lateinit var mContext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach: Called")
    }

    companion object {
        lateinit var groupListArrayList: ArrayList<GroupListModelItem>
        var isFirstTimeCallUserList = true
        var isFirstTimeDialogueOpen = false
        var sendBaseUrl = ""
        var userName: String = ""
        lateinit var userRoleModel: UserRoleModel
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Called")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userListFragmentUserListBinding =
            FragmentUserListBinding.inflate(layoutInflater, container, false)
                .apply { executePendingBindings() }

        //Set Click Listener
        mContext = container!!.context
        initClickListener()
        Log.e("userListFragmentcall", "Call: ")
        Log.d(TAG, "onCreateView: Called")

       /* if(!isFirstTimeDialogueOpen){
            Handler().postDelayed(Runnable {
                openDialogue()
            },200)
        }else{
            callSocketConnection(sendBaseUrl)
        }*/


        /*  userListFragmentUserListBinding =
              FragmentUserListBinding.inflate(layoutInflater, container, false)
                  .apply { executePendingBindings() }*/

        userListFragmentUserListBinding.civUserPictureHome.setOnClickListener(View.OnClickListener {
            if (internetCheck) {
                navController?.navigate(R.id.navigateToProfileFragment)
            } else {
                navigateToOffScreen(internetCheck, mContext)
            }

        })
        userListFragmentUserListBinding.aivCreateGroup.setOnClickListener(View.OnClickListener {
            if (internetCheck) {
                val bundle = Bundle()
                bundle.putString("from", "create")
                navController?.navigate(R.id.navigateToCreateGroupUserListFragment, bundle)
            } else {
                navigateToOffScreen(internetCheck, mContext)
            }

        })
        userListFragmentUserListBinding.aivAddUser.setOnClickListener(View.OnClickListener {
            if (internetCheck) {
                val bundle = Bundle()
                bundle.putString("from", "create")
                navController?.navigate(R.id.navigateToContactListFragment, bundle)
            } else {
                navigateToOffScreen(internetCheck, mContext)
            }

        })
        callSocketConnection()

        return userListFragmentUserListBinding.root
    }

    private fun callSocketConnection(/*sendBaseUrl: String*/) {
        groupListArrayList = ArrayList()
        userListFragmentUserListBinding.shimmerLayoutUserList.visibility = View.VISIBLE
        userListFragmentUserListBinding.shimmerLayoutUserList.startShimmer()

        val activity = mContext as Activity

        // Log.e("getBaseUrl", "secretKey: "+ LibraryActivity.chatSecretKey)

        SocketHandler.setSocket(activity/*,sendBaseUrl*/)
        val groupRepository = GroupRepository(activity)
        groupListViewModel =
            ViewModelProvider(this, GroupListViewModelFactory(groupRepository)).get(
                GroupListViewModel::class.java
            )
        groupListViewModel.groupLiveData.observe(viewLifecycleOwner, Observer {
            groupListArrayList = it as ArrayList<GroupListModelItem>
            //CommanUtils.setGroupArray(groupListArrayList)
            rvChatList.layoutManager =
                LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            rvChatList.isNestedScrollingEnabled = false
            rvChatList.setHasFixedSize(true)

            userListFragmentUserListBinding.shimmerLayoutUserList.visibility = View.GONE
            userListFragmentUserListBinding.shimmerLayoutUserList.stopShimmer()
            isFirstTimeCallUserList = false
            groupListAdapter = GroupListAdapter(mContext, groupListArrayList)
            rvChatList.adapter = groupListAdapter
        })

      //  SocketHandler.getProfileEmit()
       // SocketHandler.onlineUser()
        if (isFirstTimeCallUserList) {
            SocketHandler.getUserRole()
        } else {
            try {
                if (activity != null) {
                    activity.runOnUiThread {
                        if (userRoleModel.createGroup == 1) {
                            userListFragmentUserListBinding.aivCreateGroup.visibility = View.VISIBLE
                        }
                        if (userRoleModel.createOneToOneChat == 1) {
                            userListFragmentUserListBinding.aivAddUser.visibility = View.VISIBLE
                        }
                        if (userRoleModel.updateProfile == 1) {
                            userListFragmentUserListBinding.civUserPictureHome.visibility =
                                View.VISIBLE
                        }
                        if (userRoleModel.createGroup == 0 &&
                            userRoleModel.createOneToOneChat == 0 &&
                            userRoleModel.updateProfile == 0
                        ) {
                          /*  userListFragmentUserListBinding.civBackButtonHome.visibility =
                                View.VISIBLE*/
                        }
                    }
                }
            } catch (e: Exception) {

            }
        }

       // SocketHandler.joinGroup(SocketHandler.myUserId)

        // SocketHandler.totalUnreadCount()
        /*SocketHandler.mSocket.on(SocketEvent.USER_UNREAD_COUNT_RES.event){
            Log.e("getTotalUnraed", ": " + it[0].toString())
        }*/
       /* SocketHandler.mSocket.on(SocketEvent.GET_GROUPS_CALL.event) {
            val jresponse = JSONObject(it[0].toString())
            val isUpdate = jresponse.getString("isUpdate")
            if (isUpdate.equals("true")) {
                val obj = JSONObject()
                try {
                    obj.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
                    obj.put(SocketFields.ID, SocketHandler.myUserId)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                SocketHandler.getGroup(obj)
            }
            Log.e("getGroupsCall", ": " + isUpdate)

        }*/
        SocketHandler.mSocket.on(SocketEvent.PROFILE_RES.event) {
            if (activity != null) {
                activity.runOnUiThread {
                    val gson = GsonBuilder().create()
                    receiveProfileInfoModel =
                        gson.fromJson(it.get(0).toString(), ReceiveProfileInfoModel::class.java)
                    Log.e("getProfilePicure", ": " + receiveProfileInfoModel.profilePicture)
                    if (CommonUtils.isValidContextForGlide(mContext)) {
                        Glide.with(mContext).load(receiveProfileInfoModel.profilePicture)
                            .placeholder(mContext.resources.getDrawable(R.drawable.ic_placeholder_profile))
                            .into(userListFragmentUserListBinding.civUserPictureHome)
                    }

                    userName = receiveProfileInfoModel.name.toString()
                }
            }

        }
        SocketHandler.mSocket.on(SocketEvent.USER_ROLE_RES.event) {
            val gson = GsonBuilder().create()
            userRoleModel =
                gson.fromJson(it.get(0).toString(), UserRoleModel::class.java)
            Log.e("getUserRole", ": " + it.get(0).toString())
            if (activity != null) {
                activity.runOnUiThread {
                    if (userRoleModel.createGroup == 1) {
                        userListFragmentUserListBinding.aivCreateGroup.visibility = View.VISIBLE
                    }
                    if (userRoleModel.createOneToOneChat == 1) {
                        userListFragmentUserListBinding.aivAddUser.visibility = View.VISIBLE
                    }
                    if (userRoleModel.updateProfile == 1) {
                        userListFragmentUserListBinding.civUserPictureHome.visibility = View.VISIBLE
                    }
                    if (userRoleModel.createGroup == 0 &&
                        userRoleModel.createOneToOneChat == 0 &&
                        userRoleModel.updateProfile == 0
                    ) {
                     //   userListFragmentUserListBinding.civBackButtonHome.visibility = View.VISIBLE
                    }
                }
            }


        }
        userListFragmentUserListBinding.searchbtnUserListScreen.setOnClickListener(View.OnClickListener {
            userListFragmentUserListBinding.searchGroupListEdt.setText("")
        })
        userListFragmentUserListBinding.civBackButtonHome.setOnClickListener(View.OnClickListener {
            isFirstTimeCallUserList = true
            SocketHandler.mSocket.off(SocketEvent.PROFILE_RES.event)
            requireActivity()!!.onBackPressed()
        })
        userListFragmentUserListBinding.searchGroupListEdt.addTextChangedListener(object :
            TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                groupSearch(s.toString())
                userListFragmentUserListBinding.searchbtnUserListScreen.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_cancel
                    )
                )
                if (s.toString().isEmpty()) {
                    userListFragmentUserListBinding.searchbtnUserListScreen.setImageDrawable(
                        resources.getDrawable(R.drawable.ic_baseline_search_24)
                    )
                }
            }
        })
    }

    //Set Click Listener
    private fun initClickListener() {
        userListFragmentUserListBinding.civUserPictureHome.setOnClickListener(this)
        userListFragmentUserListBinding.aivCreateGroup.setOnClickListener(this)
        userListFragmentUserListBinding.aivAddUser.setOnClickListener(this)
        userListFragmentUserListBinding.searchbtnUserListScreen.bringToFront()

        CommonUtils.setBackgroundColor(mContext, selectedTheme,userListFragmentUserListBinding.clToolBar)

    }

    private fun openDialogue() {
        val alertDialog = Dialog(mContext, android.R.style.Theme_Black_NoTitleBar)
        val dialogView = LayoutInflater.from(mContext)
            .inflate(R.layout.dialog_with_title_message, null)
        val imgBgBlur: ImageView = dialogView.findViewById(R.id.imgBgBlur)
        val tvSubmitBtnText: TextView = dialogView.findViewById(R.id.tvSubmitBtnText)
        val edtEnterUrl: EditText = dialogView.findViewById(R.id.edtEnterUrl)
        tvSubmitBtnText.setOnClickListener {
            if (edtEnterUrl.text.toString().isNotEmpty()) {
              //  if (URLUtil.isValidUrl(edtEnterUrl.text.toString())) {
                    alertDialog.dismiss()
                    isFirstTimeDialogueOpen = true
                    sendBaseUrl = edtEnterUrl.text.toString()
                    callSocketConnection(/*sendBaseUrl*/)
               /* } else {
                    Toast.makeText(mContext, "Please Enter Valid url...", Toast.LENGTH_SHORT).show()
                }*/

            } else {
                Toast.makeText(mContext, "Please Enter Url...", Toast.LENGTH_SHORT).show()
            }

        }
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setContentView(dialogView)
        alertDialog.window!!.setGravity(Gravity.CENTER)
        alertDialog.setCancelable(false)
        setBlurImage(imgBgBlur, LibraryActivity.libraryActivity!!, null)
        alertDialog.show()
    }

    fun setBlurImage(imgBgBlur: ImageView, activity: LibraryActivity, rootView: View?) {
        val root: View = rootView ?: activity.window.decorView.rootView
        val mBlurBitmap = createBlurBitmap(activity, root)
        imgBgBlur.setImageBitmap(mBlurBitmap)
    }

    private fun createBlurBitmap(activity: LibraryActivity, rootView: View): Bitmap? {
        val bitmap = captureView(rootView)
        blurBitmapWithRenderscript(
            RenderScript.create(activity),
            bitmap
        )
        return bitmap
    }

    private fun captureView(view: View): Bitmap {
        //Create a Bitmap with the same dimensions as the View
        val image = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_4444
        ) //reduce quality
        //Draw the view inside the Bitmap
        val canvas = Canvas(image)
        view.draw(canvas)

        //Make it frosty
        val paint = Paint()
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val filter = LightingColorFilter(-0x1, 0x00222222) // lighten
        //ColorFilter filter =
        //   new LightingColorFilter(0xFF7F7F7F, 0x00000000); // darken
        paint.colorFilter = filter
        canvas.drawBitmap(image, 0F, 0F, paint)
        return image
    }

    fun blurBitmapWithRenderscript(
        rs: RenderScript, bitmap2: Bitmap
    ) {
        val input = Allocation.createFromBitmap(rs, bitmap2)
        val output = Allocation.createTyped(
            rs,
            input.type
        )
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        // must be >0 and <= 25
        script.setRadius(25f)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(bitmap2)
    }

    fun groupSearch(text: String?) {
        val temp: ArrayList<GroupListModelItem> = ArrayList()
        for (d in groupListArrayList) {
            if (d.groupName.toLowerCase().trim().contains(text!!.toLowerCase().trim())) {
                temp.add(d)
            }
            /*if (d.isGroup) {
                if (d.name.toLowerCase().trim().contains(text!!.toLowerCase().trim())) {
                    temp.add(d)
                }
            } else {
                for (userName in d.users.indices) {
                    if (d.users.get(userName).userId != SocketHandler.myUserId) {
                        if (d.users.get(userName).name.toLowerCase().trim()
                                .contains(text!!.toLowerCase().trim())
                        ) {
                            temp.add(d)
                        }
                    }
                }
            }*/

        }
        groupListAdapter?.updateGroupList(temp)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Called")
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.civUserPictureHome -> {
                navController?.navigate(R.id.navigateToProfileFragment)
            }
            R.id.aivCreateGroup -> {
                val bundle = Bundle()
                bundle.putString("from", "create")
                navController?.navigate(R.id.navigateToCreateGroupUserListFragment, bundle)
            }
            R.id.aivAddUser -> {

            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d(TAG, "onViewStateRestored: Called")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: Called")
    }

    override fun onResume() {
        super.onResume()
        Log.e("onResumeCall", "onResume: Called" + isFirstTimeCallUserList)
        try {
            if (!isFirstTimeCallUserList) {
                if (CommonUtils.getGroupArray().isNotEmpty()) {
                    groupListAdapter?.updateGroupList(CommonUtils.getGroupArray())
                } else {
                    groupListAdapter?.updateGroupList(groupListArrayList)
                }

                val activity = mContext as Activity
                isFirstTimeCallUserList = true
                SocketHandler.setSocket(activity/*,sendBaseUrl*/)
              //  SocketHandler.onlineUser()
                SocketHandler.mSocket.on(SocketEvent.GROUP_LIST.event) { args ->
                    Log.e("receiveGroupList", ": " + args)
                    try {
                        for (i in args.indices) {
                            groupListArrayList = ArrayList(
                                Gson().fromJson(
                                    args.get(i).toString(),
                                    Array<GroupListModelItem>::class.java
                                ).toList()
                            )
                            CommonUtils.setGroupArray(groupListArrayList)

                        }
                        activity.runOnUiThread {
                            try {
                                rvChatList.layoutManager = LinearLayoutManager(mContext)
                                userListFragmentUserListBinding.shimmerLayoutUserList.visibility =
                                    View.GONE
                                userListFragmentUserListBinding.shimmerLayoutUserList.stopShimmer()
                                //groupListAdapter?.updateGroupList(groupListArrayList)

                                groupListAdapter = GroupListAdapter(mContext, groupListArrayList)
                                rvChatList.adapter = groupListAdapter
                                rvChatList.setHasFixedSize(true)
                            } catch (e: Exception) {

                            }

                        }
                    } catch (e: Exception) {

                    }


                }

                /* SocketHandler.getProfileEmit()
                 SocketHandler.mSocket.on(SocketEvent.PROFILE_RES.event) {
                     if (activity != null) {
                         activity.runOnUiThread {
                             val gson = GsonBuilder().create()
                             receiveProfileInfoModel =
                                 gson.fromJson(
                                     it.get(0).toString(),
                                     ReceiveProfileInfoModel::class.java
                                 )
                             Glide.with(mContext).load(receiveProfileInfoModel.profilePicture)
                                 .placeholder(mContext.resources.getDrawable(R.drawable.ic_placeholder_profile))
                                 .into(userListFragmentUserListBinding.civUserPictureHome)
                         }
                     }

                 }*/
            }
        } catch (e: Exception) {

        }

    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: Called")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState: Called")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Called")
    }

    override fun onDestroy() {
        super.onDestroy()
        isFirstTimeCallUserList = true
        Log.d(TAG, "onDestroy: Called")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach: Called")
    }
}