package jp.gr.aqua.adice.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.openZip
import okio.source
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class DownloadRepository : KoinComponent {
    private val fileSystem = FileSystem.SYSTEM
    private val contextModel: ContextModel by inject()

    private val STORAGE: Path by lazy { contextModel.filesDir }

    private fun getName(path: String): String {
        val patharr = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return patharr[patharr.size - 1]
    }

    suspend fun downloadDicfile(url: String): String? {
        return withContext(Dispatchers.IO){
            val client = OkHttpClient()
            val request = Request.Builder()
                    .url(url)
                    .build()

            client.newCall(request).execute().use { response ->
                val body = response.body.byteStream()

                if (url.endsWith(".dic")) {
                    val dst = STORAGE / getName(url)
                    body.use { input ->
                        fileSystem.sink(dst).buffer().use { sink ->
                            sink.writeAll(input.source())
                        }
                    }
                    dst.toString()
                } else if (url.endsWith(".zip")) {
                    val zipPath = STORAGE / getName(url)
                    body.use { input ->
                        fileSystem.sink(zipPath).buffer().use { sink ->
                            sink.writeAll(input.source())
                        }
                    }
                    try {
                        extractZip(zipPath)
                    } finally {
                        fileSystem.delete(zipPath, mustExist = false)
                    }
                }else{
                    null
                }
            }
        }
    }

    private fun extractZip(zipPath: Path): String? {
        try {
            fileSystem.openZip(zipPath).use { zipFs ->
                val dicEntry = findDicEntry(zipFs) ?: return null
                val nf = STORAGE / dicEntry.name
                zipFs.source(dicEntry).buffer().use { source ->
                    fileSystem.sink(nf).buffer().use { sink ->
                        sink.writeAll(source)
                    }
                }
                return nf.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun findDicEntry(zipFs: FileSystem, dir: Path = "/".toPath()): Path? {
        val children = try {
            zipFs.list(dir)
        } catch (_: IOException) {
            return null
        }
        for (child in children) {
            if (child.name.endsWith(".dic", ignoreCase = true)) {
                return child
            }
            val nested = findDicEntry(zipFs, child)
            if (nested != null) {
                return nested
            }
        }
        return null
    }
}
