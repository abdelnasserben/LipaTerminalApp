package com.lipa.terminal.data.api

import com.lipa.terminal.data.model.NfcChallengeRequest
import com.lipa.terminal.data.model.NfcChallengeResponse
import com.lipa.terminal.data.model.OperatorLoginRequest
import com.lipa.terminal.data.model.TerminalLoginRequest
import com.lipa.terminal.data.model.TerminalPaymentRequest
import com.lipa.terminal.data.model.TerminalPaymentResponse
import com.lipa.terminal.data.model.TerminalTokenResponse

interface TerminalApi {
    suspend fun login(req: TerminalLoginRequest): ApiResult<TerminalTokenResponse>
    suspend fun operatorLogin(terminalToken: String, req: OperatorLoginRequest): ApiResult<TerminalTokenResponse>
    suspend fun logout(accessToken: String): ApiResult<Unit>
    suspend fun nfcChallenge(operatorToken: String, req: NfcChallengeRequest): ApiResult<NfcChallengeResponse>
    suspend fun submitPayment(
        operatorToken: String,
        idempotencyKey: String,
        correlationId: String?,
        req: TerminalPaymentRequest,
    ): ApiResult<TerminalPaymentResponse>
}
