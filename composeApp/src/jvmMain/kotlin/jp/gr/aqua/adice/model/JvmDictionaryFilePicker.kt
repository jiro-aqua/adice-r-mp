package jp.gr.aqua.adice.model

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Source

class JvmDictionaryFilePicker(
    private val parent: Frame? = null
) {
    fun pickDictionary(): PickedFileHandle? {
        val dialog = FileDialog(parent, "Select Dictionary", FileDialog.LOAD)
        dialog.isMultipleMode = false
        dialog.isVisible = true
        val file = dialog.file ?: return null
        val directory = dialog.directory ?: return null
        val selected = File(directory, file)
        return JvmPickedFileHandle(selected.absolutePath.replace('\\', '/').toPath())
    }
}

class JvmDictionaryFilePickerPort(
    private val parent: Frame? = null,
    private val onPicked: (PickedFileHandle) -> Unit
) : DictionaryFilePickerPort {
    private val picker = JvmDictionaryFilePicker(parent)

    override fun launch() {
        picker.pickDictionary()?.let(onPicked)
    }
}

class JvmPickedFileHandle(
    private val path: Path
) : PickedFileHandle {
    override val displayName: String = path.name

    override fun openSource(): Source = FileSystem.SYSTEM.source(path)
}
