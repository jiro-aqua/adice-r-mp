package jp.gr.aqua.adice.model

import android.content.ContentResolver
import android.content.Context
import jp.gr.aqua.adice.BuildConfig
import okio.Path
import okio.Path.Companion.toPath

class ContextModel{
    companion object{
        lateinit var cacheDir : Path
        lateinit var filesDir : Path
        lateinit var contentResolver : ContentResolver

        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE

        fun initialize(context : Context) {
            cacheDir = context.cacheDir.absolutePath.toSystemPath()
            filesDir = context.filesDir.absolutePath.toSystemPath()
            contentResolver = context.contentResolver
        }

        private fun String.toSystemPath(): Path = replace('\\', '/').toPath()
    }
}

