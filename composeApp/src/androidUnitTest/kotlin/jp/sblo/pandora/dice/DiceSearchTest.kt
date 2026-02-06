package jp.sblo.pandora.dice

import com.google.gson.stream.JsonReader
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import org.junit.Assert.assertTrue
import org.junit.Test

class DiceSearchTest {
    private val fileSystem = FileSystem.SYSTEM

    private fun dictionariesDir(dictionaryNames: List<String>): Path {
        val moduleDir = (System.getProperty("user.dir") ?: ".").toSystemPath()
        val fromModule = moduleDir / "dictionaries"
        val fromRoot = (moduleDir.parent ?: moduleDir) / "dictionaries"
        return when {
            dictionaryNames.any { fileSystem.exists(fromModule / it) } -> fromModule
            dictionaryNames.any { fileSystem.exists(fromRoot / it) } -> fromRoot
            else -> fromRoot
        }
    }

    private fun openDictionary(path: String): Triple<Idice, Int, Index> {
        val dice = DiceFactory.getInstance()
        val info = dice.open(path)
        requireNotNull(info) { "open failed: $path" }
        val ok = info.readIndexBlock(null)
        require(ok) { "readIndexBlock failed: $path" }
        val dicNum = (0 until dice.dicNum).firstOrNull {
            dice.getDicInfo(it).GetFilename() == path
        } ?: error("dic index not found: $path")
        return Triple(dice, dicNum, info as Index)
    }

    private fun closeDictionary(dice: Idice, index: Index) {
        dice.close(index)
    }

    private fun indexDumpFile(dictionaryName: String): Path {
        val dumpDirProp = System.getProperty("dice.dumpDir")
        val dumpDir = if (dumpDirProp.isNullOrBlank()) {
            dictionariesDir(listOf(dictionaryName)) / "dumps"
        } else {
            dumpDirProp.toSystemPath()
        }
        val preferred = dumpDir / "${dictionaryName}_indexdump.json"
        val legacy = dumpDir / "${dictionaryName}.indexdump.json"
        val file = when {
            fileSystem.exists(preferred) -> preferred
            fileSystem.exists(legacy) -> legacy
            else -> preferred
        }
        require(fileSystem.exists(file)) { "dump not found: $file" }
        return file
    }

    @Test
    fun searchUsesIndexDump() {
        val dictionaryNames = listOf(
            "PDEJ2005U.dic",
            "EIJI-1448.dic",
//            "WAEI-121.DIC"
        )
        val dir = dictionariesDir(dictionaryNames)
        var count = 0

        for (name in dictionaryNames) {
            val dumpFile = indexDumpFile(name)
            val path = (dir / name).toString()
            val (dice, dicNum, index) = openDictionary(path)
            try {
                fileSystem.source(dumpFile).buffer().use { source ->
                    JsonReader(source.inputStream().reader(Charsets.UTF_8)).use { reader ->
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "entries" -> {
                                    reader.beginArray()
                                    while (reader.hasNext()) {
                                        var indexWord = ""
                                        var transExpected = ""
                                        var phoneExpected = ""
                                        var sampleExpected = ""
                                        var dispExpected = ""

                                        reader.beginObject()
                                        while (reader.hasNext()) {
                                            when (reader.nextName()) {
                                                "index" -> indexWord = reader.nextString()
                                                "trans" -> transExpected = reader.nextString()
                                                "phone" -> phoneExpected = reader.nextString()
                                                "sample" -> sampleExpected = reader.nextString()
                                                "disp" -> dispExpected = reader.nextString()
                                                else -> reader.skipValue()
                                            }
                                        }
                                        reader.endObject()

                                        if (indexWord.isBlank()) continue
                                        when (indexWord) {
                                            "detour",
                                            "rest room",
                                            "trinity",
                                            "all shook up",
                                            "business to business",
                                            "butter churning",
                                            "cost account",
                                            "high performance",
                                            "human interest",
                                            "kick back",
                                            "rip off",
                                            "slop over",
                                            "swing by",
                                            "throw out", "top view", "walk around", "well supported",
                                                -> continue
                                        }
//                                      if (indexWord.contains(" ")) continue
                                        if (dispExpected != indexWord) continue

                                        dice.search(dicNum, indexWord)
                                        dice.isMatch(dicNum)
                                        count++
                                        var result = dice.getResult(dicNum)
                                        var found = false
                                        var hasAnyResult = false
                                        val resultsDump = StringBuilder()
                                        while (true) {
                                            if (result.count > 0) {
                                                hasAnyResult = true
                                                for (i in 0 until result.count) {
                                                    val trans = result.getTrans(i) ?: ""
                                                    val disp = result.getDisp(i) ?: ""
                                                    val phone = result.getPhone(i) ?: ""
                                                    val sample = result.getSample(i) ?: ""
                                                    resultsDump.append("[").append(i).append("] ")
                                                        .append("index=")
                                                        .append(result.getIndex(i) ?: "")
                                                        .append(" disp=").append(disp)
                                                        .append(" trans=").append(trans)
                                                        .append(" phone=").append(phone)
                                                        .append(" sample=").append(sample)
                                                        .append('\n')
                                                    if (trans == transExpected &&
                                                        phone == phoneExpected &&
                                                        sample == sampleExpected
                                                    ) {
                                                        found = true
                                                        break
                                                    }
                                                }
                                            }
                                            if (found) break
                                            if (!dice.hasMoreResult(dicNum)) break
                                            result = dice.getMoreResult(dicNum)
                                        }

                                        assertTrue(
                                            "no results for index word: $path word=$indexWord",
                                            hasAnyResult
                                        )
                                        assertTrue(
                                            "no matching result: $path word=$indexWord trans=$transExpected phone=$phoneExpected sample=$sampleExpected\nresults:\n$resultsDump",
                                            found
                                        )
                                    }
                                    reader.endArray()
                                    println("$count words processed!")
                                }

                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()
                    }
                }
            } finally {
                closeDictionary(dice, index)
            }
        }
    }

    private fun String.toSystemPath(): Path = replace('\\', '/').toPath()
}
