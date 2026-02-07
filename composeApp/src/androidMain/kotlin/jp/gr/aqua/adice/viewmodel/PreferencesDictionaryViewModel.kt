package jp.gr.aqua.adice.viewmodel

import androidx.lifecycle.ViewModel
import jp.gr.aqua.adice.model.DictionaryRepository

class PreferencesDictionaryViewModel : ViewModel()
{
    private val dictionaryRepository = DictionaryRepository()

    fun swap(name:String, up:Boolean) = dictionaryRepository.swap(name, up)
    fun remove(name:String) = dictionaryRepository.remove(name)
}
