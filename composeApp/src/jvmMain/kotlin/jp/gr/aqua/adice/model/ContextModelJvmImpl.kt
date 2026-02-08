package jp.gr.aqua.adice.model

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

class ContextModelJvmImpl : ContextModel {
    private val fileSystem = FileSystem.SYSTEM
    private val rootDir: Path = (System.getProperty("user.home") + "/.adicer-mp").toPath()

    override val cacheDir: Path = (rootDir / "cache").also { fileSystem.createDirectories(it) }
    override val filesDir: Path = (rootDir / "files").also { fileSystem.createDirectories(it) }
    override val versionName: String = "1.0"
    override val versionCode: Int = 1

    override fun copyToClip(text: String) {
        runCatching {
            val selection = StringSelection(text)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
        }
    }
}
