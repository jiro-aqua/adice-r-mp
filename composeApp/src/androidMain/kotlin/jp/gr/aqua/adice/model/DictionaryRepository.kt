package jp.gr.aqua.adice.model

import jp.sblo.pandora.dice.DiceFactory
import jp.sblo.pandora.dice.IIndexCacheFile
import jp.sblo.pandora.dice.IdicInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DictionaryRepository : KoinComponent {
    private val mDice = DiceFactory.getInstance()
    private val fileSystem = FileSystem.SYSTEM
    private val preferenceRepository: PreferenceRepository by inject()

    private suspend fun createIndex(dicname: String, english: Boolean, defname: String): Boolean {
        return withContext(Dispatchers.IO) {
            // 辞書追加
            var failed = true
            val dicinfo = mDice.open(dicname)
            if (dicinfo != null) {
                // 登録成功ならば
                // インデクスキャッシュファイル名取得
                val cachename = dicname.replace("/", ".") + ".idx"

                // インデクス作成
                if (!dicinfo.readIndexBlock(object : IIndexCacheFile {
                        private val file = ContextModel.cacheDir / cachename

                        override fun getInput() = fileSystem.source(file).buffer()

                        override fun getOutput() = fileSystem.sink(file).buffer()
                    })) {
                    mDice.close(dicinfo)
                } else {
                    failed = false
                    preferenceRepository.setDefaultSettings(dicname, defname, english)
                }
            }
            !failed
        }
    }

    suspend fun importDictionary(file: PickedFileHandle): DictionaryImportResult {
        return withContext(Dispatchers.IO) {
            val rawName = file.displayName.trim()
            if (rawName.isEmpty()) {
                return@withContext DictionaryImportResult(
                    success = false,
                    dicName = "",
                    error = DictionaryImportError.EmptyDisplayName
                )
            }

            val safeName = sanitizeFilename(rawName)
            val destination = uniqueDestinationPath(safeName)

            try {
                file.openSource().use { source ->
                    fileSystem.sink(destination).buffer().use { sink ->
                        sink.writeAll(source)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                return@withContext DictionaryImportResult(
                    success = false,
                    dicName = safeName,
                    error = DictionaryImportError.ReadFailed
                )
            }

            val addResult = addDictionary(destination.toString(), false, destination.nameWithoutExtension())
            if (addResult.first) {
                DictionaryImportResult(success = true, dicName = addResult.second)
            } else {
                DictionaryImportResult(
                    success = false,
                    dicName = addResult.second,
                    error = DictionaryImportError.InvalidDictionary
                )
            }
        }
    }

    suspend fun addDictionary(dicname: String?, english: Boolean, defname: String): Pair<Boolean, String> {
        if (dicname != null) {
            val result = createIndex(dicname, english, defname)
            if (result) {
                writeDictionary()
                return true to dicname
            }
            return false to dicname
        }
        return false to ""
    }

    private fun writeDictionary() {
        val dics = List(mDice.dicNum) { mDice.getDicInfo(it).GetFilename() }
        preferenceRepository.writeDics(dics)
    }

    fun swap(name: String, up: Boolean) {
        val dir = if (up) -1 else 1
        mDice.swap(mDice.getDicInfo(name), dir)
        writeDictionary()
    }

    private fun close(name: String) = mDice.close(mDice.getDicInfo(name))

    fun remove(name: String) {
        // 該当する辞書を閉じる
        close(name)
        // 一覧を更新
        writeDictionary()
        // インデクスファイル削除
        fileSystem.delete(indexCacheFilename(name), mustExist = false)
        // 辞書削除
        fileSystem.delete(name.toSystemPath(), mustExist = false)
        // プレファレンスから削除
        preferenceRepository.removeDic(name)
    }

    // 辞書一覧取得
    fun getDicList(): List<IdicInfo> = List(mDice.dicNum) { mDice.getDicInfo(it) }

    private fun indexCacheFilename(name: String): Path {
        val cacheName = name.replace('/', '.').replace('\\', '.') + ".idx"
        return ContextModel.cacheDir / cacheName
    }

    fun indexCacheAccessor(name: String): IIndexCacheFile {
        return object : IIndexCacheFile {
            private val file = indexCacheFilename(name)

            override fun getInput() = fileSystem.source(file).buffer()

            override fun getOutput() = fileSystem.sink(file).buffer()
        }
    }

    private fun sanitizeFilename(filename: String): String {
        return filename
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .ifEmpty { "dictionary.dic" }
    }

    private fun uniqueDestinationPath(filename: String): Path {
        val dotIndex = filename.lastIndexOf('.')
        val base = if (dotIndex > 0) filename.substring(0, dotIndex) else filename
        val ext = if (dotIndex > 0) filename.substring(dotIndex) else ""
        var candidate = ContextModel.filesDir / filename
        var index = 1
        while (fileSystem.metadataOrNull(candidate) != null) {
            candidate = ContextModel.filesDir / "$base($index)$ext"
            index++
        }
        return candidate
    }

    private fun Path.nameWithoutExtension(): String {
        val filename = name
        val dot = filename.lastIndexOf('.')
        return if (dot <= 0) filename else filename.substring(0, dot)
    }

    private fun String.toSystemPath(): Path = replace('\\', '/').toPath()
}
