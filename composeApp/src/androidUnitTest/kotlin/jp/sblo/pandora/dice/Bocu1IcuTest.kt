package jp.sblo.pandora.dice

import com.ibm.icu.charset.CharsetICU
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import okio.Buffer

class Bocu1IcuTest {
    private val icuBocu = CharsetICU.forName("BOCU-1")

    private companion object {
        private const val BOCU1_MIN = 0x21
        private const val BOCU1_MIDDLE = 0x90
        private const val BOCU1_TRAIL_CONTROLS_COUNT = 20
        private const val BOCU1_TRAIL_BYTE_OFFSET = (BOCU1_MIN - BOCU1_TRAIL_CONTROLS_COUNT)
        private const val BOCU1_TRAIL_COUNT = ((0xff - BOCU1_MIN + 1) + BOCU1_TRAIL_CONTROLS_COUNT)

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

        private const val BOCU1_START_POS_2 = (BOCU1_MIDDLE + BOCU1_REACH_POS_1 + 1)
        private const val BOCU1_START_POS_3 = (BOCU1_START_POS_2 + BOCU1_LEAD_2)
        private const val BOCU1_START_POS_4 = (BOCU1_START_POS_3 + BOCU1_LEAD_3)
        private const val BOCU1_START_NEG_2 = (BOCU1_MIDDLE + BOCU1_REACH_NEG_1)
        private const val BOCU1_START_NEG_3 = (BOCU1_START_NEG_2 - BOCU1_LEAD_2)

        private fun trailToByte(trail: Int): Int {
            return if (trail >= BOCU1_TRAIL_CONTROLS_COUNT) {
                trail + BOCU1_TRAIL_BYTE_OFFSET
            } else {
                error("control trail not used in tests")
            }
        }
    }

    @Test
    fun encodeMatchesIcu4j() {
        val samples = listOf(
            "",
            "ABC",
            "A B\nC",
            "あいうえお",
            "日本語",
            "mix: Englishと日本語",
            "emoji: 😀😃😄"
        )
        for (sample in samples) {
            val expected = sample.toByteArray(icuBocu)
            val actual = Bocu1.encode(sample)
            assertArrayEquals(sample, expected, actual)
        }
    }

    @Test
    fun decodeMatchesIcu4j() {
        val samples = listOf(
            "",
            "ABC",
            "A B\nC",
            "あいうえお",
            "日本語",
            "mix: Englishと日本語",
            "emoji: 😀😃😄"
        )
        for (sample in samples) {
            val bytes = sample.toByteArray(icuBocu)
            val expected = Buffer().write(bytes).inputStream().reader(icuBocu).use { it.readText() }
            val actual = Bocu1.decode(bytes)
            assertEquals(sample, expected, actual)
        }
    }

    @Test
    fun encodeBoundaryDiffTransitionsMatchIcu4j() {
        val prev = 0x40
        val cases = listOf(
            prev + BOCU1_REACH_POS_1 to (BOCU1_START_POS_2 - 1),
            prev + BOCU1_REACH_POS_1 + 1 to BOCU1_START_POS_2,
            prev + BOCU1_REACH_POS_2 to (BOCU1_START_POS_3 - 1),
            prev + BOCU1_REACH_POS_2 + 1 to BOCU1_START_POS_3,
            prev + BOCU1_REACH_POS_3 to (BOCU1_START_POS_4 - 1),
            prev + BOCU1_REACH_POS_3 + 1 to BOCU1_START_POS_4
        )

        for ((codePoint, expectedLead) in cases) {
            val sample = String(Character.toChars(codePoint))
            val expected = sample.toByteArray(icuBocu)
            val actual = Bocu1.encode(sample)

            assertArrayEquals(sample, expected, actual)
            assertEquals(sample, expectedLead, expected[0].toInt() and 0xff)

            val decoded = Bocu1.decode(expected)
            assertEquals(sample, sample, decoded)
        }
    }
}
