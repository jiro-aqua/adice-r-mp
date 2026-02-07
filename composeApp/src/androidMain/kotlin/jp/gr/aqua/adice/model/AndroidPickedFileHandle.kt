package jp.gr.aqua.adice.model

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.IOException
import okio.Source
import okio.source

class AndroidPickedFileHandle(
    private val contentResolver: ContentResolver,
    private val uri: Uri
) : PickedFileHandle {

    override val displayName: String by lazy { resolveDisplayName() }

    override fun openSource(): Source {
        val input = contentResolver.openInputStream(uri)
            ?: throw IOException("failed to open uri: $uri")
        return input.source()
    }

    private fun resolveDisplayName(): String {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    val name = cursor.getString(index)
                    if (!name.isNullOrBlank()) {
                        return name
                    }
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/') ?: ""
    }
}
