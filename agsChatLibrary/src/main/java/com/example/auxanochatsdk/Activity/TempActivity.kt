package com.example.auxanochatsdk.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.network.Theme

class TempActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temp)

       // CommonUtils.lunchActivityChat(this, Theme.RED)
        CommonUtils.lunchFragmentChat(supportFragmentManager,Theme.RED,R.id.loadChatFrameLayout)
    }
}