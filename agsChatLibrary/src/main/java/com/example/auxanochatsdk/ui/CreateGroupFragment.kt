package com.example.auxanochatsdk.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
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
import com.example.auxanochatsdk.databinding.FragmentCreateGroupBinding
import com.example.auxanochatsdk.model.ExitGroupResponseModel
import com.example.auxanochatsdk.model.FileUploadResponse
import com.example.auxanochatsdk.network.*
import com.example.auxanochatsdk.ui.CreateGroupUserListFragment.Companion.arrReadCount
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CreateGroupFragment : Fragment() {

    private val TAG = CreateGroupFragment::class.java.name
    lateinit var createGroupBinding: FragmentCreateGroupBinding
    lateinit var exitGroupResponseModel: ExitGroupResponseModel
    lateinit var mContext: Context
    var contentType = ""
    var fileName = ""
    val SELECT_PICTURE = 2002
    var profilepicByteArray: ByteArray? = null
    lateinit var progressDialog: ProgressDialog
    lateinit var selectedFile: File
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createGroupBinding = FragmentCreateGroupBinding.inflate(layoutInflater, container, false)
            .apply { executePendingBindings() }

        init(container)
        crateGroupListner()
        selectProfilePicture()
        return createGroupBinding.root
    }

    private fun selectProfilePicture() {
        createGroupBinding.frameLayout3.setOnClickListener(View.OnClickListener {
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
                val filePath: String = selectedFile.getPath()
                val bitmap = BitmapFactory.decodeFile(filePath)
                (mContext as Activity).runOnUiThread {
                    Glide.with(mContext).asBitmap().load(bitmap)
                        .placeholder(mContext.resources.getDrawable(R.drawable.ic_placeholder_profile))
                        .into(createGroupBinding.groupIV)
                    profilepicByteArray = CommonUtils.getBytesFromBitmap(bitmap)!!
                    try {
                        contentType =
                            getMimeType(Uri.parse(PathUtil.getPath(mContext, data!!.data)).path)!!
                        fileName = selectedFile.name
                    } catch (e: Exception) {
                        contentType = "image/jpeg"
                        fileName = "img_" + System.currentTimeMillis()
                    }
                }

            }
        }
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

    private fun crateGroupListner() {
        createGroupBinding.buttonCrateGroup.setOnClickListener(View.OnClickListener {
            if (internetCheck) {
                if (createGroupBinding.editTextUserNameCreateGroup.text.toString().isNotEmpty()) {
                    val param = JSONObject()
                    val groupDetailJsonObj = JSONObject()
                    val arrUserIdsJson = JSONArray(CreateGroupUserListFragment.arrUserIds)
                    val arrSelectedUserJson = JSONArray(CreateGroupUserListFragment.arrSelectedUser)
                    val arrReadCountJson = JSONArray(arrReadCount)
                    param.put(SocketFields.SECRET_KEY, SocketHandler.secretKey)
                    param.put(SocketFields.IS_GROUP, true)
                    param.put(SocketFields.USER_ID, SocketHandler.myUserId)
                    /*if (profilepicByteArray != null) {
                        param.put(SocketFields.GROUP_IMAGE, profilepicByteArray)
                    } else {
                        param.put(SocketFields.GROUP_IMAGE, "")
                    }*/

                    //   param.put(SocketFields.CONTENT_TYPE, contentType)
                    //  param.put(SocketFields.FILE_NAME, fileName)
                    param.put(SocketFields.MEMBERS, arrUserIdsJson)
                    param.put(SocketFields.GROUP_PERMISSION, JSONArray())

                    param.put(
                        SocketFields.NAME,
                        createGroupBinding.editTextUserNameCreateGroup.text.toString()
                    )
                    param.put(SocketFields.PIN_GROUP_FOR_ALL, 0)
                    Log.e("craeteGroupImage", ": " + param.toString())
                    if (profilepicByteArray == null) {
                        param.put(SocketFields.GROUP_IMAGE, "")
                        callCreateGroupApi(param)
                    } else {
                        callSendImageApi("image", selectedFile, param)
                        /* var task = sendFileAsyncTask(
                             this@CreateGroupFragment,
                             fileName,
                             contentType,
                             profilepicByteArray!!,
                             "image",
                             param,
                             progressDialog
                         )
                         task.execute()*/
                    }
                    //  SocketHandler.crateGroup(param)
                    /* SocketHandler.mSocket.on(SocketEvent.CREATE_GROUP_RES.event) {
                         val gson = GsonBuilder().create()
                         Log.e("checkGroupResponse", ": " + it.toString())
                         exitGroupResponseModel =
                             gson.fromJson(it.get(0).toString(), ExitGroupResponseModel::class.java)
                         if (exitGroupResponseModel.isSuccess == true) {
                             (mContext as Activity).runOnUiThread {
                                 LibraryActivity.navController?.navigate(R.id.navigateToUserListFragment)
                                 createGroupBinding.editTextUserNameCreateGroup.hideKeyboard()
                             }
                         }
                     }*/

                } else {
                    Toast.makeText(context, "Enter group name...", Toast.LENGTH_SHORT).show()
                }
            } else {
                CommonUtils.navigateToOffScreen(CommonUtils.internetCheck, mContext!!)
            }

        })
    }

    @SuppressLint("StaticFieldLeak")
    class sendFileAsyncTask(
        private var activity: CreateGroupFragment,
        private var fileName: String,
        private var contentType: String,
        private var byteArray: ByteArray,
        private var filetype: String,
        private var param: JSONObject,
        private var dialog: ProgressDialog
    ) : AsyncTask<Any, Int, Any?>() {

        override fun onPreExecute() {
            super.onPreExecute()
            dialog.show()
            // do pre stuff such show progress bar
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun doInBackground(vararg req: Any?): Any? {

            // here comes your code that will produce the desired result
            try {
                // activity.callSendImageApi(fileName, contentType, byteArray, filetype, param)

            } catch (e: Exception) {
                dialog.dismiss()
            }


            return ""

        }

        // it will update your progressbar
        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)

        }


        override fun onPostExecute(result: Any?) {
            super.onPostExecute(result)
            //  dialog.dismiss()
            // do what needed on pos execute, like to hide progress bar
            return
        }

    }

    private fun callSendImageApi(
        fileType: String,
        file: File,
        param: JSONObject
    ) {
        val urlString = "http://3.139.188.226:5000/user/public/"
        if (!progressDialog.isShowing) {
            progressDialog.show()
        }
        val retrofitAPI: RetrofitAPI =
            RetrofitInterface.apiService().create(RetrofitAPI::class.java)
        Log.e("getName", ": " + fileName)
        val filePart = MultipartBody.Part.createFormData(
            "selectFile", fileName, file
                .asRequestBody(
                    requireContext().contentResolver.getType(Uri.fromFile(file)).toString()
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
                    param.put(SocketFields.GROUP_IMAGE, response.body()!!.file)
                    callCreateGroupApi(param)
                }

                Log.e("getImageResponse", "onResponse: " + response.toString())
            }

            override fun onFailure(call: Call<FileUploadResponse?>, t: Throwable) {
                Log.e("getImageResponse", "onError: " + t.message)
            }
        })

    }

    private fun callCreateGroupApi(param: JSONObject) {
        val retrofitAPI: RetrofitAPI =
            RetrofitInterface.apiService().create(RetrofitAPI::class.java)
        val json: JsonObject = JsonParser().parse(param.toString()).getAsJsonObject()
        val callApi: Call<JsonObject>? = retrofitAPI.createGroup(json)
        if (!progressDialog.isShowing) {
            progressDialog.show()
        }

        callApi!!.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(
                call: Call<JsonObject?>,
                response: Response<JsonObject?>
            ) {
                progressDialog.cancel()
                if (response.isSuccessful) {
                    (mContext as Activity).runOnUiThread {
                        LibraryActivity.navController?.navigate(R.id.navigateToUserListFragment)
                        createGroupBinding.editTextUserNameCreateGroup.hideKeyboard()
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

    private fun init(container: ViewGroup?) {
        mContext = container!!.context
        progressDialog = ProgressDialog(mContext)
        progressDialog.setMessage("Please wait.!")
        progressDialog.setCancelable(false)
        createGroupBinding.aivBackCreateGroup.setOnClickListener(View.OnClickListener {
            val bundle = Bundle()
            bundle.putString("from", "create")
            LibraryActivity.navController?.navigate(
                R.id.action_createGroupFragment_to_createGroupUserListFragment2,
                bundle
            )
        })
        CommonUtils.setBackgroundColor(mContext,CommonUtils.selectedTheme,createGroupBinding.clToolBar)
        CommonUtils.setButtonBackgroundDrawable(mContext,CommonUtils.selectedTheme,createGroupBinding.buttonCrateGroup)
    }

}