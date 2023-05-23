package com.example.auxanochatsdk.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.auxanochatsdk.Utils.CommonUtils


class NetworkChangeReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent?) {
        try {
            if (isOnline(context)) {
                CommonUtils.navigateToOffScreen(CommonUtils.internetCheck, context)
            } else {
                CommonUtils.navigateToOffScreen(CommonUtils.internetCheck, context)
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            netInfo != null && netInfo.isConnected
        } catch (e: NullPointerException) {
            e.printStackTrace()
            false
        }
    }
}