package jp.gr.aqua.adice.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import jp.gr.aqua.adice.R
import jp.gr.aqua.adice.model.ContextModel
import jp.gr.aqua.adice.ui.components.WebViewWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallScreen(
    onNavigateBack: () -> Unit,
    onDownloadResult: (String) -> Unit
) {
    val installUrl = ContextModel.resources.getString(R.string.install_url)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dldictionarytitle)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        WebViewWrapper(
            url = installUrl,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onIntentUrl = { url ->
                onDownloadResult(url)
            }
        )
    }
}
