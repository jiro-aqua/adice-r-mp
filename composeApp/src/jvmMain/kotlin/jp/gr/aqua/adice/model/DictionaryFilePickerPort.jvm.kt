package jp.gr.aqua.adice.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberDictionaryFilePickerPort(onPicked: (PickedFileHandle) -> Unit): DictionaryFilePickerPort {
    return remember(onPicked) {
        JvmDictionaryFilePickerPort(onPicked = onPicked)
    }
}
