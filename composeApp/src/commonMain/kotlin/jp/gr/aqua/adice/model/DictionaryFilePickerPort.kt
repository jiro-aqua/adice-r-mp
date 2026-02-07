package jp.gr.aqua.adice.model

import androidx.compose.runtime.Composable

fun interface DictionaryFilePickerPort {
    fun launch()
}

@Composable
expect fun rememberDictionaryFilePickerPort(
    onPicked: (PickedFileHandle) -> Unit
): DictionaryFilePickerPort
