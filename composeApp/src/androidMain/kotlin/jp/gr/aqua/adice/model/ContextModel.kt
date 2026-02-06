package jp.gr.aqua.adice.model

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import okio.Path
import okio.Path.Companion.toPath

class ContextModel{
    companion object{
        lateinit var resources : Resources
        lateinit var cacheDir : Path
        lateinit var filesDir : Path
        lateinit var assets : AssetManager
        lateinit var contentResolver : ContentResolver

        fun initialize(context : Context) {
            resources = context.resources
            cacheDir = context.cacheDir.absolutePath.toSystemPath()
            filesDir = context.filesDir.absolutePath.toSystemPath()
            assets = context.assets
            contentResolver = context.contentResolver
        }

        private fun String.toSystemPath(): Path = replace('\\', '/').toPath()
    }
}
