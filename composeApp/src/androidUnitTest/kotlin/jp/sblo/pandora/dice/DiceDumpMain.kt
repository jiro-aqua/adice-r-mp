package jp.sblo.pandora.dice

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import java.time.OffsetDateTime

object DiceDumpMain {
    private val fileSystem = FileSystem.SYSTEM
    private val json = Json { prettyPrint = true }

    @JvmStatic
    fun main(args: Array<String>) {
        val outputDirPath = args.firstOrNull() ?: "../dictionaries/dumps"
        val outputDir = outputDirPath.toSystemPath()
        fileSystem.createDirectories(outputDir)

        val dictionaries = listOf(
            "PDEJ2005U.dic",
            "EIJI-1448.dic",
//            "WAEI-121.DIC"
        )
        val dir = dictionariesDir(dictionaries)

        dictionaries.forEach { name ->
            val path = (dir / name).toString()
            val (dice, dicNum, index) = openDictionary(path)
            try {
                index.SetSearchMax(1000)
                val dumpEntries = ArrayList<DiceIndexDumpEntry>()

                fun collectEntries(): List<DiceIndexDumpEntry> {
                    val entries = ArrayList<DiceIndexDumpEntry>()
                    fun addResult(result: IdicResult) {
                        val count = result.count
                        for (i in 0 until count) {
                            val index = result.getIndex(i) ?: continue
                            if (index.isBlank()) continue
                            entries.add(
                                DiceIndexDumpEntry(
                                    index = index,
                                    trans = result.getTrans(i) ?: "",
                                    phone = result.getPhone(i) ?: "",
                                    sample = result.getSample(i) ?: "",
                                    disp = result.getDisp(i) ?: "",
                                )
                            )
                        }
                    }

                    addResult(dice.getResult(dicNum))
                    while (dice.hasMoreResult(dicNum)) {
                        addResult(dice.getMoreResult(dicNum))
                    }
                    return entries
                }

                for (first in 'a'..'z') {
                    for (second in 'a'..'z') {
                        val word = "$first$second"
                        dice.search(dicNum, word)
                        dice.isMatch(dicNum)
                        val entries = collectEntries()
                        dumpEntries.addAll(entries)
                    }
                }

                val dump = DiceIndexDump(
                    version = 1,
                    generatedAt = OffsetDateTime.now().toString(),
                    dictionary = name,
                    entries = dumpEntries
                )

                val outputFile = outputDir / "${name}_indexdump.json"
                fileSystem.sink(outputFile).buffer().use { sink ->
                    sink.writeUtf8(json.encodeToString(dump))
                }
            } finally {
                closeDictionary(dice, index)
            }
        }
    }

    private fun dictionariesDir(dictionaries: List<String>): Path {
        val moduleDir = (System.getProperty("user.dir") ?: ".").toSystemPath()
        val fromModule = moduleDir / "dictionaries"
        val fromRoot = (moduleDir.parent ?: moduleDir) / "dictionaries"
        return when {
            dictionaries.any { fileSystem.exists(fromModule / it) } -> fromModule
            dictionaries.any { fileSystem.exists(fromRoot / it) } -> fromRoot
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

    private fun String.toSystemPath(): Path = replace('\\', '/').toPath()

}

@Serializable
data class DiceIndexDump(
    val version: Int,
    val generatedAt: String,
    val dictionary: String,
    val entries: List<DiceIndexDumpEntry>
)

@Serializable
data class DiceIndexDumpEntry(
    val index: String,
    val trans: String,
    val phone: String,
    val sample: String,
    val disp: String,
)
