package jp.sblo.pandora.dice

class BlockCache {
    private val cache = LruCache<Int, ByteArray>(CACHESIZE)

    fun getBuff(key: Int): ByteArray? {
        return cache[key]
    }

    fun putBuff(key: Int, data: ByteArray) {
        cache[key] = data
    }

    private companion object {
        private const val CACHESIZE = 1000
    }
}
