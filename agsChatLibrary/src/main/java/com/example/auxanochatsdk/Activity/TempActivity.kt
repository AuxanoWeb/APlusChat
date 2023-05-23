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

       // CommonUtils.lunchActivityChat(this, Theme.RED,"U2FsdGVkX19wmVtaa5bOlZVjpazEyB3tEX/0BAmWufQjL2AscUo+sZ72L19onNWL","10")
        CommonUtils.lunchFragmentChat(supportFragmentManager,Theme.RED,R.id.loadChatFrameLayout,"U2FsdGVkX19wmVtaa5bOlZVjpazEyB3tEX/0BAmWufQjL2AscUo+sZ72L19onNWL","3")
    }
}