package com.lipa.terminal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val data: T,
    val timestamp: String,
)

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val details: List<String> = emptyList(),
    val correlationId: String? = null,
    val timestamp: String,
)

@Serializable
data class TerminalTokenResponse(
    val tokenType: String,
    val accessToken: String,
    val accessTokenExpiresAt: String,
    val terminalId: String,
    val terminalSerialNumber: String,
    val merchantId: String,
    val operatorId: String? = null,
    val operatorFullName: String? = null,
)

@Serializable
data class NfcChallengeResponse(
    val challengeId: String,
    val challengeHex: String,
    val method: CardAuthMethod,
    val expiresAt: String,
)

@Serializable
data class TerminalPaymentResponse(
    val outcome: TransactionControlOutcome,
    val matchedThresholdAmount: Long? = null,
    val transactionId: String? = null,
    val status: TransactionStatus? = null,
    val requestedAmount: Long,
    val feeAmount: Long? = null,
    val netAmountToMerchant: Long? = null,
    val currency: String = "KMF",
    val completedAt: String? = null,
    val replayed: Boolean? = null,
    val cardAuthMethod: CardAuthMethod? = null,
)
