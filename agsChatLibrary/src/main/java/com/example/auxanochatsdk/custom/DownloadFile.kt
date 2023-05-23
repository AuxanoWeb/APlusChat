package com.example.auxanochatsdk.custom

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.*
import java.net.URL
import java.net.URLConnection

@SuppressLint("StaticFieldLeak")
class DownloadFile(
    var context: Context?,
    var message: String?,
    var fileName: String,
    var fileType: String
) : AsyncTask<Any, Int, Any?>() {
    var mProgressDialog: ProgressDialog =
        ProgressDialog(context) // Change Mainactivity.this with your activity name.
    var strFolderName: String? = null
    protected override fun onPreExecute() {
        super.onPreExecute()
        mProgressDialog.setMessage("Downloading...")
        mProgressDialog.setIndeterminate(false)
        mProgressDialog.setMax(100)
        mProgressDialog.setCancelable(false)
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        mProgressDialog.show()
    }

    override fun doInBackground(vararg req: Any?): Any? {
        var count: Int
        try {
            val url = URL(message)
            val conexion: URLConnection = url.openConnection()
            conexion.connect()
            val targetFileName =
                "Img_" + System.currentTimeMillis() + ".png" //Change name and subname
            val lenghtOfFile: Int = conexion.getContentLength()

            var PATH: String = ""

            if (fileType.equals("image")) {
                PATH = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ).toString() + "/" + "GeChat" + "/"
            } else if (fileType.equals("document")) {
                PATH = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS
                ).toString() + "/" + "GeChat" + "/"
            }
            val folder =
                File(PATH)
            if (!folder.exists()) {
                folder.mkdir() //If there is no folder it will be created.
            }
            val input: InputStream = BufferedInputStream(url.openStream())
            val output: OutputStream = FileOutputStream(PATH + fileName)
            val data = ByteArray(1024)
            var total: Long = 0
            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                publishProgress((total * 100 / lenghtOfFile).toInt())
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()
            if (fileType.equals("image")) {
                MediaScannerConnection.scanFile(context,
                    arrayOf<String>(PATH + fileName),
                    null,
                    object : MediaScannerConnection.OnScanCompletedListener {
                        override fun onScanCompleted(path: String, uri: Uri) {
                            Log.e("ExternalStorage", "Scanned $path:")
                            Log.e("ExternalStorage", "-> uri=$uri")
                        }
                    })
            }

        } catch (e: Exception) {
            mProgressDialog.dismiss()
            Log.e("getErrorCatch", "download: " + e.message)
        }
        return null
    }

    protected override fun onProgressUpdate(vararg progress: Int?) {
        mProgressDialog.setProgress(progress[0]!!)
        if (mProgressDialog.getProgress() === mProgressDialog.getMax()) {
            mProgressDialog.dismiss()
            Toast.makeText(context, "Download Completed.", Toast.LENGTH_LONG).show()
        }
    }

    protected fun onPostExecute(result: String?) {}
}