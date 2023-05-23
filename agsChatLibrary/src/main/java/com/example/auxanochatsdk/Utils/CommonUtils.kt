package com.example.auxanochatsdk.Utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.auxanochatsdk.Activity.LibraryActivity
import com.example.auxanochatsdk.Activity.MainFragment
import com.example.auxanochatsdk.R
import com.example.auxanochatsdk.network.Theme
import com.example.soketdemo.Model.GroupListModelItem
import com.lassi.presentation.camera.CameraFragment
import kotlinx.android.synthetic.main.fragment_user_detail.*
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


object CommonUtils {
    var goneInternetFlag = false
    var internetCheck = true
    var selectedTheme = 1
    var groupListArrayListTemp: ArrayList<GroupListModelItem> = arrayListOf()
    var multiSelectArrayListTemp: ArrayList<MultiSelectModel> = arrayListOf()
    var isFromActivity=true
    fun fromMillisToTimeString(millis: Long): String {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(millis * 1000)
    }

    fun fromMillisToDateString(millis: Long): String {
        val format = SimpleDateFormat("LLL, d yyyy", Locale.getDefault())
        return format.format(millis * 1000)
    }

    fun BitMapToString(bitmap: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun StringToBitMap(encodedString: String?): Bitmap? {
        return try {
            val encodeByte =
                Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message
            null
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun getBytesFromBitmap(bitmap: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return stream.toByteArray()
    }

    fun getYesterdayDate(): String {
        val dateFormat: DateFormat = SimpleDateFormat("MM/dd/yy")
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1);
        return dateFormat.format(cal.getTime())
    }

    fun setStatusBarColorDark(parent: Activity) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            parent.window.apply {
                decorView.systemUiVisibility = 0
            }
        } else {
            parent.window.apply {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    fun getFileTypeFromExtension(extension: String): String {
        var fileType = ""
        val arrImageExtension = arrayOf<String>("jpg", "png", "jpeg", "gif", "svg", "webp")
        val arrDocExtension =
            arrayOf<String>("doc", "docx", "xml", "xmlx", "pdf", "rtf", "txt", "ppt", "xlsx", "xls")
        val arrAudioExtension = arrayOf<String>("mp3", "aac", "wav", "ogg", "m4a")
        val arrVideoExtension = arrayOf<String>(
            "mp4",
            "avi",
            "mov",
            "3gp",
            "3gpp",
            "mpg",
            "mpeg",
            "webm",
            "flv",
            "m4v",
            "wmv",
            "asx",
            "asf"
        )

        if (arrImageExtension.contains(extension)) {
            fileType = "image"
        } else if (arrDocExtension.contains(extension)) {
            fileType = "document"
        } else if (arrAudioExtension.contains(extension)) {
            fileType = "audio"
        } else if (arrVideoExtension.contains(extension)) {
            fileType = "video"
        }
        return fileType

    }

    fun navigateToOffScreen(istrue: Boolean, context: Context) {
        if (!istrue) {
            goneInternetFlag = true
            internetCheck = istrue
            Toast.makeText(context, "Internet not Found!", Toast.LENGTH_LONG).show()
            /*   val snackBar = Snackbar.make(
                   mBindingMainActivity.snakbarbtn, "Internet not Found!",
                   Snackbar.LENGTH_LONG
               ).setAction("Action", null)
               snackBar.setActionTextColor(Color.WHITE)
               val snackBarView = snackBar.view
               snackBarView.setBackgroundColor(Color.RED)
               val textView =
                   snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
               textView.setTextColor(Color.WHITE)
               snackBar.show()*/
        } else {
            internetCheck = istrue
            if (goneInternetFlag) {
                goneInternetFlag = false
                Toast.makeText(context, "Network Found!", Toast.LENGTH_LONG).show()
                /* val snackBar = Snackbar.make(
                     mBindingMainActivity.snakbarbtn, "Network Found!",
                     Snackbar.LENGTH_LONG
                 ).setAction("Action", null)
                 snackBar.setActionTextColor(Color.WHITE)
                 val snackBarView = snackBar.view
                 snackBarView.setBackgroundColor(resources.getColor(R.color.backTonetColor))
                 val textView =
                     snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
                 textView.setTextColor(Color.WHITE)
                 snackBar.show()*/
            }

        }
    }

    fun setGroupArray(groupListArrayList: ArrayList<GroupListModelItem>) {
        groupListArrayListTemp = arrayListOf()
        // groupListArrayListTemp.clear()
        groupListArrayListTemp = groupListArrayList
    }

    fun getGroupArray(): ArrayList<GroupListModelItem> {
        return groupListArrayListTemp
    }

    fun setSelectMessageArray(multiSelectArrayList: ArrayList<MultiSelectModel>) {
        multiSelectArrayListTemp = arrayListOf()
        multiSelectArrayListTemp.clear()
        multiSelectArrayListTemp = multiSelectArrayList
    }

    fun getSelectMessageArray(): ArrayList<MultiSelectModel> {
        return multiSelectArrayListTemp
    }

    fun isValidContextForGlide(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        if (context is Activity) {
            val activity = context
            if (activity.isDestroyed || activity.isFinishing) {
                return false
            }
        }
        return true
    }

    data class MultiSelectModel(
        val msgId: String,
        val type: String,
        val path: String,
        val contentType: String,
        val thumbnailPath: String,
        val fileName: String
    ) : Serializable

    fun lunchActivityChat(context: Context, theme: Int) {
        Log.e("getSelectedTheme", "lunch: " + theme)
        val intent = Intent(context, LibraryActivity::class.java)
        intent.putExtra("theme", theme)
        (context as Activity).startActivity(intent)
    }
    fun lunchFragmentChat(context: FragmentManager, theme: Int, view: Int) {
        Log.e("getSelectedTheme", "fragmentslunch: " + theme)
        val transaction = getTransaction(context, true)
        transaction.replace(view, MainFragment.newInstance(theme)).commitAllowingStateLoss()

    }
    private fun getTransaction(fragmentManager: FragmentManager, isAnimated: Boolean): FragmentTransaction {
        val transaction = fragmentManager.beginTransaction()
        if (isAnimated)
            transaction.setCustomAnimations(0, 0,
                0, 0)
        return transaction
    }

    fun setBackgroundColor(context: Context, theme: Int, view: View) {
        if (theme == Theme.RED) {
            view.setBackgroundColor(
                context.resources.getColor(
                    R.color.colorToolbarBackground
                )
            )
        } else if (theme == Theme.BLUE) {
            view.setBackgroundColor(
                context.resources.getColor(
                    R.color.colorBackGroundText
                )
            )
        } else if (theme == Theme.YELLOW) {
            view.setBackgroundColor(
                context.resources.getColor(
                    R.color.colorYellow
                )
            )
        }
    }

    fun setBackgroundDrawable(context: Context, theme: Int, view: View) {
        if (theme == Theme.RED) {
            view.setBackgroundDrawable(
                context.resources.getDrawable(
                    R.drawable.my_message_bubble_red
                )
            )
        } else if (theme == Theme.BLUE) {
            view.setBackgroundDrawable(
                context.resources.getDrawable(
                    R.drawable.my_message_bubble_blue
                )
            )
        } else if (theme == Theme.YELLOW) {
            view.setBackgroundDrawable(
                context.resources.getDrawable(
                    R.drawable.my_message_bubble_yellow
                )
            )
        }
    }
    fun setButtonBackgroundDrawable(context: Context, theme: Int, view: View) {
        if (theme == Theme.RED) {
            view.setBackgroundDrawable(
                context.resources.getDrawable(
                    R.drawable.custom_button_red
                )
            )
        } else if (theme == Theme.BLUE) {
            view.setBackgroundDrawable(
                context.resources.getDrawable(
                    R.drawable.custom_button_blue
                )
            )
        } else if (theme == Theme.YELLOW) {
            view.setBackgroundDrawable(
                context.resources.getDrawable(
                    R.drawable.custom_button_yellow
                )
            )
        }
    }

    fun setTextViewTitleStyle(context: Context, theme: Int, view: TextView) {
        if (theme == Theme.RED) {
            view.setTextAppearance(context, R.style.textViewTitle)
        } else if (theme == Theme.BLUE) {
            view.setTextAppearance(context, R.style.textViewTitleBlue)
        } else if (theme == Theme.YELLOW) {
            view.setTextAppearance(context, R.style.textViewTitle)
        }
    }
    fun setTextViewChatStyle(context: Context, theme: Int, view: TextView) {
        if (theme == Theme.RED) {
            view.setTextAppearance(context, R.style.textWhiteColor)
        } else if (theme == Theme.BLUE) {
            view.setTextAppearance(context, R.style.textWhiteColor)
        } else if (theme == Theme.YELLOW) {
            view.setTextAppearance(context, R.style.textBlackColor)
        }
    }

    fun setTextViewSubTitleStyle(context: Context, theme: Int, view: TextView) {
        if (theme == Theme.RED) {
            view.setTextAppearance(context, R.style.textViewSubTitle)
        } else if (theme == Theme.BLUE) {
            view.setTextAppearance(context, R.style.textViewSubTitleBlue)
        } else if (theme == Theme.YELLOW) {
            view.setTextAppearance(context, R.style.textViewSubTitle)
        }
    }

    fun setTextViewNormalStyle(context: Context, theme: Int, view: TextView) {
        if (theme == Theme.RED) {
            view.setTextAppearance(context, R.style.textViewNormal)
        } else if (theme == Theme.BLUE) {
            view.setTextAppearance(context, R.style.textViewNormal)
        } else if (theme == Theme.YELLOW) {
            view.setTextAppearance(context, R.style.textViewNormal)
        }
    }
}