package jp.gr.aqua.adice.model

import android.graphics.Typeface

data class ResultModel(
        val mode: Mode,
        val dic: Int,
        val index: CharSequence? = null,
        val phone: CharSequence? = null,
        val trans: CharSequence? = null,
        val sample: CharSequence? = null,

        val indexFont: Typeface? = null,
        val phoneFont: Typeface? = null,
        val transFont: Typeface? = null,
        val sampleFont: Typeface? = null,
        val indexSize: Int = 0,
        val phoneSize: Int = 0,
        val transSize: Int = 0,
        val sampleSize: Int = 0
        ) {
    private companion object {
        val EIJIRO_LINK_REGEX = Regex("<(→(.+?))>")
        val WAEI_LINK_REGEX = Regex("(→　(.+))")
        val RYAKUJIRO_LINK_REGEX = Regex("(＝(.+))●")
        val PAST_TENSE_REGEX = Regex("(《動》(.+)(の過去形|の過去分詞形))")
        val SYNONYM_REGEX = Regex("(【([同|類])】([a-zA-Z; ]+))")
        val CONJUGATION_REGEX = Regex("(【変化】《動》([a-zA-Z| ]+))")
        val UK_LINK_REGEX = Regex("(〈英〉→(.+))")
    }

    fun allText() : String{
        val all = StringBuilder()
        all.append(index!!.toString())
        trans?.let{
            all.append("\n")
            all.append(it)
        }
        sample?.let{
            all.append("\n")
            all.append(it)
        }
        all.append("\n")
        return all.toString()
    }

    fun links(): Pair<Array<String>,Array<String>>
    {
        val items = ArrayList<String>()
        val disps = ArrayList<String>()

        trans?.let{
            trans->
            val transText = trans.toString()
            // <→リンク> 英辞郎形式
            run {
                EIJIRO_LINK_REGEX.findAll(transText).forEach { match ->
                    disps.add(match.groupValues[1])
                    items.add(match.groupValues[2])
                }
            }
            // "→　" 和英辞郎形式
            run {
                WAEI_LINK_REGEX.findAll(transText).forEach { match ->
                    disps.add(match.groupValues[1])
                    items.add(match.groupValues[2])
                }
            }

            // "＝リンク●" 略辞郎形式
            run {
                RYAKUJIRO_LINK_REGEX.findAll(transText).forEach { match ->
                    disps.add(match.groupValues[1])
                    val item = match.groupValues[2]
                    if ( item.contains(";") ){
                        val split = item.split(";")
                        items.add(split[0])
                    }else{
                        items.add(item)
                    }
                }
            }
            // 過去形・過去分詞形　英辞郎形式
            run {
                PAST_TENSE_REGEX.findAll(transText).forEach { match ->
                    disps.add(match.groupValues[1])
                    items.add(match.groupValues[2])
                }
            }
            // 【同】　英辞郎形式
            run {
                SYNONYM_REGEX.findAll(transText).forEach { match ->
                    val title = match.groupValues[2]
                    val synonymous = match.groupValues[3]
                    val splited = synonymous.split(";")
                    splited.forEach{
                        disps.add("【$title】$it")
                        items.add(it)
                    }
                }
            }
            // 【変化】　英辞郎形式
            run {
                CONJUGATION_REGEX.findAll(transText).forEach { match ->
                    val synonymous = match.groupValues[2]
                    val splited = synonymous.split("|")
                    splited.forEach{
                        disps.add("【変化】$it")
                        items.add(it)
                    }
                }
            }
            // 〈英〉→　英辞郎形式
            run {
                UK_LINK_REGEX.findAll(transText).forEach { match ->
                    disps.add(match.groupValues[1])
                    items.add(match.groupValues[2])
                }
            }
        }
        return disps.toTypedArray() to items.toTypedArray()
    }

    enum class Mode(val mode:Int) {
        WORD(0),
        MORE(1),
        NONE(2),
        NORESULT(3),
        FOOTER(4)
    }
}
