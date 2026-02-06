package jp.sblo.pandora.dice

class Element {
    lateinit var mDic: IdicInfo
    var mAttr: Byte = 0
    var mIndex: String = ""
    var mDisp: String = ""
    var mTrans: String = ""
    var mSample: String = ""
    var mPhone: String = ""

    constructor(parent: IdicInfo) {
        mDic = parent
    }

    constructor()
}
