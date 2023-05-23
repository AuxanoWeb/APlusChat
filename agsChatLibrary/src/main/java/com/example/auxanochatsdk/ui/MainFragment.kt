package com.example.auxanochatsdk.Activity

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.Utils.CommonUtils
import com.example.auxanochatsdk.Utils.CommonUtils.goneInternetFlag
import com.example.auxanochatsdk.Utils.CommonUtils.selectedTheme


class MainFragment : Fragment() {

    private val TAG = MainFragment::class.java.name

    //   private lateinit var mBindingMainActivity: ActivityLiraryBinding
    private var mNetworkReceiver: BroadcastReceiver? = null
    companion object {
        lateinit var navController: NavController
        fun newInstance(selectedTheme: Int = 1): MainFragment {
            val fragment = MainFragment()
            val args = Bundle()
            args.putInt("selectedTheme", selectedTheme)
            fragment.arguments = args
            return fragment
        }
        // var internetCheck = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //mBindingMainActivity = DataBindingUtil.setContentView((requireActivity()), R.layout.activity_lirary)
        getExtras()
        return inflater.inflate(R.layout.activity_lirary, container, false)
    }
    private fun getExtras() {
        selectedTheme = requireArguments().getInt("selectedTheme")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        CommonUtils.setStatusBarColorDark(requireActivity())
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment?
        // CommanUtils.setStatusBarColorDark((requireActivity()))
        Log.e("getGroupList", "fragment: ")
        //     if(navHostFragment!=null){
        navController = navHostFragment!!.navController
        LibraryActivity.navController = navController
        CommonUtils.isFromActivity =false
        goneInternetFlag = false
        //   mNetworkReceiver = NetworkChangeReceiver()
        registerNetworkBroadcastForNougat()
        val appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()
        //  }

    }
    fun refreshNavigationController(): NavController {
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment?
        LibraryActivity.navController = navHostFragment!!.navController
        return LibraryActivity.navController
    }

    /*fun navigateToOffScreen(istrue: Boolean) {
        *//* if (!istrue) {
             goneInternetFlag = true
             internetCheck=istrue
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
             internetCheck=istrue
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

         }*//*
    }*/

    private fun registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (requireActivity()).registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (requireActivity()).registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    protected fun unregisterNetworkChanges() {
        try {
            (requireActivity()).unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

/*    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) {
            super.onBackPressed()
            //additional code
        } else {
            supportFragmentManager.popBackStack()
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
        unregisterNetworkChanges()
    }
}