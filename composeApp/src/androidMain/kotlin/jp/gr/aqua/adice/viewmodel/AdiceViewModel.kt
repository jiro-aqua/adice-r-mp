package jp.gr.aqua.adice.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import jp.gr.aqua.adice.model.ResultModel
import jp.gr.aqua.adice.model.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdiceViewModel(application: Application) : AndroidViewModel(application) {
    private val searchRepository = SearchRepository()

    data class MainUiState(
        val searchWord: String = "",
        val results: List<ResultModel> = emptyList(),
        val resetScroll: Boolean = false,
        val loseFocus: Boolean = false
    )

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            searchRepository.initialize()
        }
    }

    fun startPage() {
        viewModelScope.launch(Dispatchers.IO) {
            searchRepository.startPage().let { results ->
                _uiState.value = _uiState.value.copy(
                    results = results,
                    resetScroll = true,
                    loseFocus = false
                )
            }
        }
    }

    fun search(text: String, loseFocus: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(searchWord = text)
            searchRepository.search(text)?.let { results ->
                _uiState.value = _uiState.value.copy(
                    results = results,
                    resetScroll = true,
                    loseFocus = loseFocus
                )
            }
        }
    }

    fun updateSearchWord(text: String) {
        _uiState.value = _uiState.value.copy(searchWord = text)
    }

    fun more(position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            searchRepository.more(_uiState.value.results, position)?.let { results ->
                _uiState.value = _uiState.value.copy(
                    results = results,
                    resetScroll = false,
                    loseFocus = false
                )
            }
        }
    }

    fun clearScrollFlag() {
        _uiState.value = _uiState.value.copy(resetScroll = false)
    }

    fun clearFocusFlag() {
        _uiState.value = _uiState.value.copy(loseFocus = false)
    }

    fun updateScreenSize(isLargeScreen: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            searchRepository.updateScreenSize(isLargeScreen)
        }
    }

    fun onResume() = searchRepository.applySettings()
    fun pushHistory() = searchRepository.pushHistory()
    fun popHistory(): CharSequence? = searchRepository.popHistory()
}
