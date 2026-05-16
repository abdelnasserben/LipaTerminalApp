package com.lipa.terminal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TerminalLoginRequest(
    val serialNumber: String,
    val apiKey: String,
)

@Serializable
data class OperatorLoginRequest(
    val phoneCountryCode: String,
    val phoneNumber: String,
    val pin: String,
)

@Serializable
data class NfcChallengeRequest(
    val cardUid: String,
    val method: CardAuthMethod? = null,
)

@Serializable
data class TerminalPaymentRequest(
    val cardUid: String,
    val amount: Long,
    val challengeId: String? = null,
    val cardAuthResponse: String? = null,
    val pinValidated: Boolean? = null,
    val confirmationAcknowledged: Boolean? = null,
)
