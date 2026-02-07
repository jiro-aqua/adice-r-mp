package jp.gr.aqua.adice.model

import android.graphics.Typeface
import android.util.Log
import android.content.res.Configuration
import adicermp.composeapp.generated.resources.Res
import adicermp.composeapp.generated.resources.description
import adicermp.composeapp.generated.resources.resulttitlehtml
import adicermp.composeapp.generated.resources.start_footer
import adicermp.composeapp.generated.resources.start_title
import adicermp.composeapp.generated.resources.trans_text_size
import adicermp.composeapp.generated.resources.trans_text_size_large
import jp.gr.aqua.adice.BuildConfig
import jp.sblo.pandora.dice.DiceFactory
import jp.sblo.pandora.dice.IdicInfo
import jp.sblo.pandora.dice.IdicResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class SearchRepository {
    private var mInitialized = false

    private val mDice = DiceFactory.getInstance()

    private var mNormalize = true
    private var mLast: String? = ""

    private val mSearchHistory: ArrayList<CharSequence> = ArrayList()

    private lateinit var phoneticFont: Typeface
    private lateinit var mFooter: String
    private lateinit var mDescription: String
    private var transTextSize: Int = 0

    fun initialize() {
        loadResources()
        loadIrreg()
        initDice()
        Log.i(TAG, "aDice Initialized")
        mInitialized = true
    }

    suspend fun startPage(): List<ResultModel> {
        return withContext(Dispatchers.IO) {
            val result = ArrayList<ResultModel>()
            generateDisp(DISP_MODE_START, 0, null, result, -1)
            result
        }
    }

    suspend fun search(text: String): List<ResultModel>? {
        while(!mInitialized) { delay(100) }
        if (text.isEmpty()) return null
        return synchronized(this){
            val converted = if (mNormalize) DiceFactory.convert(text) else text
            if (converted.isNotEmpty() && mLast != converted) {
                val result = searchProc(converted)
                mLast = converted
                result
            } else {
                null
            }
        }
    }

    suspend fun more(currentResult: List<ResultModel>, position: Int): List<ResultModel>? {
        while(!mInitialized) { delay(100) }
        return synchronized(this){
            val result: ArrayList<ResultModel> = currentResult.toCollection(ArrayList())
            result.removeAt(position)
            val dic = result[position].dic
            val pr = mDice.getMoreResult(dic)
            generateDisp(DISP_MODE_RESULT, dic, pr, result, position)
            result
        }
    }

    fun pushHistory() {
        val last = mLast ?: ""
        if (last.isNotEmpty() && (mSearchHistory.isEmpty() || last != mSearchHistory[0])) {     // todo linkedHashMapで置き換え
            mSearchHistory.add(0, last)
        }
    }

    fun popHistory(): CharSequence? {
        return if (mSearchHistory.isNotEmpty()) {
            val cs = mSearchHistory[0]
            mSearchHistory.removeAt(0)
            cs
        } else {
            null
        }
    }

    fun applySettings() {
        mLast = ""

        val settings = PreferenceRepository().readGeneralSettings()
        mNormalize = settings.normalize

        for (i in 0 until mDice.dicNum) {
            val dicinfo = mDice.getDicInfo(i)
            val name = dicinfo.GetFilename()
            applyDictionarySettings(dicinfo, PreferenceRepository().readDictionarySettings(name))
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    private fun initDice() {
        val dicss = PreferenceRepository().getDics()
        val dics = dicss.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }

        // 外部辞書読込
        dics.forEach { name ->
            if (name.isEmpty()) {
                return@forEach
            }
            val dicinfo = mDice.open(name)
            if (dicinfo != null) {
                Log.i(TAG, "Open OK:$name")

                // インデクス作成
                if (!dicinfo.readIndexBlock(DictionaryRepository().indexCacheAccessor(name))) {
                    mDice.close(dicinfo)
                } else {
                    applyDictionarySettings(dicinfo, PreferenceRepository().readDictionarySettings(name))
                }
            } else {
                Log.i(TAG, "Open NG:$name")
            }
        }
    }

    private fun applyDictionarySettings(dicinfo: IdicInfo, settings: PreferenceRepository.DictionarySettings) {
        dicinfo.SetDicName(settings.dicname)
        dicinfo.SetEnglish(settings.english)
        dicinfo.SetNotuse(!settings.use)
        dicinfo.SetSearchMax(settings.resultNum)

        dicinfo.SetIndexSize(18)
        dicinfo.SetPhoneticSize(18)
        dicinfo.SetTransSize(18)
        dicinfo.SetSampleSize(18)
    }

    // 英語向けIRREG読込
    private fun loadIrreg() {
        val name = "files/irregdic.txt"
        val irreg = HashMap<String, String>()
        val content = runBlocking { String(Res.readBytes(name), Charsets.UTF_8) }
        content.lineSequence().forEach { rawLine ->
            val line = rawLine.trimEnd('\r')
            val s = line.split('\t', limit = 2)
            if (s.size == 2) {
                irreg[s[0]] = s[1]
            }
        }
        Log.i(TAG, "Open OK:$name")
        mDice.setIrreg(irreg)
    }

    private fun searchProc(text: String): List<ResultModel> {

        val result = ArrayList<ResultModel>()
        // Log.i("search thread ", "sleeping...");
        // Log.i("search thread ", "got up");
        val dicnum = mDice.dicNum
        for (dic in 0 until dicnum) {
            if (!mDice.isEnable(dic)) {
                continue
            }

            mDice.search(dic, text)

            val pr = mDice.getResult(dic)

            if (pr.count > 0) {
                generateDisp(DISP_MODE_RESULT, dic, pr, result, -1)
                generateDisp(DISP_MODE_FOOTER, dic, null, result, -1)
            }
        }
        if (result.size == 0) {
            generateDisp(DISP_MODE_NORESULT, -1, null, result, -1)
        }
        return result
    }

    private fun generateDisp(mode: Int, dic: Int, pr: IdicResult?, result: ArrayList<ResultModel>, _pos: Int) {
        var pos = _pos
        synchronized(this) {
            when (mode) {
                DISP_MODE_RESULT -> {
                    // 表示させる内容を生成
                    for (i in 0 until pr!!.count) {
                        val idx = pr.getDisp(i)
                        val index = if (idx.isEmpty()) {
                            pr.getIndex(i)
                        }else{
                            idx
                        }
                        val info = mDice.getDicInfo(dic)

                        val data = ResultModel(mode=ResultModel.Mode.WORD, dic=dic,
                                index = index,
                                phone = pr.getPhone(i),
                                trans = pr.getTrans(i).replace(Regex(",《"),"\n《"),     // 英辞郎144.8用パッチ("treat")
                                sample = pr.getSample(i),

                                indexSize = info.GetIndexSize(),
                                phoneSize = info.GetPhoneticSize(),
                                transSize = transTextSize,
                                sampleSize = info.GetSampleSize(),

                                indexFont = null,
                                phoneFont = phoneticFont,
                                transFont = null,
                                sampleFont = null
                                )

                        if (pos == -1) {
                            result.add(data)
                        } else {
                            result.add(pos++, data)
                        }
                    }

                    // 結果がまだあるようならmoreボタンを表示
                    if (mDice.hasMoreResult(dic)) {
                        val data = ResultModel(mode=ResultModel.Mode.MORE, dic=dic)

                        if (pos == -1) {
                            result.add(data)
                        } else {
                            result.add(pos++, data)
                        }
                    }
                }
                DISP_MODE_FOOTER -> {
                    var dicname: String = mDice.getDicInfo(dic).GetDicName()
                    if (dicname.isEmpty()) {
                        dicname = mDice.getDicInfo(dic).GetFilename()
                    }
                    val data = ResultModel(mode=ResultModel.Mode.FOOTER, dic=dic,
                            index = String.format(mFooter, dicname),
                            indexSize = 16)

                    if (pos == -1) {
                        result.add(data)
                    } else {
                        result.add(pos++, data)
                    }
                }
                DISP_MODE_NORESULT -> {
                    val data = ResultModel(mode=ResultModel.Mode.NORESULT, dic=0,
                            indexSize = 16)
                    result.add(data)
                }
                DISP_MODE_START -> {
                    val versionName = BuildConfig.VERSION_NAME
                    val versionCode = BuildConfig.VERSION_CODE
                    val version = "Ver. " + String.format("%s (%d)", versionName, versionCode)
                    val title = runBlocking { getString(Res.string.start_title) }
                    val footer = runBlocking { getString(Res.string.start_footer) }
                    val data = ResultModel(
                        mode = ResultModel.Mode.NONE,
                        dic = 0,
                        index = title,
                        phone = version,
                        trans = mDescription,
                        sample = footer,
                        indexSize = 36,
                        phoneSize = 16,
                        transSize = 16,
                        sampleSize = 14
                    )
                    result.add(data)
                }
            }
            Unit
        }
    }

    private fun loadResources() {
        mFooter = runBlocking { getString(Res.string.resulttitlehtml) }
        mDescription = runBlocking { getString(Res.string.description) }
        phoneticFont = Typeface.createFromAsset(ContextModel.assets, "DoulosSILR.ttf")
        val screenLayout = ContextModel.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val transTextSizeRes: StringResource = if (screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
            Res.string.trans_text_size_large
        } else {
            Res.string.trans_text_size
        }
        transTextSize = runBlocking { getString(transTextSizeRes) }.toIntOrNull() ?: 16
    }

    companion object {
        private const val TAG = "aDiceVM"

        private const val DISP_MODE_RESULT = 0
        private const val DISP_MODE_MORE = 1
        private const val DISP_MODE_FOOTER = 2
        private const val DISP_MODE_NORESULT = 3
        private const val DISP_MODE_START = 4
    }


}
