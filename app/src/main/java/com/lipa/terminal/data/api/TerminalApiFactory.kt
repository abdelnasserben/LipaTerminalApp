package com.lipa.terminal.data.api

import com.lipa.terminal.BuildConfig

object TerminalApiFactory {
    fun create(): TerminalApi =
        HttpTerminalApi(baseUrl = BuildConfig.KOMOPAY_API_BASE_URL)
}
