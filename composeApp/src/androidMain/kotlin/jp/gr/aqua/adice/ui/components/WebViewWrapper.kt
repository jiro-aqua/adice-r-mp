package jp.gr.aqua.adice.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import jp.gr.aqua.adice.BuildConfig
import jp.gr.aqua.adice.R
import jp.gr.aqua.adice.model.ContextModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewWrapper(
    url: String,
    modifier: Modifier = Modifier,
    onIntentUrl: ((String) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                isFocusable = true
                isFocusableInTouchMode = true

                webViewClient = WebViewClient()

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun getAboutStrings(key: String): String {
                        return when (key) {
                            "version" -> {
                                val versionName = BuildConfig.VERSION_NAME
                                val versionCode = BuildConfig.VERSION_CODE
                                "Ver. $versionName ($versionCode)"
                            }
                            "description" -> ContextModel.resources.getString(R.string.description)
                            "manual" -> ContextModel.resources.getString(R.string.manual)
                            else -> ""
                        }
                    }

                    @JavascriptInterface
                    fun throwIntentByUrl(url: String?, @Suppress("UNUSED_PARAMETER") requestCode: Int) {
                        if (!url.isNullOrEmpty()) {
                            coroutineScope.launch(Dispatchers.Main) {
                                onIntentUrl?.invoke(url)
                            }
                        }
                    }
                }, "jscallback")

                loadUrl(url)
            }
        },
        modifier = modifier
    )
}
