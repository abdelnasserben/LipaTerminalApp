package com.lipa.terminal.data.mock

sealed class ForcedOutcome {
    data object Auto : ForcedOutcome()
    data object Executed : ForcedOutcome()
    data object PendingPin : ForcedOutcome()
    data object PendingConfirmation : ForcedOutcome()
    data class Decline(val code: String) : ForcedOutcome()
}

data class MockRules(
    var forcedOutcome: ForcedOutcome = ForcedOutcome.Auto,
    var failTerminalLogin: Boolean = false,
    var forceTerminalSuspended: Boolean = false,
    var forceTerminalRevoked: Boolean = false,
    var forceTerminalNotRegistered: Boolean = false,
    var forceOperatorSuspended: Boolean = false,
)
