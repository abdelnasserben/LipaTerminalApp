package com.lipa.terminal.data.api

import com.lipa.terminal.BuildConfig
import com.lipa.terminal.data.mock.MockTerminalApi

object TerminalApiFactory {
    fun create(): TerminalApi =
        if (BuildConfig.KOMOPAY_USE_MOCK_API) {
            MockTerminalApi()
        } else {
            HttpTerminalApi(baseUrl = BuildConfig.KOMOPAY_API_BASE_URL)
        }
}
