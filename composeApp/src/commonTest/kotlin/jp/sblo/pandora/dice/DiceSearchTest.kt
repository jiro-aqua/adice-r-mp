package jp.sblo.pandora.dice

import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.PushbackReader

class DiceSearchTest {
    private val fileSystem = FileSystem.SYSTEM
    private val json = Json { ignoreUnknownKeys = true }

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
                // テスト用のファイルが大きいためにOOMが出るので、順次処理を行う
                forEachDumpEntry(dumpFile) { entry ->
                    val indexWord = entry.index
                    val transExpected = entry.trans
                    val phoneExpected = entry.phone
                    val sampleExpected = entry.sample
                    val dispExpected = entry.disp

                    if (indexWord.isBlank()) return@forEachDumpEntry
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
                            -> return@forEachDumpEntry
                    }
//                  if (indexWord.contains(" ")) return@forEachDumpEntry
                    if (dispExpected != indexWord) return@forEachDumpEntry

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
                                val trans = result.getTrans(i)
                                val disp = result.getDisp(i)
                                val phone = result.getPhone(i)
                                val sample = result.getSample(i)
                                resultsDump.append("[").append(i).append("] ")
                                    .append("index=")
                                    .append(result.getIndex(i))
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
                println("$count words processed!")
            } finally {
                closeDictionary(dice, index)
            }
        }
    }

    private fun forEachDumpEntry(
        dumpFile: Path,
        onEntry: (DiceIndexDumpEntry) -> Unit
    ) {
        fileSystem.source(dumpFile).buffer().inputStream().reader(Charsets.UTF_8)
            .use { baseReader ->
                val reader = PushbackReader(baseReader, 2)

                expectChar(reader, '{')
                while (true) {
                    when (val next = readNonWhitespace(reader)) {
                        '}'.code -> return
                        '"'.code -> {
                            val key = readJsonString(reader)
                            expectChar(reader, ':')
                            if (key == "entries") {
                                readEntriesArray(reader, onEntry)
                            } else {
                                skipJsonValue(reader)
                            }
                        }

                        else -> error("invalid root object in dump: expected key, got '${next.toChar()}'")
                    }

                    when (val sep = readNonWhitespace(reader)) {
                        ','.code -> continue
                        '}'.code -> return
                        else -> error("invalid root object separator in dump: '${sep.toChar()}'")
                    }
                }
            }
    }

    private fun readEntriesArray(
        reader: PushbackReader,
        onEntry: (DiceIndexDumpEntry) -> Unit
    ) {
        expectChar(reader, '[')
        while (true) {
            when (val next = readNonWhitespace(reader)) {
                ']'.code -> return
                '{'.code -> {
                    val entryJson = readJsonObject(reader)
                    onEntry(json.decodeFromString(entryJson))
                }

                else -> error("invalid entries array element in dump: '${next.toChar()}'")
            }

            when (val sep = readNonWhitespace(reader)) {
                ','.code -> continue
                ']'.code -> return
                else -> error("invalid entries array separator in dump: '${sep.toChar()}'")
            }
        }
    }

    private fun readJsonObject(reader: PushbackReader): String {
        val sb = StringBuilder().append('{')
        var depth = 1
        var inString = false
        var escaped = false

        while (depth > 0) {
            val ch = readRequired(reader)
            sb.append(ch.toChar())

            if (inString) {
                if (escaped) {
                    escaped = false
                } else {
                    when (ch) {
                        '\\'.code -> escaped = true
                        '"'.code -> inString = false
                    }
                }
                continue
            }

            when (ch) {
                '"'.code -> inString = true
                '{'.code -> depth++
                '}'.code -> depth--
            }
        }

        return sb.toString()
    }

    private fun skipJsonValue(reader: PushbackReader) {
        when (val first = readNonWhitespace(reader)) {
            '"'.code -> skipJsonString(reader)
            '{'.code -> skipJsonComposite(reader, '{', '}')
            '['.code -> skipJsonComposite(reader, '[', ']')
            else -> skipJsonPrimitive(reader, first)
        }
    }

    private fun skipJsonComposite(
        reader: PushbackReader,
        open: Char,
        close: Char
    ) {
        var depth = 1
        var inString = false
        var escaped = false

        while (depth > 0) {
            val ch = readRequired(reader)
            if (inString) {
                if (escaped) {
                    escaped = false
                } else {
                    when (ch) {
                        '\\'.code -> escaped = true
                        '"'.code -> inString = false
                    }
                }
                continue
            }

            when (ch) {
                '"'.code -> inString = true
                open.code -> depth++
                close.code -> depth--
            }
        }
    }

    private fun skipJsonString(reader: PushbackReader) {
        var escaped = false
        while (true) {
            val ch = readRequired(reader)
            if (escaped) {
                escaped = false
                continue
            }
            when (ch) {
                '\\'.code -> escaped = true
                '"'.code -> return
            }
        }
    }

    private fun skipJsonPrimitive(reader: PushbackReader, first: Int) {
        var ch = first
        while (ch != -1 && !isJsonDelimiter(ch)) {
            ch = reader.read()
        }
        if (ch != -1) {
            reader.unread(ch)
        }
    }

    private fun readJsonString(reader: PushbackReader): String {
        val sb = StringBuilder()
        var escaped = false
        while (true) {
            val ch = readRequired(reader)
            if (escaped) {
                sb.append(ch.toChar())
                escaped = false
                continue
            }
            when (ch) {
                '\\'.code -> escaped = true
                '"'.code -> return sb.toString()
                else -> sb.append(ch.toChar())
            }
        }
    }

    private fun expectChar(reader: PushbackReader, expected: Char) {
        val actual = readNonWhitespace(reader)
        if (actual != expected.code) {
            error("invalid dump format: expected '$expected', got '${actual.toChar()}'")
        }
    }

    private fun readNonWhitespace(reader: PushbackReader): Int {
        while (true) {
            val ch = reader.read()
            if (ch == -1 || !ch.toChar().isWhitespace()) {
                return ch
            }
        }
    }

    private fun readRequired(reader: PushbackReader): Int {
        val ch = reader.read()
        if (ch == -1) error("unexpected end of dump file")
        return ch
    }

    private fun isJsonDelimiter(ch: Int): Boolean {
        return ch == ','.code || ch == ']'.code || ch == '}'.code || ch.toChar().isWhitespace()
    }

    private fun String.toSystemPath(): Path = replace('\\', '/').toPath()
}
