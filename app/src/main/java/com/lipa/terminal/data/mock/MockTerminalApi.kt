package com.lipa.terminal.data.mock

import com.lipa.terminal.data.api.ApiResult
import com.lipa.terminal.data.api.TerminalApi
import com.lipa.terminal.data.model.ApiError
import com.lipa.terminal.data.model.CardAuthMethod
import com.lipa.terminal.data.model.ErrorCodes
import com.lipa.terminal.data.model.NfcChallengeRequest
import com.lipa.terminal.data.model.NfcChallengeResponse
import com.lipa.terminal.data.model.OperatorLoginRequest
import com.lipa.terminal.data.model.TerminalLoginRequest
import com.lipa.terminal.data.model.TerminalPaymentRequest
import com.lipa.terminal.data.model.TerminalPaymentResponse
import com.lipa.terminal.data.model.TerminalTokenResponse
import com.lipa.terminal.data.model.TransactionControlOutcome
import com.lipa.terminal.data.model.TransactionStatus
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class MockTerminalApi(
    private val rules: MockRules = MockRules(),
) : TerminalApi {

    private val idempotencyStore = mutableMapOf<String, TerminalPaymentResponse>()
    private val challenges = mutableMapOf<String, Pair<String, Instant>>()
    private var apiKeyAttempts = 0

    override suspend fun login(req: TerminalLoginRequest): ApiResult<TerminalTokenResponse> {
        delay(700)
        if (req.serialNumber.isBlank() || req.apiKey.isBlank()) {
            return failure(400, ErrorCodes.VALIDATION_FIELD_REQUIRED, "serialNumber and apiKey are required")
        }
        if (rules.forceTerminalSuspended) {
            return failure(422, ErrorCodes.TERMINAL_SUSPENDED, "Terminal is suspended")
        }
        if (rules.forceTerminalRevoked) {
            return failure(422, ErrorCodes.TERMINAL_REVOKED, "Terminal is revoked")
        }
        if (rules.forceTerminalNotRegistered) {
            return failure(422, ErrorCodes.TERMINAL_NOT_REGISTERED, "Terminal is not registered")
        }
        if (rules.failTerminalLogin) {
            apiKeyAttempts += 1
            if (apiKeyAttempts >= 5) {
                return failure(422, ErrorCodes.TERMINAL_SUSPENDED, "Terminal auto-suspended after 5 failed attempts")
            }
            return failure(401, ErrorCodes.TERMINAL_AUTH_FAILED, "Terminal authentication failed")
        }
        apiKeyAttempts = 0
        val now = Instant.now()
        return ApiResult.Success(
            TerminalTokenResponse(
                tokenType = "Bearer",
                accessToken = "terminal." + UUID.randomUUID(),
                accessTokenExpiresAt = now.plus(30, ChronoUnit.DAYS).toString(),
                terminalId = UUID.randomUUID().toString(),
                merchantId = UUID.randomUUID().toString(),
            ),
            httpStatus = 200,
        )
    }

    override suspend fun operatorLogin(
        terminalToken: String,
        req: OperatorLoginRequest,
    ): ApiResult<TerminalTokenResponse> {
        delay(550)
        if (terminalToken.isBlank()) {
            return failure(401, ErrorCodes.UNAUTHORIZED, "Missing token")
        }
        if (req.phoneCountryCode.isBlank() || req.phoneNumber.isBlank() || req.pin.length !in 4..12) {
            return failure(400, ErrorCodes.VALIDATION_FIELD_REQUIRED, "Invalid operator credentials payload")
        }
        if (rules.forceOperatorSuspended) {
            return failure(422, ErrorCodes.OPERATOR_SUSPENDED, "Operator suspended")
        }
        if (req.pin == "0000") {
            return failure(401, ErrorCodes.INVALID_CREDENTIALS, "Wrong PIN")
        }
        val now = Instant.now()
        return ApiResult.Success(
            TerminalTokenResponse(
                tokenType = "Bearer",
                accessToken = "operator." + UUID.randomUUID(),
                accessTokenExpiresAt = now.plus(8, ChronoUnit.HOURS).toString(),
                terminalId = UUID.randomUUID().toString(),
                merchantId = UUID.randomUUID().toString(),
            ),
            httpStatus = 200,
        )
    }

    override suspend fun logout(accessToken: String): ApiResult<Unit> {
        delay(180)
        if (accessToken.isBlank()) {
            return failure(401, ErrorCodes.UNAUTHORIZED, "Missing token")
        }
        return ApiResult.Success(Unit, httpStatus = 204)
    }

    override suspend fun nfcChallenge(
        operatorToken: String,
        req: NfcChallengeRequest,
    ): ApiResult<NfcChallengeResponse> {
        delay(280)
        if (operatorToken.isBlank()) return failure(401, ErrorCodes.UNAUTHORIZED, "Missing operator token")
        if (req.cardUid.length != 14 || !req.cardUid.all { it.isDigit() || it.uppercaseChar() in 'A'..'F' }) {
            return failure(400, ErrorCodes.VALIDATION_FIELD_REQUIRED, "cardUid must be exactly 14 hex chars")
        }
        val challengeId = UUID.randomUUID().toString()
        val challengeHex = (1..32).joinToString("") { "0123456789ABCDEF".random().toString() }
        val expiresAt = Instant.now().plus(60, ChronoUnit.SECONDS)
        challenges[challengeId] = req.cardUid to expiresAt
        return ApiResult.Success(
            NfcChallengeResponse(
                challengeId = challengeId,
                challengeHex = challengeHex,
                method = req.method ?: CardAuthMethod.NFC_CHALLENGE_RESPONSE,
                expiresAt = expiresAt.toString(),
            ),
            httpStatus = 200,
        )
    }

    override suspend fun submitPayment(
        operatorToken: String,
        idempotencyKey: String,
        correlationId: String?,
        req: TerminalPaymentRequest,
    ): ApiResult<TerminalPaymentResponse> {
        delay(800)
        if (operatorToken.isBlank()) return failure(401, ErrorCodes.UNAUTHORIZED, "Missing operator token")
        if (idempotencyKey.isBlank()) {
            return failure(400, ErrorCodes.MISSING_IDEMPOTENCY_KEY, "Idempotency-Key header is required")
        }
        if (req.amount <= 0) {
            return failure(400, ErrorCodes.VALIDATION_FIELD_REQUIRED, "amount must be strictly positive")
        }
        if (req.cardUid.length != 14) {
            return failure(400, ErrorCodes.VALIDATION_FIELD_REQUIRED, "cardUid must be 14 hex chars")
        }
        if ((req.challengeId == null) != (req.cardAuthResponse == null)) {
            return failure(400, "VALIDATION_ERROR", "challengeId and cardAuthResponse must be both present or both absent")
        }

        idempotencyStore[idempotencyKey]?.let { prior ->
            val replayed = prior.copy(replayed = true)
            return ApiResult.Success(replayed, httpStatus = 200)
        }

        if (req.challengeId != null) {
            val entry = challenges.remove(req.challengeId)
                ?: return failure(422, ErrorCodes.CARD_AUTH_FAILED, "Unknown or consumed challenge")
            val (boundUid, expiresAt) = entry
            if (Instant.now().isAfter(expiresAt) || boundUid != req.cardUid) {
                return failure(422, ErrorCodes.CARD_AUTH_FAILED, "Challenge expired or UID mismatch")
            }
        }

        val forced = rules.forcedOutcome
        val outcome = when (forced) {
            ForcedOutcome.Auto -> autoOutcome(req)
            ForcedOutcome.Executed -> TransactionControlOutcome.EXECUTED
            ForcedOutcome.PendingPin -> TransactionControlOutcome.PENDING_PIN
            ForcedOutcome.PendingConfirmation -> TransactionControlOutcome.PENDING_CONFIRMATION
            is ForcedOutcome.Decline -> return failure(422, forced.code, declineMessage(forced.code))
        }

        return when (outcome) {
            TransactionControlOutcome.EXECUTED -> {
                if (req.pinValidated == false && req.amount >= 100000) {
                }
                val fee = (req.amount * 15 / 1000)
                val authMethod = if (req.challengeId != null) CardAuthMethod.NFC_CHALLENGE_RESPONSE else CardAuthMethod.UID_ONLY
                val response = TerminalPaymentResponse(
                    outcome = TransactionControlOutcome.EXECUTED,
                    matchedThresholdAmount = null,
                    transactionId = UUID.randomUUID().toString(),
                    status = TransactionStatus.COMPLETED,
                    requestedAmount = req.amount,
                    feeAmount = fee,
                    netAmountToMerchant = req.amount - fee,
                    currency = "KMF",
                    completedAt = Instant.now().toString(),
                    replayed = false,
                    cardAuthMethod = authMethod,
                )
                idempotencyStore[idempotencyKey] = response
                ApiResult.Success(response, httpStatus = 200)
            }
            TransactionControlOutcome.PENDING_PIN -> ApiResult.Success(
                TerminalPaymentResponse(
                    outcome = TransactionControlOutcome.PENDING_PIN,
                    matchedThresholdAmount = 25_000L,
                    requestedAmount = req.amount,
                    currency = "KMF",
                    replayed = false,
                ),
                httpStatus = 202,
            )
            TransactionControlOutcome.PENDING_CONFIRMATION -> ApiResult.Success(
                TerminalPaymentResponse(
                    outcome = TransactionControlOutcome.PENDING_CONFIRMATION,
                    matchedThresholdAmount = 50_000L,
                    requestedAmount = req.amount,
                    currency = "KMF",
                    replayed = false,
                ),
                httpStatus = 202,
            )
            TransactionControlOutcome.PENDING_APPROVAL -> failure(500, "INTERNAL_ERROR", "PENDING_APPROVAL is not returned to terminals")
        }
    }

    private fun autoOutcome(req: TerminalPaymentRequest): TransactionControlOutcome {
        if (req.confirmationAcknowledged == true || req.pinValidated == true) {
            return TransactionControlOutcome.EXECUTED
        }
        return when {
            req.amount >= 50_000 -> TransactionControlOutcome.PENDING_CONFIRMATION
            req.amount >= 25_000 -> TransactionControlOutcome.PENDING_PIN
            else -> TransactionControlOutcome.EXECUTED
        }
    }

    private fun declineMessage(code: String): String = when (code) {
        ErrorCodes.CARD_BLOCKED -> "Card is blocked"
        ErrorCodes.CARD_LOST -> "Card reported lost"
        ErrorCodes.CARD_STOLEN -> "Card reported stolen"
        ErrorCodes.CARD_EXPIRED -> "Card expired"
        ErrorCodes.CARD_NOT_ACTIVE -> "Card not active"
        ErrorCodes.CARD_AUTH_FAILED -> "Card authentication failed"
        ErrorCodes.PIN_LOCKED -> "PIN locked"
        ErrorCodes.INSUFFICIENT_BALANCE -> "Insufficient balance"
        ErrorCodes.LIMIT_EXCEEDED -> "Limit exceeded"
        ErrorCodes.WALLET_FROZEN -> "Wallet frozen"
        else -> code
    }

    private fun <T> failure(status: Int, code: String, message: String): ApiResult<T> =
        ApiResult.Failure(
            httpStatus = status,
            error = ApiError(
                code = code,
                message = message,
                details = emptyList(),
                correlationId = UUID.randomUUID().toString(),
                timestamp = Instant.now().toString(),
            ),
        )
}
