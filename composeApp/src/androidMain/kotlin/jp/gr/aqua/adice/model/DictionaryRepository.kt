package jp.gr.aqua.adice.model

import android.net.Uri
import android.provider.OpenableColumns
import jp.sblo.pandora.dice.DiceFactory
import jp.sblo.pandora.dice.IIndexCacheFile
import jp.sblo.pandora.dice.IdicInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.source

class DictionaryRepository {
    private val mDice = DiceFactory.getInstance()
    private val fileSystem = FileSystem.SYSTEM

    private suspend fun createIndex(dicname: String, english: Boolean, defname: String) : Boolean
    {
        return withContext(Dispatchers.IO){
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
                    PreferenceRepository().setDefaultSettings(dicname, defname, english)
                }
            }
            !failed
        }
    }


    suspend fun openDictionary(uri : Uri) : Pair<Boolean,String>
    {
        return withContext(Dispatchers.IO){
            val cr = ContextModel.contentResolver
            val cursor = cr.query(uri, null, null, null, null, null)
            var displayName = ""
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
                cursor!!.close()

                val dicFile = ContextModel.filesDir / displayName
                cr.openInputStream(uri)?.use { input ->
                    fileSystem.sink(dicFile).buffer().use { sink ->
                        sink.writeAll(input.source())
                    }
                }
                cursor.close()
                addDictionary(dicFile.toString(), false, dicFile.nameWithoutExtension())
            } catch (e: Throwable) {
                e.printStackTrace()
                false to ""
            }
        }
    }

    suspend fun addDictionary(dicname: String?, english: Boolean, defname: String) : Pair<Boolean,String>
    {
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
        val dics = List<String>(mDice.dicNum) { mDice.getDicInfo(it).GetFilename() }
        PreferenceRepository().writeDics(dics)
    }
    fun swap(name : String , up : Boolean) {
        val dir = if ( up ) -1 else 1
        mDice.swap(mDice.getDicInfo(name), dir)
        writeDictionary()
    }

    private fun close(name : String ) = mDice.close(mDice.getDicInfo(name))

    fun remove( name : String ){
        // 該当する辞書を閉じる
        close(name)
        // 一覧を更新
        writeDictionary()
        // インデクスファイル削除
        fileSystem.delete(indexCacheFilename(name), mustExist = false)
        // 辞書削除
        fileSystem.delete(name.toSystemPath(), mustExist = false)
        // プレファレンスから削除
        PreferenceRepository().removeDic(name)
    }

    // 辞書一覧取得
    fun getDicList()  = List<IdicInfo>(mDice.dicNum) { mDice.getDicInfo(it) }

    private fun indexCacheFilename(name:String): Path {
        val cacheName = name.replace('/', '.').replace('\\', '.') + ".idx"
        return ContextModel.cacheDir / cacheName
    }

    fun indexCacheAccessor(name : String) : IIndexCacheFile {
        return object : IIndexCacheFile {
            private val file = indexCacheFilename(name)

            override fun getInput() = fileSystem.source(file).buffer()

            override fun getOutput() = fileSystem.sink(file).buffer()
        }
    }

    private fun Path.nameWithoutExtension(): String {
        val filename = name
        val dot = filename.lastIndexOf('.')
        return if (dot <= 0) filename else filename.substring(0, dot)
    }

    private fun String.toSystemPath(): Path = replace('\\', '/').toPath()

}
