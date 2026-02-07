package jp.gr.aqua.adice.model

import android.content.ContentResolver
import android.content.Context
import okio.Path
import okio.Path.Companion.toPath

class ContextModel{
    companion object{
        lateinit var cacheDir : Path
        lateinit var filesDir : Path
        lateinit var contentResolver : ContentResolver

        fun initialize(context : Context) {
            cacheDir = context.cacheDir.absolutePath.toSystemPath()
            filesDir = context.filesDir.absolutePath.toSystemPath()
            contentResolver = context.contentResolver
        }

        private fun String.toSystemPath(): Path = replace('\\', '/').toPath()
    }
}
