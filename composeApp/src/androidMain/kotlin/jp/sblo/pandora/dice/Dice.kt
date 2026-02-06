package jp.sblo.pandora.dice

import okio.FileSystem
import okio.IOException
import okio.buffer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

internal class Dice : Idice {
    private val mIndex: ArrayList<Index> = ArrayList()
    private var mIrreg: HashMap<String, String> = HashMap()

    override fun open(filename: String): IdicInfo? {
        var ret: IdicInfo? = null
        val headerSize = 256

        // 辞書の重複をチェック
        for (i in 0 until dicNum) {
            if (getDicInfo(i).GetFilename().compareTo(filename) == 0) {
                return ret
            }
        }

        try {
            val path = filename.toOkioPath()
            FileSystem.SYSTEM.source(path).buffer().use { source ->
                val headerBytes = ByteArray(headerSize)
                try {
                    source.readFully(headerBytes)
                } catch (_: IOException) {
                    return@use
                }

                val header = Header()
                if (header.load(headerBytes) == 0) {
                    return@use
                }
                // Unicode辞書 かつ ver6以上のみ許容
                if ((header.version.toInt() and 0xFF00) < 0x0600 || header.os.toInt() != 0x20) {
                    return@use
                }
                val unicode = true
                val dic = Index(
                    filename,
                    header.header_size + header.extheader,
                    header.block_size * header.index_block,
                    header.nindex2,
                    header.index_blkbit,
                    header.block_size.toInt(),
                    unicode
                )
                mIndex.add(dic)
                dic.setIrreg(mIrreg)
                ret = dic
            }
        } catch (_: IOException) {
        }

        return ret
    }

    override fun isEnable(num: Int): Boolean {
        val idx = mIndex[num]
        return !idx.GetNotuse() /*&& !idx.GetIrreg()*/
    }

    override val dicNum: Int
        get() = mIndex.size

    override fun close(info: IdicInfo) {
        if (info is Index) {
            info.close()
        }
        mIndex.remove(info)
    }

    override fun getDicInfo(num: Int): IdicInfo = mIndex[num]

    override fun getDicInfo(filename: String): IdicInfo {
        for (i in 0 until mIndex.size) {
            val di = mIndex[i]
            if (di.GetFilename() == filename) {
                return di
            }
        }
        throw NoSuchElementException("Dictionary not found: $filename")
    }

    override fun search(num: Int, word: String) {
        val idx = mIndex[num]
        if (!idx.GetNotuse() /*&& !idx.GetIrreg()*/) {
            idx.Search(word)
        }
    }

    override fun isMatch(num: Int): Boolean {
        val idx = mIndex[num]
        return !idx.GetNotuse() && idx.IsMatch()
    }

    override fun getResult(num: Int): IdicResult {
        val idx = mIndex[num]
        return idx.GetResult()
    }

    override fun getMoreResult(num: Int): IdicResult {
        val idx = mIndex[num]
        return idx.getMoreResult()
    }

    override fun hasMoreResult(num: Int): Boolean {
        val idx = mIndex[num]
        return idx.hasMoreResult(false)
    }

    override fun swap(info: IdicInfo, dir: Int) {
        val current = mIndex.indexOf(info)
        if (dir == 0) {
            return
        } else if (dir < 0 && current > 0) {
            mIndex.remove(info)
            mIndex.add(current - 1, info as Index)
        } else if (dir > 0 && current < mIndex.size - 1) {
            mIndex.remove(info)
            mIndex.add(current + 1, info as Index)
        }
    }

    override fun setIrreg(irreg: HashMap<String, String>) {
        mIrreg = irreg
    }
}
