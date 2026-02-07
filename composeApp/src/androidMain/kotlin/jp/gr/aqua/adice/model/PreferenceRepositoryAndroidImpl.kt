package jp.gr.aqua.adice.model

import adicermp.composeapp.generated.resources.Res
import adicermp.composeapp.generated.resources.dicname_edict
import adicermp.composeapp.generated.resources.dicname_eijiro
import adicermp.composeapp.generated.resources.dicname_ichirofj
import adicermp.composeapp.generated.resources.dicname_pdej
import adicermp.composeapp.generated.resources.dicname_reijiro
import adicermp.composeapp.generated.resources.dicname_ryakujiro
import adicermp.composeapp.generated.resources.dicname_waeijiro
import adicermp.composeapp.generated.resources.dicname_webster
import android.content.Context
import androidx.preference.PreferenceManager
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class PreferenceRepositoryAndroidImpl(private val context: Context) : PreferenceRepository{

    private val sp = PreferenceManager.getDefaultSharedPreferences(context)

    override fun dictionaryPreferenceName(name:String) = name.replace("/","_")
    private fun dictionaryPreference(name:String) = context.getSharedPreferences(dictionaryPreferenceName(name), Context.MODE_PRIVATE)


    override fun readDictionarySettings(name:String) : DictionarySettings
    {
        val dicsp = dictionaryPreference(name)

        return DictionarySettings(
            dicname = dicsp.getString(KEY_DICNAME, "")!!,
            english = dicsp.getBoolean(KEY_ENGLISH, false),
            use = dicsp.getBoolean(KEY_USE, false),
            resultNum = Integer.parseInt(dicsp.getString(KEY_RESULTNUM, "30")!!)
        )
    }

    override fun setDefaultSettings(name : String, defname: String, english: Boolean) {

        val dicsp = dictionaryPreference(name)

        // 名称未設定の時はデフォルトに戻す
        if (dicsp.getString(KEY_DICNAME, "")!!.isEmpty()) {
            dicsp.edit().apply{
                putString(KEY_DICNAME, defname)
                putBoolean(KEY_ENGLISH, english)
                putBoolean(KEY_USE, true)
                putString(KEY_RESULTNUM, "30")

                // 辞書名自動判定
                for (i in DICNTEMPLATE.indices) {
                    val match = Regex(
                        pattern = DICNTEMPLATE[i].pattern,
                        option = RegexOption.IGNORE_CASE
                    ).find(name)
                    if (match != null) {
                        val dicname = if (match.groupValues.size > 1) {
                            val edt = match.groupValues[1]
                            getStringByResource(DICNTEMPLATE[i].resourceDicname, edt)
                        } else {
                            getStringByResource(DICNTEMPLATE[i].resourceDicname)
                        }
                        putString(KEY_DICNAME, dicname)
                        putBoolean(KEY_ENGLISH, DICNTEMPLATE[i].englishFlag)
                    }
                }
                apply()
            }
        }
    }

    override fun readGeneralSettings(): Settings {
        return Settings(
            normalize = sp.getBoolean(KEY_NORMALIZE_SEARCH, true)
        )
    }

    override fun setNormalize(normalize: Boolean) {
        sp.edit().putBoolean(KEY_NORMALIZE_SEARCH, normalize).apply()
    }

    override fun getDics(): String {
        return sp.getString(KEY_DICS, "")!!
    }

    override fun writeDics(filenames: List<String>) {
        val dics = filenames.fold(StringBuilder()) { acc, s -> acc.append("$s|") }
        sp.edit().putString(KEY_DICS, dics.toString()).apply()
    }

    override fun getDicName(name:String): String? {
        val dicsp = dictionaryPreference(name)
        return dicsp.getString(KEY_DICNAME, name)
    }

    override fun updateDictionarySettings(name: String, settings: DictionarySettings) {
        val dicsp = dictionaryPreference(name)
        dicsp.edit().apply {
            putString(KEY_DICNAME, settings.dicname)
            putBoolean(KEY_ENGLISH, settings.english)
            putBoolean(KEY_USE, settings.use)
            putString(KEY_RESULTNUM, settings.resultNum.toString())
            apply()
        }
    }

    override fun removeDic(name : String){
        val dicsp = dictionaryPreference(name)
        dicsp.edit().clear().apply()
    }

    private fun getStringByResource(resource: StringResource, vararg args: Any): String {
        return runBlocking {
            getString(resource, *args)
        }
    }

    override fun isVersionUp(): Boolean {
        val lastVersion= sp.getInt(KEY_LASTVERSION, 0)
        val versioncode= ContextModel.versionCode
        if ( lastVersion == 0 ){
            sp.edit()
                .putBoolean(KEY_NORMALIZE_SEARCH, true)
                .apply()
        }

        if (lastVersion != versioncode) {
            sp.edit()
                .putInt(KEY_LASTVERSION, versioncode)
                .apply()
            return true
        }
        return false
    }


    companion object {
        const val KEY_DICS = "dics"

        const val KEY_ADD_DICTIONARY = "AddDictionary"
        const val KEY_DOWNLOAD_DICTIONARY = "DownloadDictionary"
        const val KEY_PROGRESS = "progress"
        private const val KEY_NORMALIZE_SEARCH = "normalizesearch"
        private const val KEY_LASTVERSION = "LastVersion"

        const val KEY_DICNAME = "|dicname"
        const val KEY_FILENAME = "|filename"
        const val KEY_USE = "|use"
        const val KEY_ENGLISH = "|english"
        const val KEY_RESULTNUM = "|resultnum"
        const val KEY_MOVE_UP = "|MoveUp"
        const val KEY_MOVE_DOWN = "|MoveDown"
        const val KEY_REMOVE = "|Remove"


        internal class DicTemplate(
            var pattern: String,
            var resourceDicname: StringResource,
            var englishFlag: Boolean
        )

        private val DICNTEMPLATE = arrayOf(
            DicTemplate("/EIJI[a-zA-Z]*-([0-9]+)U?.*\\.DIC", Res.string.dicname_eijiro, true),
            DicTemplate("/WAEI-([0-9]+)U?.*\\.DIC", Res.string.dicname_waeijiro, false),
            DicTemplate("/REIJI([0-9]+)U?.*\\.DIC", Res.string.dicname_reijiro, false),
            DicTemplate("/RYAKU([0-9]+)U?.*\\.DIC", Res.string.dicname_ryakujiro, false),
            DicTemplate("/PDEJ2005U?.dic", Res.string.dicname_pdej, true),
            DicTemplate("/PDEDICTU?.dic", Res.string.dicname_edict, false),
            DicTemplate("/PDWD1913U?.dic", Res.string.dicname_webster, true),
            DicTemplate("/f2jdic.dic", Res.string.dicname_ichirofj, false)
        )
    }
}
