package com.lipa.terminal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.lipa.terminal.data.api.TerminalApi
import com.lipa.terminal.data.mock.MockTerminalApi
import com.lipa.terminal.data.repository.SessionRepository
import com.lipa.terminal.domain.TerminalViewModel
import com.lipa.terminal.nfc.NfcReader
import com.lipa.terminal.ui.TerminalApp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {

    private lateinit var nfcReader: NfcReader

    private val api: TerminalApi = MockTerminalApi()
    private val sessionRepository = SessionRepository()

    private val viewModel: TerminalViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                TerminalViewModel(api, sessionRepository) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcReader = NfcReader(this)
        viewModel.setNfcStatus(nfcReader.isSupported, nfcReader.isEnabled)

        nfcReader.cardUids
            .onEach { uid -> viewModel.onCardDetected(uid) }
            .launchIn(lifecycleScope)

        setContent {
            TerminalApp(viewModel = viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        if (nfcReader.isSupported) {
            nfcReader.enableForegroundDispatch()
            viewModel.setNfcStatus(true, nfcReader.isEnabled)
        }
    }

    override fun onPause() {
        super.onPause()
        if (nfcReader.isSupported) {
            nfcReader.disableForegroundDispatch()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nfcReader.onNewIntent(intent)
    }
}
