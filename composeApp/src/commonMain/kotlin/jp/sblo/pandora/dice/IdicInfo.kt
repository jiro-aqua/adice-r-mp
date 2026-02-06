package jp.sblo.pandora.dice

import kotlin.collections.HashMap

interface IdicInfo {
    fun GetFilename(): String
    fun GetSearchMax(): Int
    fun SetSearchMax(m: Int)
    fun SetAccent(b: Boolean)
    fun GetAccent(): Boolean
    fun SetEnglish(b: Boolean)
    fun GetEnglish(): Boolean
    fun SetNotuse(b: Boolean)
    fun GetNotuse(): Boolean
    fun SetIndexFont(b: String)
    fun GetIndexFont(): String
    fun SetIndexSize(b: Int)
    fun GetIndexSize(): Int
    fun SetTransFont(b: String)
    fun GetTransFont(): String
    fun SetTransSize(b: Int)
    fun GetTransSize(): Int
    fun SetPhonetic(b: Boolean)
    fun GetPhonetic(): Boolean
    fun SetPhoneticFont(b: String)
    fun GetPhoneticFont(): String
    fun SetPhoneticSize(b: Int)
    fun GetPhoneticSize(): Int
    fun SetSample(b: Boolean)
    fun GetSample(): Boolean
    fun SetSampleFont(b: String)
    fun GetSampleFont(): String
    fun SetSampleSize(b: Int)
    fun GetSampleSize(): Int
    fun SetDicName(b: String)
    fun GetDicName(): String
    fun setIrreg(irreg: HashMap<String, String>)
    fun getIrreg(key: String): String
    fun readIndexBlock(indexcache: IIndexCacheFile?): Boolean
}
