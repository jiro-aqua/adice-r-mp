package jp.sblo.pandora.dice

import kotlin.collections.HashMap

interface Idice {
    fun open(filename: String): IdicInfo?
    val dicNum: Int
    fun isEnable(num: Int): Boolean
    fun search(num: Int, word: String)
    fun isMatch(num: Int): Boolean
    fun getResult(num: Int): IdicResult
    fun getMoreResult(num: Int): IdicResult
    fun hasMoreResult(num: Int): Boolean
    fun close(info: IdicInfo)
    fun getDicInfo(num: Int): IdicInfo
    fun getDicInfo(filename: String): IdicInfo
    fun setIrreg(irreg: HashMap<String, String>)
    fun swap(info: IdicInfo, dir: Int)
}
