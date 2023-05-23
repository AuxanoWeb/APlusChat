package com.example.auxanochatsdk.Activity

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Receiver.NetworkChangeReceiver
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.Utils.CommonUtils.isFromActivity
import com.example.auxanochatsdk.Utils.CommonUtils.selectedTheme
import com.example.auxanochatsdk.databinding.ActivityLiraryBinding
import com.google.android.material.snackbar.Snackbar


class LibraryActivity : AppCompatActivity() {

    private val TAG = LibraryActivity::class.java.name
    private lateinit var mBindingMainActivity: ActivityLiraryBinding
    private var mNetworkReceiver: BroadcastReceiver? = null
    var goneInternetFlag = false


    companion object {
        lateinit var navController: NavController
        var internetCheck = false
        lateinit var libraryActivity: LibraryActivity
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        selectedTheme = intent.getIntExtra("theme", 1)
        mBindingMainActivity = DataBindingUtil.setContentView(this, R.layout.activity_lirary)
        libraryActivity = this
        isFromActivity=true
        CommonUtils.setStatusBarColorDark(this)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment?
        navController = navHostFragment!!.navController
        goneInternetFlag = false
        mNetworkReceiver = NetworkChangeReceiver()

        Log.e("getSelectedTheme", "library: "+ selectedTheme)
        registerNetworkBroadcastForNougat()

        val appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()
    }

    fun refreshNavigationController(): NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment?
        navController = navHostFragment!!.navController
        return navController
    }

    fun navigateToOffScreen(istrue: Boolean) {
        if (!istrue) {
            goneInternetFlag = true
            internetCheck = istrue
            val snackBar = Snackbar.make(
                mBindingMainActivity.snakbarbtn, "Internet not Found!",
                Snackbar.LENGTH_LONG
            ).setAction("Action", null)
            snackBar.setActionTextColor(Color.WHITE)
            val snackBarView = snackBar.view
            snackBarView.setBackgroundColor(Color.RED)
            val textView =
                snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
            textView.setTextColor(Color.WHITE)
            snackBar.show()
        } else {
            internetCheck = istrue
            if (goneInternetFlag) {
                goneInternetFlag = false
                val snackBar = Snackbar.make(
                    mBindingMainActivity.snakbarbtn, "Network Found!",
                    Snackbar.LENGTH_LONG
                ).setAction("Action", null)
                snackBar.setActionTextColor(Color.WHITE)
                val snackBarView = snackBar.view
                snackBarView.setBackgroundColor(resources.getColor(R.color.backTonetColor))
                val textView =
                    snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
                textView.setTextColor(Color.WHITE)
                snackBar.show()
            }

        }
    }

    private fun registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    protected fun unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) {
            super.onBackPressed()
            //additional code
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNetworkChanges()
    }
}