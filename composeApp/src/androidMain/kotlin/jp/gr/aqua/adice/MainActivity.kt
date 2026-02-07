package jp.gr.aqua.adice

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import jp.gr.aqua.adice.model.ContextModel
import jp.gr.aqua.adice.ui.navigation.AdiceNavHost
import jp.gr.aqua.adice.ui.theme.AdiceTheme
import jp.gr.aqua.adice.viewmodel.AdiceViewModel
import jp.gr.aqua.adice.viewmodel.PreferencesGeneralViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URLDecoder

class MainActivity : ComponentActivity() {

    private var initialText by mutableStateOf("")
    private val adiceViewModel: AdiceViewModel by viewModel()
    private val settingsViewModel: PreferencesGeneralViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ContextModel.initialize(this)

        initialText = if (savedInstanceState == null) {
            getWordFromIntent(intent)
        } else ""

        setContent {
            AdiceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdiceNavHost(
                        initialText = initialText,
                        adiceViewModel = adiceViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }

    private fun getWordFromIntent(intent: Intent?): String {
        intent?.let {
            when (intent.action) {
                Intent.ACTION_SEND -> intent.extras?.getString(Intent.EXTRA_TEXT)
                Intent.ACTION_SEARCH -> intent.extras?.getString(SearchManager.QUERY)
                "org.chromium.arc.intent.action.VIEW",
                Intent.ACTION_VIEW -> {
                    try {
                        URLDecoder.decode(intent.data.toString().substring(6), "utf-8")
                    } catch (_: Exception) {
                        null
                    }
                }
                else -> null
            }?.let {
                val pos = it.indexOf("\n")
                return if (pos > 0) {
                    it.substring(0, pos)
                } else {
                    it
                }
            }
        }
        return ""
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val newKeyEvent = if (event.keyCode == KeyEvent.KEYCODE_ESCAPE) {
            KeyEvent(event.action, KeyEvent.KEYCODE_BACK)
        } else {
            event
        }
        return super.dispatchKeyEvent(newKeyEvent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        initialText = getWordFromIntent(intent)
    }
}
