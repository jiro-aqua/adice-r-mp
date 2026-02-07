package jp.gr.aqua.adice.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import adicermp.composeapp.generated.resources.Res
import adicermp.composeapp.generated.resources.menu_copy_all
import adicermp.composeapp.generated.resources.menu_copy_index
import adicermp.composeapp.generated.resources.menu_share
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResultLongClickDialog(
    title: String,
    allText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                // Share
                Text(
                    text = stringResource(Res.string.menu_share),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, allText)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                            onDismiss()
                        }
                        .padding(vertical = 12.dp)
                )

                // Copy index
                Text(
                    text = stringResource(Res.string.menu_copy_index),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val clip = ClipData.newPlainText("text", title)
                            clipboardManager.setPrimaryClip(clip)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp)
                )

                // Copy all
                Text(
                    text = stringResource(Res.string.menu_copy_all),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val clip = ClipData.newPlainText("text", allText)
                            clipboardManager.setPrimaryClip(clip)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp)
                )
            }
        },
        confirmButton = {}
    )
}
