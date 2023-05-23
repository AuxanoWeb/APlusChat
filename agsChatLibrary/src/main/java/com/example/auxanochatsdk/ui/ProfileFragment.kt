package com.example.auxanochatsdk.ui

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.example.auxanochatsdk.Activity.LibraryActivity
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.Utils.CommonUtils.hideKeyboard
import com.example.auxanochatsdk.Utils.CommonUtils.internetCheck
import com.example.auxanochatsdk.Utils.PathUtil
import com.example.auxanochatsdk.databinding.FragmentProfileBinding
import com.example.auxanochatsdk.model.ExitGroupResponseModel
import com.example.auxanochatsdk.model.ReceiveProfileInfoModel
import com.example.auxanochatsdk.network.SocketEvent
import com.example.auxanochatsdk.network.SocketFields
import com.example.auxanochatsdk.network.SocketHandler
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import java.io.File


class ProfileFragment : Fragment(), View.OnClickListener {

    private val TAG = ProfileFragment::class.java.name
    lateinit var profileBinding: FragmentProfileBinding
    lateinit var receiveProfileInfoModel: ReceiveProfileInfoModel
    lateinit var mContext: Context
    val SELECT_PICTURE = 200
    var profilepicByteArray: ByteArray? = null
    lateinit var activity: Activity
    var contentType = ""
    var fileName = ""
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach: Called")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContext = container!!.context
        profileBinding = FragmentProfileBinding.inflate(layoutInflater, container, false)
            .apply { executePendingBindings() }
        SocketHandler.getProfileEmit()
        SocketHandler.mSocket.on(SocketEvent.PROFILE_RES.event) {
            Log.e("getProfileResponse", ": " + it.toString())
            val gson = GsonBuilder().create()
            receiveProfileInfoModel =
                gson.fromJson(it.get(0).toString(), ReceiveProfileInfoModel::class.java)
            activity = container!!.context as Activity
            activity.runOnUiThread {
                profileBinding.editTextUserName.setText(receiveProfileInfoModel.name)
                Glide.with(mContext).load(receiveProfileInfoModel.profilePicture)
                    .into(profileBinding.profilePictureIV)
            }

        }
        CommonUtils.setBackgroundColor(mContext,CommonUtils.selectedTheme,profileBinding.clToolBar)
        CommonUtils.setButtonBackgroundDrawable(mContext,CommonUtils.selectedTheme,profileBinding.buttonNext)
        return profileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileBinding.aivBack.setOnClickListener(this)
        profileBinding.buttonNext.setOnClickListener(this)
        profileBinding.profilePictureIV.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == profileBinding.aivBack) {
            LibraryActivity.navController?.navigate(R.id.navigateToUserListFragment)
           // LibraryActivity.navController?.navigateUp()
        } else if (v == profileBinding.buttonNext) {
            if (internetCheck) {
                if (/*profilepicByteArray != null &&*/ profileBinding.editTextUserName.text.toString()
                        .isNotEmpty()
                ) {
                    val chatJsonObj = JSONObject()
                    try {
                        chatJsonObj.put(SocketFields.USER_ID, SocketHandler.myUserId)
                        chatJsonObj.put(
                            SocketFields.NAME,
                            profileBinding.editTextUserName.text.toString()
                        )
                        if(profilepicByteArray!=null){
                            chatJsonObj.put(SocketFields.PROFILE_PICTURE, profilepicByteArray)
                        }else{
                            chatJsonObj.put(SocketFields.PROFILE_PICTURE, "")
                        }

                        chatJsonObj.put(SocketFields.CONTENT_TYPE, contentType)
                        chatJsonObj.put(SocketFields.FILE_NAME, fileName)
                        chatJsonObj.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
                        SocketHandler.updateProfile(chatJsonObj)
                        SocketHandler.mSocket.on(SocketEvent.UPDATE_PROFILE_RES.event) {
                            Log.e("getResponceUpdate", ": " + it.get(0).toString())
                            try {
                                val gson = GsonBuilder().create()
                                val exitGroupResponseModel =
                                    gson.fromJson(
                                        it.get(0).toString(),
                                        ExitGroupResponseModel::class.java
                                    )
                                if (exitGroupResponseModel.isSuccess == true) {
                                    (context as Activity).runOnUiThread {
                                        try {
                                            LibraryActivity.navController?.navigate(R.id.navigateToUserListFragment)
                                            profileBinding.buttonNext.hideKeyboard()
                                        } catch (e: Exception) {

                                        }

                                    }
                                }
                            } catch (e: Exception) {
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.e("getcatchEror", ": " + e.message)
                    }
                } else {
                    /*if (profilepicByteArray == null) {
                        Toast.makeText(mContext, "Select Image...", Toast.LENGTH_SHORT).show()
                    } else*/ if (profileBinding.editTextUserName.text.toString().isEmpty()) {
                        Toast.makeText(mContext, "Enter Name...", Toast.LENGTH_SHORT).show()
                    }

                }
            } else {
                CommonUtils.navigateToOffScreen(CommonUtils.internetCheck, mContext!!)
            }

        } else if (v == profileBinding.profilePictureIV) {
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
        }

    }


    fun requestPermission(permission: String): Boolean {
        val isGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(permission),
                101
            )
        }
        return isGranted
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PICTURE) {
            if (resultCode == RESULT_OK && data!!.getData() != null) {
                val file = File(Uri.parse(PathUtil.getPath(mContext, data!!.data)).path)

                val filePath: String = file.getPath()
                val bitmap = BitmapFactory.decodeFile(filePath)
                profileBinding.profilePictureIV.setImageBitmap(bitmap)
                profilepicByteArray = CommonUtils.getBytesFromBitmap(bitmap)
                try {
                    contentType =
                        getMimeType(Uri.parse(PathUtil.getPath(mContext, data!!.data)).path)!!
                    fileName = file.name
                } catch (e: Exception) {
                    contentType = "image/jpeg"
                    fileName = "img_" + System.currentTimeMillis()
                }

                //   Log.e("getprofilepicByteArray", "name: "+file.name )
                // Log.e("getprofilepicByteArray", "type: "+getMimeType(Uri.parse(PathUtil.getPath(mContext, data!!.data)).path) )
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
}