package jp.sblo.pandora.dice

internal class Header {
    private val L_HEADERNAME = 100
    private val L_DICTITLE = 40

    var headername: String = ""
    var dictitle: String = ""
    var version: Short = 0
    var lword: Short = 0
    var ljapa: Short = 0
    var block_size: Short = 0
    var index_block: Short = 0
    var header_size: Short = 0
    var index_size: Short = 0

    var empty_block: Short = 0
    var nindex: Short = 0
    var nblock: Short = 0
    var nword: Int = 0

    var dicorder: Byte = 0
    var dictype: Byte = 0

    var attrlen: Byte = 0
    var os: Byte = 0
    var olenumber: Int = 0
    var lid_word: Short = 0

    var lid_japa: Short = 0
    var lid_exp: Short = 0
    var lid_pron: Short = 0
    var lid_other: Short = 0
    var index_blkbit: Boolean = false
    var extheader: Int = 0
    var empty_block2: Int = 0
    var nindex2: Int = 0
    var nblock2: Int = 0

    var update_count: Int = 0
    var dicident: String = ""

    fun load(headerBlock: ByteArray): Int {
        var ret = 0

        try {
            var pos = 0
            headername = headerBlock.decodeToString(pos, pos + L_HEADERNAME)
            pos += L_HEADERNAME
            dictitle = headerBlock.decodeToString(pos, pos + L_DICTITLE)
            pos += L_DICTITLE
            version = readShortLE(headerBlock, pos)
            pos += 2
            if (version.toInt() and 0xFF00 == 0x0500 || version.toInt() and 0xFF00 == 0x0600) {
                lword = readShortLE(headerBlock, pos)
                pos += 2
                ljapa = readShortLE(headerBlock, pos)
                pos += 2

                block_size = readShortLE(headerBlock, pos)
                pos += 2
                index_block = readShortLE(headerBlock, pos)
                pos += 2
                header_size = readShortLE(headerBlock, pos)
                pos += 2
                index_size = readShortLE(headerBlock, pos)
                pos += 2
                empty_block = readShortLE(headerBlock, pos)
                pos += 2
                nindex = readShortLE(headerBlock, pos)
                pos += 2
                nblock = readShortLE(headerBlock, pos)
                pos += 2

                nword = readIntLE(headerBlock, pos)
                pos += 4

                dicorder = headerBlock[pos++]
                dictype = headerBlock[pos++]
                attrlen = headerBlock[pos++]
                os = headerBlock[pos++]

                olenumber = readIntLE(headerBlock, pos)
                pos += 4
                lid_word = readShortLE(headerBlock, pos)
                pos += 2

                lid_japa = readShortLE(headerBlock, pos)
                pos += 2
                lid_exp = readShortLE(headerBlock, pos)
                pos += 2
                lid_pron = readShortLE(headerBlock, pos)
                pos += 2
                lid_other = readShortLE(headerBlock, pos)
                pos += 2
                index_blkbit = headerBlock[pos++].toInt() != 0
                pos++ // dummy0
                extheader = readIntLE(headerBlock, pos)
                pos += 4
                empty_block2 = readIntLE(headerBlock, pos)
                pos += 4
                nindex2 = readIntLE(headerBlock, pos)
                pos += 4
                nblock2 = readIntLE(headerBlock, pos)
                pos += 4

                // 固定部分チェック
                if (attrlen.toInt() == 1) {
                    ret = (version.toInt() shr 8)
                }
            } else if (version.toInt() and 0xFF00 == 0x0400) {
                lword = readShortLE(headerBlock, pos)
                pos += 2
                ljapa = readShortLE(headerBlock, pos)
                pos += 2

                block_size = readShortLE(headerBlock, pos)
                pos += 2
                index_block = readShortLE(headerBlock, pos)
                pos += 2
                header_size = readShortLE(headerBlock, pos)
                pos += 2
                index_size = readShortLE(headerBlock, pos)
                pos += 2
                empty_block = readShortLE(headerBlock, pos)
                pos += 2
                nindex = readShortLE(headerBlock, pos)
                pos += 2
                nblock = readShortLE(headerBlock, pos)
                pos += 2

                nword = readIntLE(headerBlock, pos)
                pos += 4

                dicorder = headerBlock[pos++]
                dictype = headerBlock[pos++]
                attrlen = headerBlock[pos++]

                olenumber = readIntLE(headerBlock, pos)
                pos += 4
                os = headerBlock[pos++]

                lid_word = readShortLE(headerBlock, pos)
                pos += 2
                lid_japa = readShortLE(headerBlock, pos)
                pos += 2
                lid_exp = readShortLE(headerBlock, pos)
                pos += 2
                lid_pron = readShortLE(headerBlock, pos)
                pos += 2
                lid_other = readShortLE(headerBlock, pos)
                pos += 2
                extheader = readIntLE(headerBlock, pos)
                pos += 4
                empty_block2 = readIntLE(headerBlock, pos)
                pos += 4
                nindex2 = readIntLE(headerBlock, pos)
                pos += 4
                nblock2 = readIntLE(headerBlock, pos)
                pos += 4
                index_blkbit = headerBlock[pos++].toInt() != 0
                // 固定部分チェック
                if (block_size.toInt() == 0x100 &&
                    header_size.toInt() == 0x100 &&
                    attrlen.toInt() == 1
                ) {
                    ret = (version.toInt() shr 8)
                }
            } else {
                // not support
            }
        } catch (_: IndexOutOfBoundsException) {
            // アンダーフロー
        }
        return ret
    }

    private fun readShortLE(bytes: ByteArray, offset: Int): Short {
        val b0 = bytes[offset].toInt() and 0xFF
        val b1 = bytes[offset + 1].toInt() and 0xFF
        return (b0 or (b1 shl 8)).toShort()
    }

    private fun readIntLE(bytes: ByteArray, offset: Int): Int {
        val b0 = bytes[offset].toInt() and 0xFF
        val b1 = bytes[offset + 1].toInt() and 0xFF
        val b2 = bytes[offset + 2].toInt() and 0xFF
        val b3 = bytes[offset + 3].toInt() and 0xFF
        return b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
    }
}
