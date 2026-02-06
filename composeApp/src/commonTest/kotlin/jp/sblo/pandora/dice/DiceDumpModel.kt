package jp.sblo.pandora.dice

import kotlinx.serialization.Serializable

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
