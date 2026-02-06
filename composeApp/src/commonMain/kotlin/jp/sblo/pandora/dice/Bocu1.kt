package jp.sblo.pandora.dice

object Bocu1 {
    private const val BOCU1_ASCII_PREV = 0x40
    private const val BOCU1_MIN = 0x21
    private const val BOCU1_MIDDLE = 0x90
    private const val BOCU1_MAX_TRAIL = 0xff
    private const val BOCU1_RESET = 0xff

    private const val BOCU1_TRAIL_CONTROLS_COUNT = 20
    private const val BOCU1_TRAIL_BYTE_OFFSET = (BOCU1_MIN - BOCU1_TRAIL_CONTROLS_COUNT)
    private const val BOCU1_TRAIL_COUNT =
        ((BOCU1_MAX_TRAIL - BOCU1_MIN + 1) + BOCU1_TRAIL_CONTROLS_COUNT)

    private const val BOCU1_SINGLE = 64
    private const val BOCU1_LEAD_2 = 43
    private const val BOCU1_LEAD_3 = 3

    private const val BOCU1_REACH_POS_1 = (BOCU1_SINGLE - 1)
    private const val BOCU1_REACH_NEG_1 = (-BOCU1_SINGLE)
    private const val BOCU1_REACH_POS_2 =
        (BOCU1_REACH_POS_1 + BOCU1_LEAD_2 * BOCU1_TRAIL_COUNT)
    private const val BOCU1_REACH_NEG_2 =
        (BOCU1_REACH_NEG_1 - BOCU1_LEAD_2 * BOCU1_TRAIL_COUNT)
    private const val BOCU1_REACH_POS_3 =
        (BOCU1_REACH_POS_2 + BOCU1_LEAD_3 * BOCU1_TRAIL_COUNT * BOCU1_TRAIL_COUNT)
    private const val BOCU1_REACH_NEG_3 =
        (BOCU1_REACH_NEG_2 - BOCU1_LEAD_3 * BOCU1_TRAIL_COUNT * BOCU1_TRAIL_COUNT)

    private const val BOCU1_START_POS_2 = (BOCU1_MIDDLE + BOCU1_REACH_POS_1 + 1)
    private const val BOCU1_START_POS_3 = (BOCU1_START_POS_2 + BOCU1_LEAD_2)
    private const val BOCU1_START_POS_4 = (BOCU1_START_POS_3 + BOCU1_LEAD_3)
    private const val BOCU1_START_NEG_2 = (BOCU1_MIDDLE + BOCU1_REACH_NEG_1)
    private const val BOCU1_START_NEG_3 = (BOCU1_START_NEG_2 - BOCU1_LEAD_2)

    private val bocu1ByteToTrail = intArrayOf(
        -1, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, -1,
        -1, -1, -1, -1, -1, -1, -1, -1,
        0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d,
        0x0e, 0x0f, -1, -1, 0x10, 0x11, 0x12, 0x13,
        -1
    )

    private val bocu1TrailToByte = intArrayOf(
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x10, 0x11,
        0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19,
        0x1c, 0x1d, 0x1e, 0x1f
    )

    private fun bocu1TrailToByte(trail: Int): Int {
        return if (trail >= BOCU1_TRAIL_CONTROLS_COUNT) {
            trail + BOCU1_TRAIL_BYTE_OFFSET
        } else {
            bocu1TrailToByte[trail]
        }
    }

    private fun bocu1SimplePrev(c: Int): Int {
        return (c and 0x7f.inv()) + BOCU1_ASCII_PREV
    }

    private fun bocu1Prev(c: Int): Int {
        return when {
            c <= 0x309f -> 0x3070
            c in 0x4e00..0x9fa5 -> 0x4e00 - BOCU1_REACH_NEG_2
            c >= 0xac00 -> (0xd7a3 + 0xac00) / 2
            else -> bocu1SimplePrev(c)
        }
    }

    private fun bocu1PrevFast(c: Int): Int {
        return if (c < 0x3040 || c > 0xd7a3) {
            bocu1SimplePrev(c)
        } else {
            bocu1Prev(c)
        }
    }

    private fun diffIsSingle(diff: Int): Boolean {
        return diff in BOCU1_REACH_NEG_1..BOCU1_REACH_POS_1
    }

    private fun diffIsDouble(diff: Int): Boolean {
        return diff in BOCU1_REACH_NEG_2..BOCU1_REACH_POS_2
    }

    private fun negDivMod(n: Int, d: Int): Pair<Int, Int> {
        var diff = n
        var m = diff % d
        diff /= d
        if (m < 0) {
            diff -= 1
            m += d
        }
        return diff to m
    }

    private fun packDiff(n: Int): Int {
        var diff = n
        var result: Int
        if (diff >= BOCU1_REACH_NEG_1) {
            if (diff <= BOCU1_REACH_POS_2) {
                diff -= BOCU1_REACH_POS_1 + 1
                result = 0x02000000

                var m = diff % BOCU1_TRAIL_COUNT
                diff /= BOCU1_TRAIL_COUNT
                result = result or bocu1TrailToByte(m)
                result = result or ((BOCU1_START_POS_2 + diff) shl 8)
            } else if (diff <= BOCU1_REACH_POS_3) {
                diff -= BOCU1_REACH_POS_2 + 1
                result = 0x03000000

                var m = diff % BOCU1_TRAIL_COUNT
                diff /= BOCU1_TRAIL_COUNT
                result = result or bocu1TrailToByte(m)

                m = diff % BOCU1_TRAIL_COUNT
                diff /= BOCU1_TRAIL_COUNT
                result = result or (bocu1TrailToByte(m) shl 8)

                result = result or ((BOCU1_START_POS_3 + diff) shl 16)
            } else {
                diff -= BOCU1_REACH_POS_3 + 1

                var m = diff % BOCU1_TRAIL_COUNT
                diff /= BOCU1_TRAIL_COUNT
                result = bocu1TrailToByte(m)

                m = diff % BOCU1_TRAIL_COUNT
                diff /= BOCU1_TRAIL_COUNT
                result = result or (bocu1TrailToByte(m) shl 8)

                result = result or (bocu1TrailToByte(diff) shl 16)
                result = result or (BOCU1_START_POS_4 shl 24)
            }
        } else {
            if (diff >= BOCU1_REACH_NEG_2) {
                diff -= BOCU1_REACH_NEG_1
                result = 0x02000000

                val divMod = negDivMod(diff, BOCU1_TRAIL_COUNT)
                diff = divMod.first
                result = result or bocu1TrailToByte(divMod.second)
                result = result or ((BOCU1_START_NEG_2 + diff) shl 8)
            } else if (diff >= BOCU1_REACH_NEG_3) {
                diff -= BOCU1_REACH_NEG_2
                result = 0x03000000

                var divMod = negDivMod(diff, BOCU1_TRAIL_COUNT)
                diff = divMod.first
                result = result or bocu1TrailToByte(divMod.second)

                divMod = negDivMod(diff, BOCU1_TRAIL_COUNT)
                diff = divMod.first
                result = result or (bocu1TrailToByte(divMod.second) shl 8)

                result = result or ((BOCU1_START_NEG_3 + diff) shl 16)
            } else {
                diff -= BOCU1_REACH_NEG_3

                var divMod = negDivMod(diff, BOCU1_TRAIL_COUNT)
                diff = divMod.first
                result = bocu1TrailToByte(divMod.second)

                divMod = negDivMod(diff, BOCU1_TRAIL_COUNT)
                diff = divMod.first
                result = result or (bocu1TrailToByte(divMod.second) shl 8)

                val m = diff + BOCU1_TRAIL_COUNT
                result = result or (bocu1TrailToByte(m) shl 16)
                result = result or (BOCU1_MIN shl 24)
            }
        }
        return result
    }

    private fun bocu1LengthFromPacked(packed: Int): Int {
        return if ((packed ushr 24) < 0x04) {
            packed ushr 24
        } else {
            4
        }
    }

    private fun decodeBocu1LeadByte(b: Int): Int {
        val diff: Int
        val count: Int
        if (b >= BOCU1_START_NEG_2) {
            if (b < BOCU1_START_POS_3) {
                diff = (b - BOCU1_START_POS_2) * BOCU1_TRAIL_COUNT + BOCU1_REACH_POS_1 + 1
                count = 1
            } else if (b < BOCU1_START_POS_4) {
                diff = (b - BOCU1_START_POS_3) * BOCU1_TRAIL_COUNT * BOCU1_TRAIL_COUNT +
                    BOCU1_REACH_POS_2 + 1
                count = 2
            } else {
                diff = BOCU1_REACH_POS_3 + 1
                count = 3
            }
        } else {
            if (b >= BOCU1_START_NEG_3) {
                diff = (b - BOCU1_START_NEG_2) * BOCU1_TRAIL_COUNT + BOCU1_REACH_NEG_1
                count = 1
            } else if (b > BOCU1_MIN) {
                diff = (b - BOCU1_START_NEG_3) * BOCU1_TRAIL_COUNT * BOCU1_TRAIL_COUNT +
                    BOCU1_REACH_NEG_2
                count = 2
            } else {
                diff = -BOCU1_TRAIL_COUNT * BOCU1_TRAIL_COUNT * BOCU1_TRAIL_COUNT +
                    BOCU1_REACH_NEG_3
                count = 3
            }
        }
        return (diff shl 2) or count
    }

    private fun decodeBocu1TrailByte(count: Int, b: Int): Int {
        var byte = b
        byte = if (byte <= 0x20) {
            bocu1ByteToTrail[byte]
        } else {
            byte - BOCU1_TRAIL_BYTE_OFFSET
        }
        if (byte < 0) {
            return -1
        }
        return when (count) {
            1 -> byte
            2 -> byte * BOCU1_TRAIL_COUNT
            else -> byte * (BOCU1_TRAIL_COUNT * BOCU1_TRAIL_COUNT)
        }
    }

    fun encode(input: String): ByteArray {
        val out = ByteArrayBuilder()
        var prev = BOCU1_ASCII_PREV
        var i = 0
        while (i < input.length) {
            val c = input.codePointAtCompat(i)
            i += codePointWidth(c)
            if (c <= 0x20) {
                if (c != 0x20) {
                    prev = BOCU1_ASCII_PREV
                }
                out.write(c)
                continue
            }
            val diff = c - prev
            prev = bocu1PrevFast(c)
            if (diffIsSingle(diff)) {
                out.write(BOCU1_MIDDLE + diff)
                continue
            }
            val packed = if (diffIsDouble(diff)) {
                packDiff(diff)
            } else {
                packDiff(diff)
            }
            val length = bocu1LengthFromPacked(packed)
            when (length) {
                2 -> {
                    out.write(packed ushr 8)
                    out.write(packed)
                }
                3 -> {
                    out.write(packed ushr 16)
                    out.write(packed ushr 8)
                    out.write(packed)
                }
                4 -> {
                    out.write(packed ushr 24)
                    out.write(packed ushr 16)
                    out.write(packed ushr 8)
                    out.write(packed)
                }
            }
        }
        return out.toByteArray()
    }

    fun decode(bytes: ByteArray): String {
        val sb = StringBuilder()
        var prev = BOCU1_ASCII_PREV
        var i = 0
        while (i < bytes.size) {
            val b = bytes[i].toInt() and 0xff
            if (b <= 0x20) {
                if (b != 0x20) {
                    prev = BOCU1_ASCII_PREV
                }
                sb.append(b.toChar())
                i++
                continue
            }
            if (b == BOCU1_RESET) {
                prev = BOCU1_ASCII_PREV
                i++
                continue
            }
            if (b >= BOCU1_START_NEG_2 && b < BOCU1_START_POS_2) {
                val c = prev + (b - BOCU1_MIDDLE)
                appendCodePoint(sb, c)
                prev = if (c < 0x3000) {
                    bocu1SimplePrev(c)
                } else {
                    bocu1PrevFast(c)
                }
                i++
                continue
            }
            val packed = decodeBocu1LeadByte(b)
            var diff = packed shr 2
            var count = packed and 3
            i++
            var valid = true
            while (count > 0) {
                if (i >= bytes.size) {
                    valid = false
                    break
                }
                val trail = bytes[i].toInt() and 0xff
                val delta = decodeBocu1TrailByte(count, trail)
                if (delta < 0) {
                    valid = false
                    break
                }
                diff += delta
                count--
                i++
            }
            if (!valid) {
                sb.append('\uFFFD')
                prev = BOCU1_ASCII_PREV
                continue
            }
            val c = prev + diff
            if (c < 0 || c > 0x10ffff) {
                sb.append('\uFFFD')
                prev = BOCU1_ASCII_PREV
                continue
            }
            appendCodePoint(sb, c)
            prev = bocu1PrevFast(c)
        }
        return sb.toString()
    }

    private fun appendCodePoint(sb: StringBuilder, c: Int) {
        if (c <= 0xffff) {
            sb.append(c.toChar())
        } else {
            val cp = c - 0x10000
            val lead = 0xD800 + (cp ushr 10)
            val trail = 0xDC00 + (cp and 0x3FF)
            sb.append(lead.toChar())
            sb.append(trail.toChar())
        }
    }

    private fun String.codePointAtCompat(index: Int): Int {
        val c1 = this[index].code
        if (c1 in 0xD800..0xDBFF && index + 1 < length) {
            val c2 = this[index + 1].code
            if (c2 in 0xDC00..0xDFFF) {
                return ((c1 - 0xD800) shl 10) + (c2 - 0xDC00) + 0x10000
            }
        }
        return c1
    }

    private fun codePointWidth(codePoint: Int): Int {
        return if (codePoint >= 0x10000) 2 else 1
    }

    private class ByteArrayBuilder(initialCapacity: Int = 128) {
        private var data = ByteArray(initialCapacity)
        private var size = 0

        fun write(value: Int) {
            val b = (value and 0xFF).toByte()
            ensureCapacity(1)
            data[size++] = b
        }

        fun toByteArray(): ByteArray = data.copyOf(size)

        private fun ensureCapacity(extra: Int) {
            val needed = size + extra
            if (needed <= data.size) return
            var newSize = data.size
            while (newSize < needed) {
                newSize = newSize * 2
            }
            data = data.copyOf(newSize)
        }
    }
}
