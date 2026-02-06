package jp.sblo.pandora.dice

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class Bocu1Test {
    @Test
    fun encodeAsciiSingle() {
        val encoded = Bocu1.encode("A")
        assertArrayEquals(byteArrayOf(0x91.toByte()), encoded)
    }

    @Test
    fun encodeAsciiSequenceWithSpace() {
        val encoded = Bocu1.encode("A B")
        assertArrayEquals(byteArrayOf(0x91.toByte(), 0x20, 0x92.toByte()), encoded)
    }

    @Test
    fun encodeControlsDirect() {
        assertArrayEquals(byteArrayOf(0x0A), Bocu1.encode("\n"))
        assertArrayEquals(byteArrayOf(0x20), Bocu1.encode(" "))
    }

    @Test
    fun decodeAsciiSequence() {
        val decoded = Bocu1.decode(byteArrayOf(0x91.toByte(), 0x92.toByte()))
        assertEquals("AB", decoded)
    }

    @Test
    fun roundTripMixedContent() {
        val samples = listOf(
            "",
            "ABC",
            "A B\nC",
            "日本語",
            "あいうえお",
            "emoji: 😀😃😄",
            "mix: Englishと日本語"
        )
        for (sample in samples) {
            val encoded = Bocu1.encode(sample)
            val decoded = Bocu1.decode(encoded)
            assertEquals(sample, decoded)
        }
    }
}
