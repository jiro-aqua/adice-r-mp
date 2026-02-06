package jp.sblo.pandora.dice

import okio.BufferedSink
import okio.BufferedSource

interface IIndexCacheFile {
    fun getInput(): BufferedSource

    fun getOutput(): BufferedSink
}
