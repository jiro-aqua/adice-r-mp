package jp.gr.aqua.adice.model

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import jp.gr.aqua.adice.BuildConfig
import okio.Path
import okio.Path.Companion.toPath

class ContextModel{
    companion object{
        lateinit var cacheDir : Path
        lateinit var filesDir : Path

        private lateinit var clipboardManager : ClipboardManager
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE

        fun copyToClip(text: String){
            val clip = ClipData.newPlainText("text", text)
            clipboardManager.setPrimaryClip(clip)
        }

        fun initialize(context : Context) {
            cacheDir = context.cacheDir.absolutePath.toSystemPath()
            filesDir = context.filesDir.absolutePath.toSystemPath()
            clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        }

        private fun String.toSystemPath(): Path = replace('\\', '/').toPath()
    }
}

