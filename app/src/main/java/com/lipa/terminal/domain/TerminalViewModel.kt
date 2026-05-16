package com.lipa.terminal.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lipa.terminal.data.api.ApiResult
import com.lipa.terminal.data.api.TerminalApi
import com.lipa.terminal.data.model.ApiError
import com.lipa.terminal.data.model.CardAuthMethod
import com.lipa.terminal.data.model.NfcChallengeRequest
import com.lipa.terminal.data.model.OperatorLoginRequest
import com.lipa.terminal.data.model.TerminalLoginRequest
import com.lipa.terminal.data.model.TerminalPaymentRequest
import com.lipa.terminal.data.model.TerminalPaymentResponse
import com.lipa.terminal.data.model.TransactionControlOutcome
import com.lipa.terminal.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface Screen {
    data object DeviceLogin : Screen
    data object OperatorLogin : Screen
    data object Idle : Screen
    data object TapCard : Screen
    data object CustomerPin : Screen
    data object Confirmation : Screen
    data object Approved : Screen
    data object Declined : Screen
    data object TerminalLocked : Screen
}

enum class TapStatus { Waiting, Reading, Authorizing }

data class TerminalUiState(
    val screen: Screen = Screen.DeviceLogin,
    val amountInput: String = "0",
    val tapStatus: TapStatus = TapStatus.Waiting,
    val cardMask: String = "",
    val cardUid: String = "",
    val authMode: CardAuthMethod = CardAuthMethod.UID_ONLY,
    val challengeId: String? = null,
    val challengeHex: String? = null,
    val cardAuthResponse: String? = null,
    val idempotencyKey: String? = null,
    val correlationId: String? = null,
    val pinValidated: Boolean = false,
    val confirmationAcknowledged: Boolean = false,
    val pendingThreshold: Long? = null,
    val executedResult: TerminalPaymentResponse? = null,
    val declineCode: String? = null,
    val declineMessage: String? = null,
    val lockedReason: String? = null,
    val deviceLoginError: String? = null,
    val operatorLoginError: String? = null,
    val customerPinError: Boolean = false,
    val isBusy: Boolean = false,
    val shiftMenuOpen: Boolean = false,
    val nfcSupported: Boolean = true,
    val nfcEnabled: Boolean = true,
)

class TerminalViewModel(
    private val api: TerminalApi,
    private val session: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TerminalUiState())
    val state: StateFlow<TerminalUiState> = _state.asStateFlow()

    val terminalSession get() = session.terminal
    val operatorSession get() = session.operator

    fun setAmountFromDigit(digit: String) {
        _state.update {
            val cur = it.amountInput
            val next = if (cur == "0" || cur.isEmpty()) digit
            else if (cur.length < 9) cur + digit
            else cur
            it.copy(amountInput = next)
        }
    }

    fun amountBackspace() {
        _state.update {
            val cur = it.amountInput
            val next = if (cur.length <= 1) "0" else cur.dropLast(1)
            it.copy(amountInput = next)
        }
    }

    fun setQuickAmount(value: Long) {
        _state.update { it.copy(amountInput = value.toString()) }
    }

    fun setAuthMode(method: CardAuthMethod) {
        _state.update { it.copy(authMode = method) }
    }

    fun setNfcStatus(supported: Boolean, enabled: Boolean) {
        _state.update { it.copy(nfcSupported = supported, nfcEnabled = enabled) }
    }

    fun openShiftMenu() = _state.update { it.copy(shiftMenuOpen = true) }
    fun closeShiftMenu() = _state.update { it.copy(shiftMenuOpen = false) }

    fun deviceLogin(serial: String, apiKey: String) {
        _state.update { it.copy(isBusy = true, deviceLoginError = null) }
        viewModelScope.launch {
            when (val res = api.login(TerminalLoginRequest(serial, apiKey))) {
                is ApiResult.Success -> {
                    session.setTerminal(res.value, serial)
                    _state.update { it.copy(isBusy = false, screen = Screen.OperatorLogin) }
                }
                is ApiResult.Failure -> handleDeviceLoginFailure(res.error)
            }
        }
    }

    private fun handleDeviceLoginFailure(error: ApiError) {
        val lockedCodes = setOf("TERMINAL_SUSPENDED", "TERMINAL_REVOKED", "TERMINAL_NOT_REGISTERED")
        if (error.code in lockedCodes) {
            _state.update {
                it.copy(
                    isBusy = false,
                    screen = Screen.TerminalLocked,
                    lockedReason = error.code,
                )
            }
        } else {
            _state.update { it.copy(isBusy = false, deviceLoginError = error.message) }
        }
    }

    fun operatorLogin(phoneCountryCode: String, phoneNumber: String, pin: String) {
        val terminal = session.terminal.value
        if (terminal == null) {
            _state.update { it.copy(screen = Screen.DeviceLogin) }
            return
        }
        _state.update { it.copy(isBusy = true, operatorLoginError = null) }
        viewModelScope.launch {
            val res = api.operatorLogin(
                terminal.token,
                OperatorLoginRequest(phoneCountryCode, phoneNumber, pin),
            )
            when (res) {
                is ApiResult.Success -> {
                    session.setOperator(res.value, phoneCountryCode, phoneNumber)
                    _state.update {
                        it.copy(
                            isBusy = false,
                            screen = Screen.Idle,
                            amountInput = "0",
                        )
                    }
                }
                is ApiResult.Failure -> {
                    val locked = res.error.code in setOf("TERMINAL_SUSPENDED", "TERMINAL_REVOKED", "TERMINAL_NOT_REGISTERED")
                    if (locked) {
                        _state.update {
                            it.copy(
                                isBusy = false,
                                screen = Screen.TerminalLocked,
                                lockedReason = res.error.code,
                            )
                        }
                    } else {
                        _state.update { it.copy(isBusy = false, operatorLoginError = res.error.message) }
                    }
                }
            }
        }
    }

    fun startCharge() {
        val amount = _state.value.amountInput.toLongOrNull() ?: 0L
        if (amount <= 0) return
        _state.update {
            it.copy(
                screen = Screen.TapCard,
                tapStatus = TapStatus.Waiting,
                cardUid = "",
                challengeId = null,
                challengeHex = null,
                cardAuthResponse = null,
                idempotencyKey = UUID.randomUUID().toString(),
                correlationId = UUID.randomUUID().toString(),
                pinValidated = false,
                confirmationAcknowledged = false,
                declineCode = null,
                executedResult = null,
            )
        }
    }

    fun cancelCharge() {
        _state.update {
            it.copy(
                screen = Screen.Idle,
                tapStatus = TapStatus.Waiting,
                amountInput = "0",
                idempotencyKey = null,
                executedResult = null,
            )
        }
    }

    fun onCardDetected(uid: String) {
        val current = _state.value
        if (current.screen != Screen.TapCard) return
        if (current.cardUid.isNotEmpty()) return
        val mask = "••• " + uid.takeLast(4)
        _state.update { it.copy(cardUid = uid, cardMask = mask, tapStatus = TapStatus.Reading) }

        viewModelScope.launch {
            if (current.authMode != CardAuthMethod.UID_ONLY) {
                val op = session.operator.value ?: return@launch
                val chRes = api.nfcChallenge(op.token, NfcChallengeRequest(uid, current.authMode))
                when (chRes) {
                    is ApiResult.Success -> {
                        val simulatedResp = (1..32).joinToString("") { "0123456789ABCDEF".random().toString() }
                        _state.update {
                            it.copy(
                                challengeId = chRes.value.challengeId,
                                challengeHex = chRes.value.challengeHex,
                                cardAuthResponse = simulatedResp,
                            )
                        }
                    }
                    is ApiResult.Failure -> {
                        _state.update {
                            it.copy(
                                screen = Screen.Declined,
                                declineCode = chRes.error.code,
                                declineMessage = chRes.error.message,
                            )
                        }
                        return@launch
                    }
                }
            }
            _state.update { it.copy(tapStatus = TapStatus.Authorizing) }
            submitPayment()
        }
    }

    fun simulateCardTap() {
        val uid = (1..14).joinToString("") { "0123456789ABCDEF".random().toString() }
        onCardDetected(uid)
    }

    private suspend fun submitPayment() {
        val s = _state.value
        val op = session.operator.value ?: return
        val amount = s.amountInput.toLongOrNull() ?: return
        val idempotencyKey = s.idempotencyKey ?: return

        val req = TerminalPaymentRequest(
            cardUid = s.cardUid,
            amount = amount,
            challengeId = s.challengeId,
            cardAuthResponse = s.cardAuthResponse,
            pinValidated = if (s.pinValidated) true else null,
            confirmationAcknowledged = if (s.confirmationAcknowledged) true else null,
        )

        when (val res = api.submitPayment(op.token, idempotencyKey, s.correlationId, req)) {
            is ApiResult.Success -> handlePaymentSuccess(res.value, res.httpStatus)
            is ApiResult.Failure -> {
                _state.update {
                    it.copy(
                        screen = Screen.Declined,
                        declineCode = res.error.code,
                        declineMessage = res.error.message,
                    )
                }
            }
        }
    }

    private fun handlePaymentSuccess(value: TerminalPaymentResponse, httpStatus: Int) {
        when (value.outcome) {
            TransactionControlOutcome.EXECUTED -> _state.update {
                it.copy(screen = Screen.Approved, executedResult = value)
            }
            TransactionControlOutcome.PENDING_PIN -> _state.update {
                it.copy(
                    screen = Screen.CustomerPin,
                    pendingThreshold = value.matchedThresholdAmount,
                    customerPinError = false,
                    tapStatus = TapStatus.Waiting,
                )
            }
            TransactionControlOutcome.PENDING_CONFIRMATION -> _state.update {
                it.copy(
                    screen = Screen.Confirmation,
                    pendingThreshold = value.matchedThresholdAmount,
                    tapStatus = TapStatus.Waiting,
                )
            }
            TransactionControlOutcome.PENDING_APPROVAL -> _state.update {
                it.copy(screen = Screen.Declined, declineCode = "INTERNAL_ERROR", declineMessage = "Unexpected outcome")
            }
        }
    }

    fun submitCustomerPin(pin: String) {
        if (pin.length != 4) return
        _state.update { it.copy(pinValidated = true, tapStatus = TapStatus.Authorizing, customerPinError = false) }
        viewModelScope.launch {
            if (_state.value.authMode != CardAuthMethod.UID_ONLY) {
                val op = session.operator.value
                if (op != null) {
                    val ch = api.nfcChallenge(op.token, NfcChallengeRequest(_state.value.cardUid, _state.value.authMode))
                    if (ch is ApiResult.Success) {
                        val simulatedResp = (1..32).joinToString("") { "0123456789ABCDEF".random().toString() }
                        _state.update {
                            it.copy(challengeId = ch.value.challengeId, challengeHex = ch.value.challengeHex, cardAuthResponse = simulatedResp)
                        }
                    }
                }
            }
            submitPayment()
        }
    }

    fun confirmLargeAmount() {
        _state.update { it.copy(confirmationAcknowledged = true, tapStatus = TapStatus.Authorizing) }
        viewModelScope.launch {
            if (_state.value.authMode != CardAuthMethod.UID_ONLY) {
                val op = session.operator.value
                if (op != null) {
                    val ch = api.nfcChallenge(op.token, NfcChallengeRequest(_state.value.cardUid, _state.value.authMode))
                    if (ch is ApiResult.Success) {
                        val simulatedResp = (1..32).joinToString("") { "0123456789ABCDEF".random().toString() }
                        _state.update {
                            it.copy(challengeId = ch.value.challengeId, challengeHex = ch.value.challengeHex, cardAuthResponse = simulatedResp)
                        }
                    }
                }
            }
            submitPayment()
        }
    }

    fun retryCharge() {
        _state.update {
            it.copy(
                screen = Screen.TapCard,
                tapStatus = TapStatus.Waiting,
                cardUid = "",
                challengeId = null,
                cardAuthResponse = null,
                idempotencyKey = UUID.randomUUID().toString(),
                pinValidated = false,
                confirmationAcknowledged = false,
                declineCode = null,
            )
        }
    }

    fun finishAndReset() {
        _state.update {
            TerminalUiState(
                screen = Screen.Idle,
                nfcSupported = it.nfcSupported,
                nfcEnabled = it.nfcEnabled,
                authMode = it.authMode,
            )
        }
    }

    fun endShift() {
        val op = session.operator.value
        _state.update { it.copy(isBusy = true, shiftMenuOpen = false) }
        viewModelScope.launch {
            if (op != null) {
                api.logout(op.token)
            }
            session.clearOperator()
            _state.update {
                TerminalUiState(
                    screen = Screen.OperatorLogin,
                    nfcSupported = it.nfcSupported,
                    nfcEnabled = it.nfcEnabled,
                    authMode = it.authMode,
                )
            }
        }
    }

    fun signOutOfTerminal() {
        val term = session.terminal.value
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            if (term != null) api.logout(term.token)
            session.clearAll()
            _state.update {
                TerminalUiState(
                    screen = Screen.DeviceLogin,
                    nfcSupported = it.nfcSupported,
                    nfcEnabled = it.nfcEnabled,
                )
            }
        }
    }
}
