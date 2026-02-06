package jp.sblo.pandora.dice

import kotlin.collections.ArrayList

internal class Result : ArrayList<Element>(), IdicResult {
    override val count: Int
        get() = size

    override fun getIndex(idx: Int): String {
        return get(idx).mIndex
    }

    override fun getDisp(idx: Int): String {
        return get(idx).mDisp
    }

    override fun getAttr(idx: Int): Byte {
        return get(idx).mAttr
    }

    override fun getTrans(idx: Int): String {
        return get(idx).mTrans
    }

    override fun getPhone(idx: Int): String {
        return get(idx).mPhone
    }

    override fun getSample(idx: Int): String {
        return get(idx).mSample
    }

    override fun getDicInfo(idx: Int): IdicInfo {
        return get(idx).mDic
    }

    private companion object {
        private const val serialVersionUID = 1L
    }
}
