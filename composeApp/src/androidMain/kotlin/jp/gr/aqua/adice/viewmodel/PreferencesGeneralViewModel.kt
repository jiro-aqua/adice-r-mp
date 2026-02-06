package jp.gr.aqua.adice.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import jp.gr.aqua.adice.model.DictionaryRepository
import jp.gr.aqua.adice.model.DownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreferencesGeneralViewModel(application: Application) : AndroidViewModel(application) {
    private val downloadRepository = DownloadRepository()
    private val dictionaryRepository = DictionaryRepository()

    data class SettingsUiState(
        val isDownloading: Boolean = false,
        val completionResult: CompletionResult? = null
    )

    data class CompletionResult(
        val success: Boolean,
        val dicName: String
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun download(site: String, english: Boolean, defname: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isDownloading = true)
            val dicname = downloadRepository.downloadDicfile("http://$site")
            val result = dictionaryRepository.addDictionary(dicname, english, defname)
            _uiState.value = _uiState.value.copy(
                isDownloading = false,
                completionResult = CompletionResult(result.first, result.second)
            )
        }
    }

    fun openDictionary(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isDownloading = true)
            val result = dictionaryRepository.openDictionary(uri)
            _uiState.value = _uiState.value.copy(
                isDownloading = false,
                completionResult = CompletionResult(result.first, result.second)
            )
        }
    }

    fun clearCompletionResult() {
        _uiState.value = _uiState.value.copy(completionResult = null)
    }
}
