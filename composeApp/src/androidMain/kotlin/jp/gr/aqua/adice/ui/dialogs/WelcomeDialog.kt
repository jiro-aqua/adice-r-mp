package jp.gr.aqua.adice.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import jp.gr.aqua.adice.R

@Composable
fun WelcomeDialog(
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.welcome_title)) },
        text = { Text(stringResource(R.string.welcome_message)) },
        confirmButton = {
            TextButton(onClick = onDownload) {
                Text(stringResource(R.string.label_download))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_close))
            }
        }
    )
}
