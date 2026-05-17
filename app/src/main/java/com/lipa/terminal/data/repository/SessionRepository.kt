package com.lipa.terminal.data.repository

import com.lipa.terminal.data.model.TerminalTokenResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TerminalSession(
    val token: String,
    val terminalId: String,
    val merchantId: String,
    val expiresAt: String,
    val serialNumber: String,
)

data class OperatorSession(
    val token: String,
    val terminalId: String,
    val merchantId: String,
    val expiresAt: String,
    val phoneCountryCode: String,
    val phoneNumber: String,
    val operatorId: String?,
    val fullName: String?,
)

class SessionRepository {
    private val _terminal = MutableStateFlow<TerminalSession?>(null)
    val terminal: StateFlow<TerminalSession?> = _terminal.asStateFlow()

    private val _operator = MutableStateFlow<OperatorSession?>(null)
    val operator: StateFlow<OperatorSession?> = _operator.asStateFlow()

    fun setTerminal(token: TerminalTokenResponse) {
        _terminal.value = TerminalSession(
            token = token.accessToken,
            terminalId = token.terminalId,
            merchantId = token.merchantId,
            expiresAt = token.accessTokenExpiresAt,
            serialNumber = token.terminalSerialNumber,
        )
    }

    fun setOperator(token: TerminalTokenResponse, phoneCountryCode: String, phoneNumber: String) {
        _operator.value = OperatorSession(
            token = token.accessToken,
            terminalId = token.terminalId,
            merchantId = token.merchantId,
            expiresAt = token.accessTokenExpiresAt,
            phoneCountryCode = phoneCountryCode,
            phoneNumber = phoneNumber,
            operatorId = token.operatorId,
            fullName = token.operatorFullName,
        )
    }

    fun clearOperator() {
        _operator.value = null
    }

    fun clearAll() {
        _operator.value = null
        _terminal.value = null
    }
}
