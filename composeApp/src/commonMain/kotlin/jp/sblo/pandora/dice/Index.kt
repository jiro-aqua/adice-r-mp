package jp.sblo.pandora.dice

import okio.FileHandle
import okio.FileSystem
import okio.IOException

internal class Index(
    filename: String,
    start: Int,
    size: Int,
    nindex: Int,
    blockbits: Boolean,
    blocksize: Int,
    unicode: Boolean
) : IdicInfo {
    protected var m_filename: String = filename
    protected var m_bodyptr: Int = 0
    protected var mSearchResult: Result = Result()

    protected var m_start: Int = start
    protected var m_size: Int = size
    protected var m_blockbits: Int = if (blockbits) 4 else 2
    protected var m_nindex: Int = nindex
    protected var m_blocksize: Int = blocksize
    protected var m_match: Boolean = false
    protected var m_searchmax: Int = 10
    protected var m_accent: Boolean = false
    protected var m_english: Boolean = false
    protected var m_notuse: Boolean = false

    protected var m_IndexFont: String = ""
    protected var m_IndexSize: Int = 0
    protected var m_TransFont: String = ""
    protected var m_TransSize: Int = 0
    protected var m_bPhonetic: Boolean = false
    protected var m_PhoneticFont: String = ""
    protected var m_PhoneticSize: Int = 0
    protected var m_bSample: Boolean = false
    protected var m_SampleFont: String = ""
    protected var m_SampleSize: Int = 0

    protected var m_dicname: String = ""

    protected var mIndexPtr: IntArray? = null
    protected var mIndexArray: ByteArray? = null

    protected var mUnicode: Boolean = unicode
    protected var mBlockCache: BlockCache = BlockCache()
    protected var mEncodeCache = LruCache<String, ByteArray>(ENCODE_CACHE_SIZE)

    protected var mAnalyze: AnalyzeBlock? = null
    protected var mLastIndex: Int = 0
    protected var mIndexCache: IndexCache

    private var mSrcFile: FileHandle? = null

    init {
        try {
            mSrcFile = FileSystem.SYSTEM.openReadOnly(m_filename.toOkioPath())
        } catch (_: IOException) {
        }
        mAnalyze = AnalyzeBlock(mUnicode)
        mIndexCache = IndexCache(mSrcFile!!, m_start, m_size)
    }

    fun searchIndexBlock(word: String): Int {
        var min = 0
        var max = m_nindex - 1

        var __word = mEncodeCache[word]
        if (__word == null) {
            __word = encodeToBytes(word)
            mEncodeCache[word] = __word
        }
        val _word = __word
        val _wordlen = _word.size

        val indexPtr = mIndexPtr!!
        val blockbits = m_blockbits
        val indexCache = mIndexCache

        for (i in 0 until 32) {
            if ((max - min) <= 1) {
                return min
            }
            val look = (min + max) / 2
            val len = indexPtr[look + 1] - indexPtr[look] - blockbits
            val comp = indexCache.compare(_word, 0, _wordlen, indexPtr[look], len)
            if (comp < 0) {
                max = look
            } else if (comp > 0) {
                min = look
            } else {
                return look
            }
        }
        return min
    }

    override fun readIndexBlock(indexcache: IIndexCacheFile?): Boolean {
        var ret = false

        if (mSrcFile != null) {
            m_bodyptr = m_start + m_size

            if (indexcache != null) {
                try {
                    val buff = ByteArray((m_nindex + 1) * 4)
                    val readlen = indexcache.getInput().use { source ->
                        var total = 0
                        while (total < buff.size) {
                            val read = source.read(buff, total, buff.size - total)
                            if (read == -1) break
                            total += read
                        }
                        total
                    }

                    if (readlen == buff.size) {
                        val indexlen = m_nindex
                        val indexptr = IntArray(m_nindex + 1)
                        mIndexPtr = indexptr
                        var ptr = 0
                        for (i in 0..indexlen) {
                            var dat = 0
                            var b = buff[ptr++].toInt() and 0xFF
                            dat = b
                            b = buff[ptr++].toInt() and 0xFF
                            dat = dat or (b shl 8)
                            b = buff[ptr++].toInt() and 0xFF
                            dat = dat or (b shl 16)
                            b = buff[ptr++].toInt() and 0xFF
                            dat = dat or (b shl 24)
                            indexptr[i] = dat
                        }
                        ret = true
                    }
                } catch (_: IOException) {
                }
            }

            if (!ret) {
                val nindex = m_nindex
                val indexPtr = IntArray(nindex + 1)
                mIndexPtr = indexPtr
                ret = mIndexCache.createIndex(m_blockbits, nindex, indexPtr)
                if (ret) {
                    val buff = ByteArray(indexPtr.size * 4)
                    var p = 0
                    for (c in 0..nindex) {
                        var data = indexPtr[c]
                        buff[p++] = (data and 0xFF).toByte()
                        data = data shr 8
                        buff[p++] = (data and 0xFF).toByte()
                        data = data shr 8
                        buff[p++] = (data and 0xFF).toByte()
                        data = data shr 8
                        buff[p++] = (data and 0xFF).toByte()
                    }

                    if (indexcache != null) {
                        try {
                            indexcache.getOutput().use { sink ->
                                sink.write(buff, 0, buff.size)
                                sink.flush()
                            }
                        } catch (_: IOException) {
                        }
                    }
                }
            }
        }
        if (!ret) {
            mIndexPtr = null
        }

        return ret
    }

    internal fun close() {
        try {
            mSrcFile?.close()
        } catch (_: IOException) {
        }
        mSrcFile = null
    }

    fun getBlockNo(num: Int): Int {
        val blkptr = mIndexPtr!![num] - m_blockbits

        mLastIndex = num

        val ptr = blkptr

        return if (m_blockbits == 4) {
            mIndexCache.getInt(ptr)
        } else {
            mIndexCache.getShort(ptr)
        }
    }

    fun AccentIgnoreSearch(_word: String): String {
        return AccentIgnoreSearch(_word, true, true)
    }

    fun AccentIgnoreSearch(_word: String, irreg: Boolean): String {
        return AccentIgnoreSearch(_word, irreg, true)
    }

    fun IsMatch(): Boolean {
        return m_match
    }

    override fun GetFilename(): String {
        return m_filename
    }

    override fun GetSearchMax(): Int {
        return m_searchmax
    }

    override fun SetSearchMax(m: Int) {
        m_searchmax = m
    }

    override fun SetAccent(b: Boolean) {
        m_accent = b
    }

    override fun GetAccent(): Boolean {
        return m_accent
    }

    override fun SetEnglish(b: Boolean) {
        m_english = b
    }

    override fun GetEnglish(): Boolean {
        return m_english
    }

    override fun SetNotuse(b: Boolean) {
        m_notuse = b
    }

    override fun GetNotuse(): Boolean {
        return m_notuse
    }

    override fun SetIndexFont(b: String) {
        m_IndexFont = b
    }

    override fun GetIndexFont(): String {
        return m_IndexFont
    }

    override fun SetIndexSize(b: Int) {
        m_IndexSize = b
    }

    override fun GetIndexSize(): Int {
        return m_IndexSize
    }

    override fun SetTransFont(b: String) {
        m_TransFont = b
    }

    override fun GetTransFont(): String {
        return m_TransFont
    }

    override fun SetTransSize(b: Int) {
        m_TransSize = b
    }

    override fun GetTransSize(): Int {
        return m_TransSize
    }

    override fun SetPhonetic(b: Boolean) {
        m_bPhonetic = b
    }

    override fun GetPhonetic(): Boolean {
        return m_bPhonetic
    }

    override fun SetPhoneticFont(b: String) {
        m_PhoneticFont = b
    }

    override fun GetPhoneticFont(): String {
        return m_PhoneticFont
    }

    override fun SetPhoneticSize(b: Int) {
        m_PhoneticSize = b
    }

    override fun GetPhoneticSize(): Int {
        return m_PhoneticSize
    }

    override fun SetSample(b: Boolean) {
        m_bSample = b
    }

    override fun GetSample(): Boolean {
        return m_bSample
    }

    override fun SetSampleFont(b: String) {
        m_SampleFont = b
    }

    override fun GetSampleFont(): String {
        return m_SampleFont
    }

    override fun SetSampleSize(b: Int) {
        m_SampleSize = b
    }

    override fun GetSampleSize(): Int {
        return m_SampleSize
    }

    override fun SetDicName(b: String) {
        m_dicname = b
    }

    override fun GetDicName(): String {
        return m_dicname
    }

    fun Search(_word: String): Boolean {
        val match = SearchWord(_word)

        if (!match && m_english) {
            val lastResult = mSearchResult

            var tmp = ""
            val tokenize = _word.split(" |\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (token in tokenize) {
                var mutableToken = token
                val irreg = getIrreg(mutableToken)
                if (irreg.isNotEmpty()) {
                    mutableToken = irreg
                } else {
                    var pos = IsEnding(mutableToken, "s")
                    if (pos != -1 && mutableToken.length > 1) {
                        mutableToken = mutableToken.substring(0, pos)
                    }
                    pos = IsEnding(mutableToken, "ed")
                    if (pos != -1 && mutableToken.length > 2) {
                        mutableToken = mutableToken.substring(0, pos)
                    }
                    pos = IsEnding(mutableToken, "ing")
                    if (pos != -1 && mutableToken.length > 3) {
                        mutableToken = mutableToken.substring(0, pos)
                    }
                }
                tmp += mutableToken
                tmp += " "
            }

            tmp = tmp.trim()

            if (_word != tmp) {
                mSearchResult = Result()
                val nmatch = SearchWord(tmp)
                if (mSearchResult.count > 0) {
                    return nmatch
                } else {
                    mSearchResult = lastResult
                }
            }
        }
        return match
    }

    fun AccentIgnoreSearch(_word: String, irreg: Boolean, accent: Boolean): String {
        val l1 = ArrayList<String>()
        val l2 = ArrayList<String>()
        var p1: ArrayList<String> = l1
        var p2: ArrayList<String> = l2

        p1.add("")

        val wordlen = _word.length
        for (w in 0 until wordlen) {
            val pstr = _word[w]

            val tmp = p1
            p1 = p2
            p2 = tmp

            p1.clear()

            for (s0 in p2) {
                val `var` = GetVariation1(pstr, accent)
                val varlen = `var`.length
                for (v in 0 until varlen) {
                    val p = `var`[v]
                    val s1 = s0 + p

                    if (SearchForward(s1)) {
                        p1.add(s1)
                    }
                }
            }
            if (p1.size == 0) {
                break
            }
        }
        val ret = if (p1.size > 0) {
            p1[0]
        } else {
            _word
        }
        return ret
    }

    fun SearchWord(_word: String): Boolean {
        var cnt = 0
        mSearchResult.clear()

        var ret = searchIndexBlock(_word)
        var match = false

        var searchret = false
        while (true) {
            if (ret < m_nindex) {
                val block = getBlockNo(ret++)
                val pblk = readBlockData(block)
                if (pblk != null) {
                    mAnalyze?.setBuffer(pblk)
                    mAnalyze?.setSearch(_word)
                    searchret = mAnalyze?.searchWord() ?: false
                    if (!searchret && mAnalyze?.mEob == true) {
                        continue
                    }
                }
            }
            break
        }
        if (searchret) {
            do {
                val res = mAnalyze?.getRecord()
                if (res == null) {
                    break
                }
                if (res.mIndex == _word) {
                    match = true
                }
                res.mDic = this
                mSearchResult.add(res)
                cnt++
            } while (cnt < m_searchmax && hasMoreResult(true))
        }
        return match
    }

    fun SearchForward(_word: String): Boolean {
        val ret = searchIndexBlock(_word)

        for (blk in 0..1) {
            if (ret + blk >= m_nindex) {
                break
            }
            val block = getBlockNo(ret + blk)
            val pblk = readBlockData(block)

            if (pblk != null) {
                val searchWord = _word
                mAnalyze?.setBuffer(pblk)
                mAnalyze?.setSearch(searchWord)

                if (mAnalyze?.searchWord() == true) {
                    return true
                }
            }
        }
        return false
    }

    fun GetResult(): Result {
        return mSearchResult
    }

    fun getMoreResult(): Result {
        mSearchResult.clear()
        if (mAnalyze != null) {
            var cnt = 0
            while (cnt < m_searchmax && hasMoreResult(true)) {
                val res = mAnalyze?.getRecord()
                if (res == null) {
                    break
                }
                res.mDic = this
                mSearchResult.add(res)
                cnt++
            }
        }
        return mSearchResult
    }

    fun hasMoreResult(incrementptr: Boolean): Boolean {
        var result = mAnalyze?.hasMoreResult(incrementptr) ?: false
        if (!result) {
            if (mAnalyze?.isEob() == true) {
                val nextindex = mLastIndex + 1
                if (nextindex < m_nindex) {
                    val block = getBlockNo(nextindex)
                    val pblk = readBlockData(block)

                    if (pblk != null) {
                        mAnalyze?.setBuffer(pblk)
                        result = mAnalyze?.hasMoreResult(incrementptr) ?: false
                    }
                }
            }
        }
        return result
    }

    fun readBlockData(blkno: Int): ByteArray? {
        var buff = mBlockCache.getBuff(blkno)

        if (buff == null) {
            buff = ByteArray(0x200)
            var pbuf = buff

            try {
                val offset = (m_bodyptr + blkno * m_blocksize).toLong()
                if (!readAt(offset, pbuf, 0, 0x200)) {
                    return null
                }

                var len = pbuf[0].toInt() and 0xFF
                len = len or ((pbuf[1].toInt() and 0xFF) shl 8)

                if ((len and 0x8000) != 0) {
                    len = len and 0x7FFF
                }
                if (len > 0) {
                    if (len * m_blocksize > 0x200) {
                        pbuf = ByteArray(m_blocksize * len)
                        buff.copyInto(destination = pbuf, destinationOffset = 0, startIndex = 0, endIndex = 0x200)
                        val extraOffset = offset + 0x200L
                        val extraLen = len * m_blocksize - 0x200
                        if (!readAt(extraOffset, pbuf, 0x200, extraLen)) {
                            return null
                        }
                    }
                    mBlockCache.putBuff(blkno, pbuf)
                } else {
                    pbuf = null
                }
                return pbuf
            } catch (_: IOException) {
                return null
            }
        }
        return buff
    }

    private fun readAt(offset: Long, target: ByteArray, targetOffset: Int, byteCount: Int): Boolean {
        if (byteCount <= 0) return true
        var total = 0
        return try {
            val file = mSrcFile ?: return false
            while (total < byteCount) {
                val read = file.read(offset + total, target, targetOffset + total, byteCount - total)
                if (read <= 0) {
                    return false
                }
                total += read
            }
            true
        } catch (_: IOException) {
            false
        }
    }

    inner class AnalyzeBlock(private val mUnicode: Boolean) {
        private var mBuff: ByteArray? = null
        private var mLongfield: Boolean = false
        private var mWord: ByteArray? = null
        private var mFoundPtr: Int = -1
        private var mNextPtr: Int = -1
        private val mCompbuff: ByteArray = ByteArray(1024)
        private var mCompLen: Int = 0
        internal var mEob: Boolean = false

        fun setBuffer(buff: ByteArray) {
            mBuff = buff
            mLongfield = (buff[1].toInt() and 0x80) != 0
            mNextPtr = 2
            mEob = false
            mCompLen = 0
        }

        fun setSearch(word: String) {
            var __word = mEncodeCache[word]
            __word = encodeToBytes(word)
            mEncodeCache[word] = __word
            mWord = __word
        }

        fun isEob(): Boolean {
            return mEob
        }

        fun searchWord(): Boolean {
            val _word = mWord ?: return false
            val buff = mBuff ?: return false
            val longfield = mLongfield
            val unicode = mUnicode
            val compbuff = mCompbuff
            val wordlen = _word.size

            mFoundPtr = -1

            var ptr = mNextPtr
            mNextPtr = -1
            while (true) {
                var flen = 0
                val retptr = ptr
                var b: Int

                b = buff[ptr++].toInt()
                flen = flen or (b and 0xFF)

                b = buff[ptr++].toInt()
                b = b shl 8
                flen = flen or (b and 0xFF00)

                if (longfield) {
                    b = buff[ptr++].toInt()
                    b = b shl 16
                    flen = flen or (b and 0xFF0000)

                    b = buff[ptr++].toInt()
                    b = b shl 24
                    flen = flen or (b and 0x7F000000)
                }
                if (flen == 0) {
                    mEob = true
                    break
                }
                var qtr = ptr
                ptr += flen + 1
                if (unicode) {
                    ptr++
                }

                var complen = buff[qtr++].toInt()
                complen = complen and 0xFF

                if (unicode) {
                    qtr++
                }
                while (true) {
                    val value = buff[qtr++]
                    compbuff[complen++] = value
                    if (value == 0.toByte()) {
                        break
                    }
                }

                if (complen < wordlen) {
                    continue
                }

                var equal = true
                for (i in 0 until wordlen) {
                    if (compbuff[i] != _word[i]) {
                        equal = false
                        var cc = compbuff[i].toInt() and 0xFF
                        var cw = _word[i].toInt() and 0xFF
                        if (cc > cw) {
                            return false
                        }
                        break
                    }
                }
                if (equal) {
                    mFoundPtr = retptr
                    mNextPtr = ptr
                    mCompLen = complen - 1
                    return true
                }
            }
            return false
        }

        fun getRecord(): Element? {
            if (mFoundPtr == -1) {
                return null
            }
            val res = Element()
            res.mDic = this@Index

            val compbuff = mCompbuff
            res.mIndex = decodeToString(compbuff, 0, mCompLen)

            val indexstr = res.mIndex
            val tab = indexstr.indexOf('\t')
            if (tab == -1) {
                res.mDisp = ""
            } else {
                res.mIndex = indexstr.substring(0, tab)
                res.mDisp = indexstr.substring(tab + 1)
            }

            val buff = mBuff ?: return res
            val longfield = mLongfield
            val unicode = mUnicode
            var attr: Byte = 0

            var ptr = mFoundPtr

            if (longfield) {
                ptr += 4
            } else {
                ptr += 2
            }
            var qtr = ptr

            var complen = buff[qtr++].toInt()
            complen = complen and 0xFF

            if (unicode) {
                attr = buff[qtr++]
            }
            while (buff[qtr++] != 0.toByte()) {
            }

            if (!unicode) {
                attr = buff[qtr++]
            }

            if (attr.toInt() and 0x10 != 0) {
                val trnslen = getLengthToNextZero(buff, qtr)
                res.mTrans = decodeToString(buff, qtr, trnslen).replace("\r", "")
                qtr += trnslen

                var eatr: Byte
                while (true) {
                    eatr = buff[qtr++]
                    if (eatr.toInt() and 0x80 != 0) {
                        break
                    }
                    if (eatr.toInt() and (0x10 or 0x40) == 0) {
                        if (eatr.toInt() and 0x0F == 0x01) {
                            val len = getLengthToNextZero(buff, qtr)
                            res.mSample = decodeToString(buff, qtr, len).replace("\r", "")
                            qtr += len
                        } else if (eatr.toInt() and 0x0F == 0x02) {
                            val len = getLengthToNextZero(buff, qtr)
                            res.mPhone = decodeToString(buff, qtr, len)
                            qtr += len
                        }
                    } else {
                        break
                    }
                }
            } else {
                res.mTrans = decodeToString(buff, qtr, mNextPtr - qtr).replace("\r", "")
            }
            return res
        }

        fun hasMoreResult(incrementptr: Boolean): Boolean {
            val _word = mWord ?: return false
            val buff = mBuff ?: return false
            val longfield = mLongfield
            val unicode = mUnicode
            val compbuff = mCompbuff

            if (mFoundPtr == -1) {
                return false
            }

            val wordlen = _word.size

            var ptr = mNextPtr

            val retptr = ptr
            var flen: Int
            var b: Int

            b = buff[ptr++].toInt()
            flen = b and 0xFF

            b = buff[ptr++].toInt()
            b = b shl 8
            flen = flen or (b and 0xFF00)

            if (longfield) {
                b = buff[ptr++].toInt()
                b = b shl 16
                flen = flen or (b and 0xFF0000)

                b = buff[ptr++].toInt()
                b = b shl 24
                flen = flen or (b and 0x7F000000)
            }
            if (flen == 0) {
                mEob = true
                return false
            }
            var qtr = ptr
            ptr += flen + 1
            if (unicode) {
                ptr++
            }

            var complen = buff[qtr++].toInt()
            complen = complen and 0xFF

            if (unicode) {
                qtr++
            }
            while (true) {
                val value = buff[qtr++]
                compbuff[complen++] = value
                if (value == 0.toByte()) {
                    break
                }
            }

            if (complen < wordlen) {
                return false
            }

            var equal = true
            for (i in 0 until wordlen) {
                if (compbuff[i] != _word[i]) {
                    equal = false
                    val cc = compbuff[i].toInt() and 0xFF
                    val cw = _word[i].toInt() and 0xFF
                    if (cc > cw) {
                        return false
                    }
                    break
                }
            }
            if (equal && incrementptr) {
                mFoundPtr = retptr
                mNextPtr = ptr
                mCompLen = complen - 1
            }
            return equal
        }
    }

    override fun setIrreg(irreg: HashMap<String, String>) {
        if (mIrreg == null) {
            mIrreg = irreg
        }
    }

    override fun getIrreg(key: String): String {
        return mIrreg?.get(key) ?: ""
    }

    private companion object {
        private const val ENCODE_CACHE_SIZE = 1024
        val t_variation1 = arrayOf(
            arrayOf("a", "ÀÁÂÃÄÅàáâãäåÆæ"),
            arrayOf("c", "Çç"),
            arrayOf("e", "ÈÉÊËèéêë"),
            arrayOf("i", "ÌÍÎÏìíîï"),
            arrayOf("d", "Ðð"),
            arrayOf("n", "Ññ"),
            arrayOf("o", "ÒÓÔÕÖØòóôõöøŒœ"),
            arrayOf("u", "ÙÚÛÜùúûü"),
            arrayOf("y", "ÝÞýþÿ"),
            arrayOf("s", "ß")
        )

        var mIrreg: HashMap<String, String>? = null

        fun decodeToString(array: ByteArray, pos: Int, len: Int): String {
            val slice = array.copyOfRange(pos, pos + len)
            return Bocu1.decode(slice)
        }

        fun encodeToBytes(str: String): ByteArray {
            return Bocu1.encode(str)
        }

        fun getLengthToNextZero(array: ByteArray, pos: Int): Int {
            var len = 0
            while (array[pos + len] != 0.toByte()) {
                len++
            }
            return len
        }

        fun IsEnding(str: String, token: String): Int {
            return if (str.endsWith(token)) {
                str.length - token.length
            } else {
                -1
            }
        }

        fun GetVariation1(ch: Char, accent: Boolean): String {
            val s_buf = StringBuilder()

            s_buf.append(ch)
            s_buf.append(ch.uppercaseChar())

            if (accent) {
                for (v in t_variation1) {
                    if (v[0][0] == ch) {
                        s_buf.append(v[1])
                        break
                    }
                }
            }
            return s_buf.toString()
        }
    }
}
