package jp.gr.aqua.adice.viewmodel

import androidx.lifecycle.ViewModel
import jp.gr.aqua.adice.model.DictionaryRepository

class PreferencesDictionaryViewModel(
    private val dictionaryRepository: DictionaryRepository
) : ViewModel() {

    fun swap(name:String, up:Boolean) = dictionaryRepository.swap(name, up)
    fun remove(name:String) = dictionaryRepository.remove(name)
}
