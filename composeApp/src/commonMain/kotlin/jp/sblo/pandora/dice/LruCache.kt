package jp.sblo.pandora.dice

internal class LruCache<K, V>(private val maxSize: Int) {
    private class Node<K, V>(
        val key: K,
        var value: V
    ) {
        var prev: Node<K, V>? = null
        var next: Node<K, V>? = null
    }

    private val entries: MutableMap<K, Node<K, V>> = HashMap()
    private var newest: Node<K, V>? = null
    private var oldest: Node<K, V>? = null

    init {
        require(maxSize > 0) { "maxSize must be > 0" }
    }

    operator fun get(key: K): V? {
        val node = entries[key] ?: return null
        moveToNewest(node)
        return node.value
    }

    operator fun set(key: K, value: V) {
        val existing = entries[key]
        if (existing != null) {
            existing.value = value
            moveToNewest(existing)
            return
        }

        val node = Node(key, value)
        entries[key] = node
        linkAsNewest(node)
        trimToSize()
    }

    private fun trimToSize() {
        while (entries.size > maxSize) {
            val eldest = oldest ?: return
            unlink(eldest)
            entries.remove(eldest.key)
        }
    }

    private fun moveToNewest(node: Node<K, V>) {
        if (node === newest) return
        unlink(node)
        linkAsNewest(node)
    }

    private fun linkAsNewest(node: Node<K, V>) {
        node.prev = newest
        node.next = null
        newest?.next = node
        newest = node
        if (oldest == null) {
            oldest = node
        }
    }

    private fun unlink(node: Node<K, V>) {
        val prev = node.prev
        val next = node.next

        if (prev != null) {
            prev.next = next
        } else if (oldest === node) {
            oldest = next
        }

        if (next != null) {
            next.prev = prev
        } else if (newest === node) {
            newest = prev
        }

        node.prev = null
        node.next = null
    }
}
