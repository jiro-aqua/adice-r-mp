package jp.gr.aqua.adice.model

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberDictionaryFilePickerPort(
    onPicked: (PickedFileHandle) -> Unit
): DictionaryFilePickerPort {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onPicked(AndroidPickedFileHandle(context.contentResolver, it))
        }
    }

    return remember(launcher) {
        DictionaryFilePickerPort {
            launcher.launch(arrayOf("*/*"))
        }
    }
}
