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
import java.util.prefs.Preferences
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class PreferenceRepositoryJvmImpl : PreferenceRepository, KoinComponent {

    private val sp: Preferences = Preferences.userRoot().node(PREF_ROOT)
    private val contextModel: ContextModel by inject()

    override fun dictionaryPreferenceName(name: String): String = name.replace("/", "_").replace("\\", "_")
    private fun dictionaryPreference(name: String): Preferences = sp.node("dic/" + dictionaryPreferenceName(name))

    override fun readDictionarySettings(name: String): DictionarySettings {
        val dicsp = dictionaryPreference(name)
        return DictionarySettings(
            dicname = dicsp.get(KEY_DICNAME, ""),
            english = dicsp.getBoolean(KEY_ENGLISH, false),
            use = dicsp.getBoolean(KEY_USE, false),
            resultNum = dicsp.getInt(KEY_RESULTNUM, 30)
        )
    }

    override fun setDefaultSettings(name: String, defname: String, english: Boolean) {
        val dicsp = dictionaryPreference(name)
        if (dicsp.get(KEY_DICNAME, "").isEmpty()) {
            dicsp.put(KEY_DICNAME, defname)
            dicsp.putBoolean(KEY_ENGLISH, english)
            dicsp.putBoolean(KEY_USE, true)
            dicsp.putInt(KEY_RESULTNUM, 30)

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
                    dicsp.put(KEY_DICNAME, dicname)
                    dicsp.putBoolean(KEY_ENGLISH, DICNTEMPLATE[i].englishFlag)
                }
            }
        }
    }

    override fun readGeneralSettings(): Settings {
        return Settings(
            normalize = sp.getBoolean(KEY_NORMALIZE_SEARCH, true)
        )
    }

    override fun setNormalize(normalize: Boolean) {
        sp.putBoolean(KEY_NORMALIZE_SEARCH, normalize)
    }

    override fun getDics(): String {
        return sp.get(KEY_DICS, "")
    }

    override fun writeDics(filenames: List<String>) {
        val dics = filenames.fold(StringBuilder()) { acc, s -> acc.append("$s|") }
        sp.put(KEY_DICS, dics.toString())
    }

    override fun getDicName(name: String): String? {
        val dicsp = dictionaryPreference(name)
        return dicsp.get(KEY_DICNAME, name)
    }

    override fun updateDictionarySettings(name: String, settings: DictionarySettings) {
        val dicsp = dictionaryPreference(name)
        dicsp.put(KEY_DICNAME, settings.dicname)
        dicsp.putBoolean(KEY_ENGLISH, settings.english)
        dicsp.putBoolean(KEY_USE, settings.use)
        dicsp.putInt(KEY_RESULTNUM, settings.resultNum)
    }

    override fun removeDic(name: String) {
        runCatching {
            dictionaryPreference(name).removeNode()
        }
    }

    private fun getStringByResource(resource: StringResource, vararg args: Any): String {
        return runBlocking {
            getString(resource, *args)
        }
    }

    override fun isVersionUp(): Boolean {
        val lastVersion = sp.getInt(KEY_LASTVERSION, 0)
        val versioncode = contextModel.versionCode
        if (lastVersion == 0) {
            sp.putBoolean(KEY_NORMALIZE_SEARCH, true)
        }

        if (lastVersion != versioncode) {
            sp.putInt(KEY_LASTVERSION, versioncode)
            return true
        }
        return false
    }

    companion object {
        private const val PREF_ROOT = "jp.gr.aqua.adice"

        const val KEY_DICS = "dics"
        private const val KEY_NORMALIZE_SEARCH = "normalizesearch"
        private const val KEY_LASTVERSION = "LastVersion"

        const val KEY_DICNAME = "dicname"
        const val KEY_USE = "use"
        const val KEY_ENGLISH = "english"
        const val KEY_RESULTNUM = "resultnum"

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
