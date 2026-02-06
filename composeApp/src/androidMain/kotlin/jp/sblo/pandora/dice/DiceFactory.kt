package jp.sblo.pandora.dice

object DiceFactory {
    private var mDice: Idice? = null

    fun getInstance(): Idice {
        if (mDice == null) {
            mDice = Dice()
        }
        return mDice!!
    }

    private fun trimchar(input: CharSequence, patterna: CharSequence, patternb: CharSequence): CharSequence {
        val len = input.length
        val patlena = patterna.length
        val patlenb = patternb.length
        var s = 0
        var e = len - 1

        // 行頭から
        while (s < len) {
            var found = false
            val c = input[s]
            var j = 0
            while (j < patlena) {
                if (c == patterna[j]) {
                    found = true
                    break
                }
                j++
            }
            if (!found) {
                break
            }
            s++
        }

        // 行末から
        while (e > s) {
            var found = false
            val c = input[e]
            var j = 0
            while (j < patlenb) {
                if (c == patternb[j]) {
                    found = true
                    break
                }
                j++
            }
            if (!found) {
                break
            }
            e--
        }

        return input.subSequence(s, e + 1)
    }

    fun convert(s: CharSequence): String {
        val dakuon =
            "、･-ガギグゲゴザジズゼゾダヂヅデドバビブベボパピプペポァィゥェォャュョッÀÁÂÃÄÅàáâãäåÆæÇçÈÉÊËèéêëÌÍÎÏìíîïÐðÑñÒÓÔÕÖØòóôõöøŒœÙÚÛÜùúûüÝÞýþÿß"
        val seion =
            ",・ カキクケコサシスセソタチツテトハヒフヘホハヒフヘホアイウエオヤユヨツaaaaaaaaaaaaaacceeeeeeeeiiiiiiiiddnnoooooooooooooouuuuuuuuyyyyys"

        val trimmed = trimchar(s, " 　", " 　\"'?.,()[]{}|!")
        val cs = trimmed.toString().toCharArray()

        for (j in cs.indices) {
            for (i in dakuon.indices) {
                if (cs[j] == dakuon[i]) {
                    cs[j] = seion[i]
                }
            }
        }
        return String(cs).lowercase().replace("'", "").replace("  ", " ")
    }
}
