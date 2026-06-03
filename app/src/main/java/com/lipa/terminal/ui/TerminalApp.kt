package com.lipa.terminal.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lipa.terminal.data.model.CardAuthMethod
import com.lipa.terminal.domain.Screen
import com.lipa.terminal.domain.TerminalViewModel
import com.lipa.terminal.ui.components.LipaStatusBar
import com.lipa.terminal.ui.screens.ApprovedScreen
import com.lipa.terminal.ui.screens.ConfirmationScreen
import com.lipa.terminal.ui.screens.CustomerPinScreen
import com.lipa.terminal.ui.screens.DeclinedScreen
import com.lipa.terminal.ui.screens.DeviceLoginScreen
import com.lipa.terminal.ui.screens.IdleScreen
import com.lipa.terminal.ui.screens.OperatorLoginScreen
import com.lipa.terminal.ui.screens.ShiftMenuSheet
import com.lipa.terminal.ui.screens.TapCardScreen
import com.lipa.terminal.ui.screens.TerminalLockedScreen
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TerminalApp(viewModel: TerminalViewModel) {
    val state by viewModel.state.collectAsState()
    val terminal by viewModel.terminalSession.collectAsState()
    val operator by viewModel.operatorSession.collectAsState()

    BackHandler(enabled = state.screen != Screen.Idle && state.screen != Screen.DeviceLogin) {
        when (state.screen) {
            Screen.TapCard, Screen.CustomerPin, Screen.Confirmation -> viewModel.cancelCharge()
            Screen.Approved, Screen.Declined -> viewModel.finishAndReset()
            Screen.OperatorLogin -> Unit
            Screen.TerminalLocked -> Unit
            else -> Unit
        }
    }

    LipaTheme {
        Column(modifier = Modifier.fillMaxSize().background(LipaColors.Bg).navigationBarsPadding()) {
            val operatorLabel = operator?.fullName?.takeIf { it.isNotBlank() }
                ?: operator?.phoneNumber?.takeLast(7)
            val showOperator = state.screen != Screen.DeviceLogin && state.screen != Screen.OperatorLogin && operator != null
            LipaStatusBar(
                time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                operator = if (showOperator) operatorLabel else null,
                online = state.nfcSupported,
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state.screen) {
                    Screen.DeviceLogin -> DeviceLoginScreen(
                        error = state.deviceLoginError,
                        isBusy = state.isBusy,
                        onLogin = viewModel::deviceLogin,
                    )
                    Screen.OperatorLogin -> OperatorLoginScreen(
                        deviceSerial = terminal?.serialNumber.orEmpty(),
                        error = state.operatorLoginError,
                        isBusy = state.isBusy,
                        onLogin = viewModel::operatorLogin,
                        onSignOutOfTerminal = viewModel::signOutOfTerminal,
                    )
                    Screen.Idle -> IdleScreen(
                        amount = state.amountInput,
                        onDigit = viewModel::setAmountFromDigit,
                        onBackspace = viewModel::amountBackspace,
                        onQuickAmount = viewModel::setQuickAmount,
                        onCharge = viewModel::startCharge,
                        onOpenShiftMenu = viewModel::openShiftMenu,
                    )
                    Screen.TapCard -> TapCardScreen(
                        amount = state.amountInput.toLongOrNull() ?: 0L,
                        status = state.tapStatus,
                        authModeLabel = if (state.authMode == CardAuthMethod.UID_ONLY) "UID_ONLY" else "CHALLENGE",
                        nfcUnavailable = !state.nfcSupported || !state.nfcEnabled,
                        onCancel = viewModel::cancelCharge,
                    )
                    Screen.CustomerPin -> CustomerPinScreen(
                        amount = state.amountInput.toLongOrNull() ?: 0L,
                        cardMask = state.cardMask,
                        error = state.customerPinError,
                        isVerifying = state.tapStatus == com.lipa.terminal.domain.TapStatus.Authorizing,
                        onSubmit = viewModel::submitCustomerPin,
                        onCancel = viewModel::cancelCharge,
                    )
                    Screen.Confirmation -> ConfirmationScreen(
                        amount = state.amountInput.toLongOrNull() ?: 0L,
                        threshold = state.pendingThreshold,
                        cardMask = state.cardMask,
                        authMethodLabel = state.authMode.name,
                        onConfirm = viewModel::confirmLargeAmount,
                        onCancel = viewModel::cancelCharge,
                    )
                    Screen.Approved -> {
                        val result = state.executedResult
                        if (result != null) {
                            ApprovedScreen(
                                result = result,
                                cardMask = state.cardMask,
                                onDone = viewModel::finishAndReset,
                            )
                        }
                    }
                    Screen.Declined -> DeclinedScreen(
                        code = state.declineCode,
                        message = state.declineMessage,
                        amount = state.amountInput.toLongOrNull() ?: 0L,
                        onRetry = viewModel::retryCharge,
                        onDone = viewModel::finishAndReset,
                    )
                    Screen.TerminalLocked -> TerminalLockedScreen(
                        reason = state.lockedReason,
                        onSignOut = viewModel::signOutOfTerminal,
                    )
                }

                if (state.shiftMenuOpen && operator != null) {
                    val sessionEnds = operator?.expiresAt.orEmpty().take(16).replace("T", " ")
                    ShiftMenuSheet(
                        operatorName = operator?.fullName.orEmpty(),
                        phoneCountryCode = operator?.phoneCountryCode.orEmpty(),
                        phoneNumber = operator?.phoneNumber.orEmpty(),
                        terminalSerial = terminal?.serialNumber.orEmpty(),
                        merchantId = terminal?.merchantId.orEmpty().take(8) + "…",
                        sessionEndsAt = sessionEnds,
                        onClose = viewModel::closeShiftMenu,
                        onEndShift = viewModel::endShift,
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(LipaColors.Bg))
        }
    }
}
