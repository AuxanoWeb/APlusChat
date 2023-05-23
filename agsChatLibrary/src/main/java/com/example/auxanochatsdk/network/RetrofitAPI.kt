package com.example.auxanochatsdk.network

import com.example.auxanochatsdk.model.FileUploadResponse
import com.example.auxanochatsdk.model.SendFileModel
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*


interface RetrofitAPI {
    @Multipart
    @POST("upload-file-new")
    fun sendMessageInterface(
        @Part("secretKey") secretKey: RequestBody,
        @Part("type") type: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("senderName") senderName: RequestBody,
        @Part("groupId") groupId: RequestBody,
        @Part("isChat") isChat: Int,
        @Part selectFile: MultipartBody.Part
    ): Call<FileUploadResponse>?

    @POST("create-group")
    fun createGroup(@Body createGroupPara: JsonObject?): Call<JsonObject>?
}