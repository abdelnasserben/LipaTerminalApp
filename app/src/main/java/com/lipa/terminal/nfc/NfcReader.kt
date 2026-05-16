package com.lipa.terminal.nfc

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class NfcReader(private val activity: Activity) {

    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private val uids = Channel<String>(Channel.BUFFERED)

    val isSupported: Boolean get() = adapter != null
    val isEnabled: Boolean get() = adapter?.isEnabled == true

    val cardUids: Flow<String> = uids.receiveAsFlow()

    fun enableForegroundDispatch() {
        if (adapter == null || !adapter.isEnabled) return
        adapter.enableReaderMode(
            activity,
            { tag: Tag ->
                val uid = tag.id?.toHexUpper().orEmpty()
                if (uid.length >= 14) {
                    uids.trySend(uid.take(14))
                } else if (uid.isNotEmpty()) {
                    uids.trySend(uid.padEnd(14, '0'))
                }
            },
            NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            Bundle(),
        )
    }

    fun disableForegroundDispatch() {
        adapter?.disableReaderMode(activity)
    }

    fun onNewIntent(intent: Intent) {
        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }
        val uid = tag?.id?.toHexUpper().orEmpty()
        if (uid.isNotEmpty()) {
            uids.trySend(uid.take(14).padEnd(14, '0'))
        }
    }

    private fun ByteArray.toHexUpper(): String =
        joinToString("") { String.format("%02X", it) }
}
