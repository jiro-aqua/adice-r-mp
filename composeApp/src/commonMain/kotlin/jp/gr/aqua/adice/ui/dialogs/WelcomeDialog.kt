package jp.gr.aqua.adice.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import adicermp.composeapp.generated.resources.Res
import adicermp.composeapp.generated.resources.label_close
import adicermp.composeapp.generated.resources.label_download
import adicermp.composeapp.generated.resources.welcome_message
import adicermp.composeapp.generated.resources.welcome_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeDialog(
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.welcome_title)) },
        text = { Text(stringResource(Res.string.welcome_message)) },
        confirmButton = {
            TextButton(onClick = onDownload) {
                Text(stringResource(Res.string.label_download))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.label_close))
            }
        }
    )
}
