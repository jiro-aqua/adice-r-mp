package jp.gr.aqua.adice.model

data class Settings(
    var normalize: Boolean = false
)

data class DictionarySettings(
    val dicname: String,
    val english : Boolean,
    val use : Boolean,
    val resultNum : Int
)

interface PreferenceRepository {
    fun dictionaryPreferenceName(name:String): String

    fun readDictionarySettings(name:String) : DictionarySettings

    fun setDefaultSettings(name : String, defname: String, english: Boolean)

    fun readGeneralSettings(): Settings

    fun setNormalize(normalize: Boolean)

    fun getDics(): String

    fun writeDics(filenames: List<String>)

    fun getDicName(name:String): String?

    fun updateDictionarySettings(name: String, settings: DictionarySettings)

    fun removeDic(name : String)

    fun isVersionUp(): Boolean
}

