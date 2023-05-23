package com.example.auxanochatsdk.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.auxanochatsdk.Activity.LibraryActivity
import com.example.auxanochatsdk.Adapter.GroupUserDetailAdapter
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.Utils.CommonUtils.internetCheck
import com.example.auxanochatsdk.Utils.PathUtil
import com.example.auxanochatsdk.databinding.FragmentUserDetailBinding
import com.example.auxanochatsdk.model.ExitGroupResponseModel
import com.example.auxanochatsdk.model.FileUploadResponse
import com.example.auxanochatsdk.network.*
import com.example.soketdemo.Model.GroupListModelItem
import com.example.soketdemo.Model.User
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UserDetailFragment : Fragment() {
    lateinit var userDetailBinding: FragmentUserDetailBinding
    lateinit var groupName: String
    lateinit var groupProfilePic: String
    lateinit var groupId: String
    lateinit var groupAdmin: String
    var isGroup: Boolean = false
    lateinit var mContext: Context
    lateinit var exitGroupResponseModel: ExitGroupResponseModel
    lateinit var activity: Activity
    var userGroup: ArrayList<Parcelable>? = null
    var contentType = ""
    var fileName = ""

    //var profilepicByteArray: ByteArray? = null
    lateinit var userIdList: ArrayList<String>
    val SELECT_PICTURE = 2001
    lateinit var groupDetailModel: GroupListModelItem
    var selectedFile: File? = null
    lateinit var progressDialog: ProgressDialog

    companion object {
        var isGroupAdminDetail: Boolean = false
        lateinit var userModel: ArrayList<User>
        lateinit var arrSelectedUserRemove: java.util.ArrayList<JSONObject>
        lateinit var arrUserIdsRemove: java.util.ArrayList<String>
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userDetailBinding = FragmentUserDetailBinding.inflate(layoutInflater, container, false)
            .apply { executePendingBindings() }
        init(container)
        exitGroup()
        updateProfile()
        return userDetailBinding.root
    }

    private fun updateProfile() {
        userDetailBinding.camerabtn.setOnClickListener(View.OnClickListener {
            if (requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(
                        intent,
                        "Select Picture"
                    ), SELECT_PICTURE
                )
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                selectedFile = File(Uri.parse(PathUtil.getPath(mContext, data!!.data)).path)
                val filePath: String = selectedFile!!.getPath()
                val bitmap = BitmapFactory.decodeFile(filePath)
                (mContext as Activity).runOnUiThread {
                    Glide.with(mContext).asBitmap().load(bitmap)
                        .placeholder(mContext.resources.getDrawable(R.drawable.ic_placeholder_profile))
                        .into(userDetailBinding.profilePictureUserDetailIV)
                    //profilepicByteArray = CommanUtils.getBytesFromBitmap(bitmap)
                    try {
                        contentType =
                            getMimeType(Uri.parse(PathUtil.getPath(mContext, data!!.data)).path)!!
                        fileName = selectedFile!!.name
                    } catch (e: Exception) {
                        contentType = "image/jpeg"
                        fileName = "img_" + System.currentTimeMillis()
                    }
                }

            }
        }
    }

    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    fun requestPermission(permission: String): Boolean {
        val isGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 101
            )
        }
        return isGranted
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(
                        intent,
                        "Select Picture"
                    ), SELECT_PICTURE
                )
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        permissions[0]
                    )
                )
                    showPermissionDialog(mContext)
            }
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

    private fun exitGroup() {
        userDetailBinding.exitGroupLayout.setOnClickListener(View.OnClickListener {
            if (internetCheck) {
                val dialogBuilder = AlertDialog.Builder(mContext)
                dialogBuilder.setMessage("Are you sure you want to Exit Group?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                        val obj = JSONObject()
                        obj.put(SocketFields.GROUP_ID, groupId)
                        obj.put(SocketFields.USER_ID, SocketHandler.myUserId)
                        obj.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
                        SocketHandler.exitGroup(obj)
                        SocketHandler.mSocket.on(SocketEvent.EXIT_GROUP_RES.event) {
                            Log.e("getExitRes", ": " + it.get(0).toString())
                            val gson = GsonBuilder().create()
                            exitGroupResponseModel =
                                gson.fromJson(
                                    it.get(0).toString(),
                                    ExitGroupResponseModel::class.java
                                )
                            if (exitGroupResponseModel.isSuccess!!) {
                                SocketHandler.leaveGroup(groupId)
                                activity.runOnUiThread {
                                    LibraryActivity.navController?.navigate(R.id.action_userDetailFragment_to_chatListFragment)
                                }

                            }
                        }
                    })
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Alert")
                alert.show()
            } else {
                CommonUtils.navigateToOffScreen(CommonUtils.internetCheck, mContext!!)
            }


        })
    }

    private fun init(container: ViewGroup?) {
        mContext = container!!.context
        activity = mContext as Activity
        isGroupAdminDetail = false
        userIdList = ArrayList()
        arrSelectedUserRemove = ArrayList()
        arrUserIdsRemove = ArrayList()
        userModel = ArrayList()
        groupName = requireArguments().getString("groupname").toString()
        groupProfilePic = requireArguments().getString("groupProfiePic").toString()
        groupId = requireArguments().getString("groupID").toString()
        Log.e("getGroupId", ": " + groupId)
        //   groupAdmin = requireArguments().getString("groupAdmin").toString()
        isGroup = requireArguments().getBoolean("isGroup")
        val obj = JSONObject()
        obj.put(SocketFields.GROUP_ID, groupId)
        obj.put(SocketFields.USER_ID, SocketHandler.myUserId)
        obj.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
        SocketHandler.getGroupSecond(obj)
        SocketHandler.mSocket.on(SocketEvent.GET_GROUP_RES.event) {
            val gson = GsonBuilder().create()
            groupDetailModel =
                gson.fromJson(it.get(0).toString(), GroupListModelItem::class.java)
            groupAdmin = groupDetailModel.createdBy
            userModel =
                groupDetailModel.users as ArrayList<User> /* = java.util.ArrayList<com.example.soketdemo.Model.User> */
            setData()
        }
        // userGroup = requireArguments().getParcelableArrayList<Parcelable>("userGroup")
        /*  userModel =
              userGroup as ArrayList<User>*/
        progressDialog = ProgressDialog(mContext)
        progressDialog.setMessage("Please wait.!")
        progressDialog.setCancelable(false)

        userDetailBinding.aivBackUserDetail.setOnClickListener(View.OnClickListener {
          //  requireActivity()!!.onBackPressed()
            LibraryActivity.navController?.navigateUp()

        })
        CommonUtils.setBackgroundColor(mContext,CommonUtils.selectedTheme,userDetailBinding.clToolBar)
        CommonUtils.setButtonBackgroundDrawable(mContext,CommonUtils.selectedTheme,userDetailBinding.updatebtn)
    }

    private fun setData() {
        activity?.runOnUiThread {
            val layoutManager =
                LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            userDetailBinding.rvUserDetailContactInfo.layoutManager = layoutManager
            userDetailBinding.rvUserDetailContactInfo.setHasFixedSize(true)
            userDetailBinding.rvUserDetailContactInfo.isNestedScrollingEnabled = false

            val groupUserDetailAdapter =
                GroupUserDetailAdapter(
                    mContext,
                    userModel,
                    groupAdmin,
                    userDetailBinding,
                    groupId,
                    groupDetailModel
                )
            userDetailBinding.rvUserDetailContactInfo.adapter = groupUserDetailAdapter

            userDetailBinding.tvCountParticipant.text = userModel.size.toString() + " participants"
            if (isGroup) {
                Glide.with(mContext).load(groupProfilePic)
                    .placeholder(mContext.resources.getDrawable(R.drawable.group_place_holder))
                    .into(userDetailBinding.profilePictureUserDetailIV)
                if (groupDetailModel.groupPermission[0].permission.addMember == 1) {
                    userDetailBinding.tvAddMember.visibility = View.VISIBLE
                }
                if (groupDetailModel.groupPermission[0].permission.addProfilePicture == 1) {
                    userDetailBinding.camerabtn.visibility = View.VISIBLE
                    userDetailBinding.updatebtn.visibility = View.VISIBLE
                }
                if (groupDetailModel.groupPermission[0].permission.changeGroupName == 1) {
                    userDetailBinding.tvUserNameDetail.isEnabled = true
                }
                if (groupDetailModel.groupPermission[0].permission.deleteChat == 1) {
                    userDetailBinding.deleteChatLayout.visibility = View.VISIBLE
                    userDetailBinding.tvRemoveGroupUD.text = "Delete Group"
                }
                if (groupDetailModel.groupPermission[0].permission.exitGroup == 1) {
                    userDetailBinding.exitGroupLayout.visibility = View.VISIBLE
                }
                // userDetailBinding.exitGroupLayout.visibility = View.VISIBLE
                // userDetailBinding.deleteChatLayout.visibility = View.GONE
                userDetailBinding.groupDetailLayout.visibility = View.VISIBLE
                userDetailBinding.tvUserNameDetail.setText(groupName)
                // userDetailBinding.tvUserNameDetail.isEnabled=false
                userDetailBinding.tvMailIdUserDetail.text =
                    "Group . " + userModel.size + " participants"
                if (groupAdmin.equals(SocketHandler.myUserId)) {
                    isGroupAdminDetail = true
                    // userDetailBinding.deleteChatLayout.visibility = View.VISIBLE
                    // userDetailBinding.tvAddMember.visibility = View.VISIBLE
                    //  userDetailBinding.camerabtn.visibility = View.VISIBLE
                    //  userDetailBinding.updatebtn.visibility = View.VISIBLE
                    //  userDetailBinding.tvRemoveGroupUD.text = "Delete Group"
                    //   userDetailBinding.tvUserNameDetail.isEnabled=true
                    userDetailBinding.tvAddMember.setOnClickListener {
                        if (internetCheck) {
                            for (i in userModel.indices) {
                                userIdList.add(userModel.get(i).userId)
                            }
                            val bundle = Bundle()
                            bundle.putString("from", "add")
                            bundle.putString("groupId", groupId)
                            bundle.putString("groupname", groupName)
                            bundle.putString("groupProfiePic", groupProfilePic)
                            bundle.putBoolean("isGroup", isGroup)
                            bundle.putString("groupAdmin", groupAdmin)
                            bundle.putParcelableArrayList(
                                "userIdList",
                                userIdList as java.util.ArrayList<out Parcelable>
                            )
                            LibraryActivity.navController?.navigate(
                                R.id.action_userDetailFragment_to_createGroupUserListFragment,
                                bundle
                            )
                        } else {
                            CommonUtils.navigateToOffScreen(internetCheck, mContext!!)
                        }

                    }
                }
                userDetailBinding.deleteChatLayout.setOnClickListener(View.OnClickListener {
                    if (internetCheck) {
                        if (userDetailBinding.tvRemoveGroupUD.text.equals(resources.getString(R.string.delete_chat))) {
                            deleteChat()
                        } else if (userDetailBinding.tvRemoveGroupUD.text.equals("Delete Group")) {
                            deleteGroup()
                        }
                    } else {
                        CommonUtils.navigateToOffScreen(internetCheck, mContext!!)
                    }

                })
                userDetailBinding.updatebtn.setOnClickListener(View.OnClickListener {
                    if (internetCheck) {
                        if (userDetailBinding.tvUserNameDetail.text.toString().isNotEmpty()) {
                            val updateJason = JSONObject()
                            updateJason.put(SocketFields.GROUP_ID, groupId)
                            updateJason.put(
                                SocketFields.NAME,
                                userDetailBinding.tvUserNameDetail.text.toString()
                            )
                            updateJason.put(SocketFields.CONTENT_TYPE, contentType)
                            updateJason.put(SocketFields.FILE_NAME, fileName)
                            updateJason.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
                            if (selectedFile != null) {
                                //   updateJason.put(SocketFields.GROUP_IMAGE, profilepicByteArray)
                                callSendImageApi("image", selectedFile!!, updateJason)
                            } else {
                                updateJason.put(SocketFields.GROUP_IMAGE, groupProfilePic)
                                SocketHandler.updateGroup(updateJason)
                            }
                            //    updateJason.put(SocketFields.GROUP_IMAGE,profilepicByteArray)


                            SocketHandler.mSocket.on(SocketEvent.UPDATE_GROUP_RES.event) {
                                val gson = GsonBuilder().create()
                                exitGroupResponseModel =
                                    gson.fromJson(
                                        it.get(0).toString(),
                                        ExitGroupResponseModel::class.java
                                    )
                                if (progressDialog.isShowing) {
                                    progressDialog.dismiss()
                                }
                                if (exitGroupResponseModel.isSuccess!!) {
                                    SocketHandler.leaveGroup(groupId)

                                    activity.runOnUiThread {
                                        LibraryActivity.navController?.navigate(R.id.action_userDetailFragment_to_UserListFragment)
                                    }

                                }
                            }

                        } else {
                            Toast.makeText(mContext, "Enter Group Name", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        CommonUtils.navigateToOffScreen(internetCheck, mContext!!)
                    }


                })
            } else {
                Glide.with(mContext).load(groupProfilePic)
                    .placeholder(mContext.resources.getDrawable(R.drawable.ic_placeholder_profile))
                    .into(userDetailBinding.profilePictureUserDetailIV)
                userDetailBinding.exitGroupLayout.visibility = View.GONE
                userDetailBinding.groupDetailLayout.visibility = View.GONE

                if (UserListFragment.userRoleModel.deleteChat == 0) {
                    userDetailBinding.deleteChatLayout.visibility = View.GONE
                }

                userDetailBinding.tvUserNameDetail.isEnabled = false
                for (i in userModel.indices) {
                    if (!userModel.get(i).userId.equals(SocketHandler.myUserId)) {
                        userDetailBinding.tvUserNameDetail.setText(userModel.get(i).name)
                        userDetailBinding.tvMailIdUserDetail.text = userModel.get(i).mobile_email
                    }
                }
                userDetailBinding.deleteChatLayout.setOnClickListener(View.OnClickListener {
                    if (internetCheck) {
                        if (userDetailBinding.tvRemoveGroupUD.text.equals(resources.getString(R.string.delete_chat))) {
                            deleteChat()
                        } else if (userDetailBinding.tvRemoveGroupUD.text.equals("Delete Group")) {
                            deleteGroup()
                        }
                    } else {
                        CommonUtils.navigateToOffScreen(internetCheck, mContext!!)
                    }

                })
            }
        }


    }

    private fun callSendImageApi(fileType: String, selectedFile: File, updateJason: JSONObject) {
        if (!progressDialog.isShowing) {
            progressDialog.show()
        }
        val retrofitAPI: RetrofitAPI =
            RetrofitInterface.apiService().create(RetrofitAPI::class.java)
        Log.e("getName", ": " + fileName)
        val filePart = MultipartBody.Part.createFormData(
            "selectFile", fileName, selectedFile
                .asRequestBody(
                    requireContext().contentResolver.getType(Uri.fromFile(selectedFile)).toString()
                        .toMediaTypeOrNull()
                )
        )
        val callApi: Call<FileUploadResponse>? = retrofitAPI.sendMessageInterface(
            SocketHandler.secretKey.toRequestBody(MultipartBody.FORM),
            fileType.toRequestBody(MultipartBody.FORM),
            SocketHandler.myUserId.toRequestBody(MultipartBody.FORM),
            UserListFragment.userName.toRequestBody(MultipartBody.FORM),
            "".toRequestBody(MultipartBody.FORM),
            0,
            filePart
        )
        callApi!!.enqueue(object : Callback<FileUploadResponse?> {
            override fun onResponse(
                call: Call<FileUploadResponse?>,
                response: Response<FileUploadResponse?>
            ) {
                if (response.isSuccessful) {
                    updateJason.put(SocketFields.GROUP_IMAGE, response.body()!!.file)
                    SocketHandler.updateGroup(updateJason)
                }

                Log.e("getImageResponse", "onResponse: " + response.toString())
            }

            override fun onFailure(call: Call<FileUploadResponse?>, t: Throwable) {
                Log.e("getImageResponse", "onError: " + t.message)
            }
        })
    }

    private fun deleteGroup() {
        val dialogBuilder = AlertDialog.Builder(mContext)

        dialogBuilder.setMessage("Are you sure you want to Delete Group?")
            .setCancelable(false)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                val obj = JSONObject()
                obj.put(SocketFields.GROUP_ID, groupId)
                obj.put(SocketFields.USER_ID, SocketHandler.myUserId)
                obj.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
                SocketHandler.deleteGroup(obj)
                SocketHandler.mSocket.on(SocketEvent.DELETE_GROUP_RES.event) {
                    Log.e("getDeleteRes", ": " + it.get(0).toString())
                    val gson = GsonBuilder().create()
                    exitGroupResponseModel =
                        gson.fromJson(it.get(0).toString(), ExitGroupResponseModel::class.java)
                    if (exitGroupResponseModel.isSuccess!!) {
                        SocketHandler.leaveGroup(groupId)
                        activity.runOnUiThread {
                            if (CommonUtils.isFromActivity) {
                                val navControllRefresh =
                                    (context as LibraryActivity).refreshNavigationController()
                                navControllRefresh?.navigate(R.id.action_userDetailFragment_to_chatListFragment)
                            }else{
                               LibraryActivity.navController?.navigate(R.id.action_userDetailFragment_to_chatListFragment)
                            }

                        }

                    }
                }
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

        val alert = dialogBuilder.create()
        alert.setTitle("Alert")
        alert.show()
    }

    private fun deleteChat() {
        val dialogBuilder = AlertDialog.Builder(mContext)

        dialogBuilder.setMessage("Are you sure you want to Delete Chat?")
            .setCancelable(false)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                val obj = JSONObject()
                obj.put(SocketFields.GROUP_ID, groupId)
                obj.put(SocketFields.USER_ID, SocketHandler.myUserId)
                obj.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
                SocketHandler.deleteChat(obj)
                SocketHandler.mSocket.on(SocketEvent.DELETE_CHAT_RES.event) {
                    Log.e("getDeleteRes", ": " + it.get(0).toString())
                    val gson = GsonBuilder().create()
                    exitGroupResponseModel =
                        gson.fromJson(it.get(0).toString(), ExitGroupResponseModel::class.java)
                    if (exitGroupResponseModel.isSuccess!!) {
                        SocketHandler.leaveGroup(groupId)
                        activity.runOnUiThread {
                            LibraryActivity.navController?.navigate(R.id.action_userDetailFragment_to_chatListFragment)
                        }

                    }
                }
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

        val alert = dialogBuilder.create()
        alert.setTitle("Alert")
        alert.show()
    }

}