package jp.sblo.pandora.dice

interface IdicResult {
    val count: Int
    fun getIndex(idx: Int): String
    fun getDisp(idx: Int): String
    fun getAttr(idx: Int): Byte
    fun getTrans(idx: Int): String
    fun getPhone(idx: Int): String
    fun getSample(idx: Int): String
    fun getDicInfo(idx: Int): IdicInfo
}
