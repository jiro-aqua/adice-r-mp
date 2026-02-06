package jp.sblo.pandora.dice

data class DiceDump(
    val version: Int,
    val generatedAt: String,
    val dictionaries: List<DiceDumpDictionary>
)

data class DiceDumpDictionary(
    val name: String,
    val entries: List<DiceDumpEntry>
)

data class DiceDumpEntry(
    val entry: Int,
    val word: String,
    val trans: String
)
