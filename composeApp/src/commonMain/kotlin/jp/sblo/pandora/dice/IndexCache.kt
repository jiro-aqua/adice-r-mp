package jp.sblo.pandora.dice

import okio.FileHandle
import okio.IOException

class IndexCache(
    private val mFile: FileHandle,
    private val mStart: Int,
    private val mSize: Int
) {
    private val mFix: Boolean
    private val mBlockSize: Int
    private val mMap = LruCache<Int, ByteArray>(SEGMENT_CACHE_SIZE)
    private var mFixedBuffer: ByteArray? = null
    private var mSegmentData: ByteArray? = null

    init {
        if (mSize < 1024 * 512) {
            mFix = true
            mBlockSize = mSize
        } else {
            mFix = false
            mBlockSize = 1024
        }
    }

    fun getSegmentWithoutCache(segment: Int, blocksize: Int): ByteArray? {
        if (mSegmentData == null) {
            mSegmentData = ByteArray(blocksize)
        }
        return try {
            val offset = mStart.toLong() + segment.toLong() * blocksize.toLong()
            if (readAt(offset, mSegmentData!!, 0, blocksize)) {
                mSegmentData
            } else {
                null
            }
        } catch (_: IOException) {
            null
        }
    }

    fun getSegment(segment: Int): ByteArray? {
        var segmentdata: ByteArray?

        if (mFix) {
            if (mFixedBuffer == null) {
                mFixedBuffer = ByteArray(mSize)
                try {
                    val offset = mStart.toLong()
                    if (!readAt(offset, mFixedBuffer!!, 0, mSize)) {
                        return null
                    }
                } catch (_: IOException) {
                }
            }
            return mFixedBuffer
        }

        segmentdata = mMap[segment]
        if (segmentdata == null) {
            segmentdata = ByteArray(mBlockSize)
            try {
                val offset = mStart.toLong() + segment.toLong() * mBlockSize.toLong()
                val remaining = mSize - segment * mBlockSize
                val expected = if (remaining >= mBlockSize) mBlockSize else remaining
                if (readAt(offset, segmentdata, 0, expected)) {
                    mMap[segment] = segmentdata
                } else {
                    return null
                }
            } catch (_: IOException) {
                return null
            }
        }
        return segmentdata
    }

    private fun readAt(offset: Long, target: ByteArray, targetOffset: Int, byteCount: Int): Boolean {
        if (byteCount <= 0) return true
        var total = 0
        return try {
            while (total < byteCount) {
                val read = mFile.read(offset + total, target, targetOffset + total, byteCount - total)
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

    private companion object {
        private const val SEGMENT_CACHE_SIZE = 1024
    }

    fun getShort(ptr: Int): Int {
        var segment = ptr / mBlockSize
        var address = ptr % mBlockSize
        var segmentdata = getSegment(segment++)

        var dat = 0
        if (segmentdata != null) {
            var b = segmentdata[address++].toInt() and 0xFF
            dat = dat or b

            if (address >= mBlockSize) {
                address %= mBlockSize
                segmentdata = getSegment(segment++)
            }
            b = segmentdata!![address++].toInt() and 0xFF
            dat = dat or (b shl 8)
        }
        return dat
    }

    fun getInt(ptr: Int): Int {
        var segment = ptr / mBlockSize
        var address = ptr % mBlockSize
        var segmentdata = getSegment(segment++)

        var dat = 0
        if (segmentdata != null) {
            var b = segmentdata[address++].toInt() and 0xFF
            dat = dat or b
            if (address >= mBlockSize) {
                address %= mBlockSize
                segmentdata = getSegment(segment++)
            }
            b = segmentdata!![address++].toInt() and 0xFF
            dat = dat or (b shl 8)
            if (address >= mBlockSize) {
                address %= mBlockSize
                segmentdata = getSegment(segment++)
            }
            b = segmentdata!![address++].toInt() and 0xFF
            dat = dat or (b shl 16)
            if (address >= mBlockSize) {
                address %= mBlockSize
                segmentdata = getSegment(segment++)
            }
            b = segmentdata!![address++].toInt() and 0x7F
            dat = dat or (b shl 24)
        }
        return dat
    }

    private fun compareArrayAsUnsigned(
        aa: ByteArray,
        pa: Int,
        la: Int,
        ab: ByteArray,
        pb: Int,
        lb: Int
    ): Int {
        var paVar = pa
        var laVar = la
        var pbVar = pb
        var lbVar = lb
        while (laVar-- > 0) {
            val sa = aa[paVar++]
            if (lbVar-- > 0) {
                val sb = ab[pbVar++]
                if (sa != sb) {
                    return (sa.toInt() and 0xFF) - (sb.toInt() and 0xFF)
                }
            } else {
                return 1
            }
        }
        if (lbVar > 0) {
            val sb = ab[pbVar]
            if (sb == 0x09.toByte()) {
                return 0
            }
            return -1
        }
        return 0
    }

    fun compare(aa: ByteArray, pa: Int, la: Int, ptr: Int, len: Int): Int {
        var segment = ptr / mBlockSize
        var address = ptr % mBlockSize
        var segmentdata = getSegment(segment++)

        if (segmentdata == null) return -1
        if (len < 0) {
            return 1
        }

        return if (address + len < mBlockSize) {
            compareArrayAsUnsigned(aa, pa, la, segmentdata, address, len)
        } else {
            val lena = mBlockSize - address
            val leno = if (la >= lena) lena else la
            val ret = compareArrayAsUnsigned(aa, pa, leno, segmentdata, address, lena)
            if (ret != 0) {
                ret
            } else {
                if (la < lena) {
                    -1
                } else {
                    address = 0
                    segmentdata = getSegment(segment++)
                    compareArrayAsUnsigned(aa, pa + lena, la - lena, segmentdata!!, address, len - lena)
                }
            }
        }
    }

    fun createIndex(blockbits: Int, nindex: Int, indexPtr: IntArray): Boolean {
        val blocksize = 64 * 1024
        val params = intArrayOf(0, 0, nindex, blocksize, blockbits, 1, 0)

        var segment = 0
        while (countIndexWords(params, getSegmentWithoutCache(segment++, blocksize), indexPtr)) {
        }

        mSegmentData = null

        indexPtr[params[0]] = params[1] + blockbits
        return true
    }

    private fun countIndexWords(params: IntArray, buff: ByteArray?, indexPtr: IntArray): Boolean {
        if (buff == null) {
            return false
        }
        var curidx = params[0]
        var curptr = params[1]
        val max = params[2]
        val buffmax = params[3]
        val blockbits = params[4]
        var found = params[5]
        var ignore = params[6]

        var i = 0
        while (i < buffmax && curidx < max) {
            if (ignore > 0) {
                ignore--
            } else if (found != 0) {
                val ptr = curptr + i + blockbits
                indexPtr[curidx++] = ptr
                ignore = blockbits - 1
                found = 0
            } else if (buff[i] == 0.toByte()) {
                found = 1
            }
            i++
        }

        params[0] = curidx
        params[1] = curptr + i
        params[5] = found
        params[6] = ignore
        return curidx < max
    }
}
