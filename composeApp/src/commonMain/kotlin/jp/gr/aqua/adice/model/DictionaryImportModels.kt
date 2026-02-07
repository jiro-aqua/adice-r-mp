package jp.gr.aqua.adice.model

import okio.Source

interface PickedFileHandle {
    val displayName: String
    fun openSource(): Source
}

enum class DictionaryImportError {
    EmptyDisplayName,
    ReadFailed,
    InvalidDictionary
}

data class DictionaryImportResult(
    val success: Boolean,
    val dicName: String,
    val error: DictionaryImportError? = null
)
