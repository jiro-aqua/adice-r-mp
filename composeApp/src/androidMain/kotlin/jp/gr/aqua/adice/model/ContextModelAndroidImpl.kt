package jp.gr.aqua.adice.model

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import jp.gr.aqua.adice.BuildConfig
import okio.Path
import okio.Path.Companion.toPath

class ContextModelAndroidImpl(context: Context) : ContextModel {
    override val cacheDir: Path = context.cacheDir.absolutePath.toSystemPath()
    override val filesDir: Path = context.filesDir.absolutePath.toSystemPath()
    override val versionName = BuildConfig.VERSION_NAME
    override val versionCode = BuildConfig.VERSION_CODE

    private val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    override fun copyToClip(text: String) {
        val clip = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clip)
    }

    private fun String.toSystemPath(): Path = replace('\\', '/').toPath()
}

