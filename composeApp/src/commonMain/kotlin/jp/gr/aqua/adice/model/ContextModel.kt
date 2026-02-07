package jp.gr.aqua.adice.model

import okio.Path

interface ContextModel {
    val cacheDir: Path
    val filesDir: Path
    val versionName: String
    val versionCode: Int
    fun copyToClip(text: String)
}
