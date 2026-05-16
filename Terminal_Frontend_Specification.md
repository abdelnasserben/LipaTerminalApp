# Terminal - Frontend Specification Document

**Version:** 1.0 | **Source:** KomoPay backend codebase analysis | **Date:** 2026-05-14  
**Status:** Single source of truth. Do not call or display anything that is not listed here.

---

## Table of Contents

1. [Scope](#1-scope)
2. [HTTP Contract](#2-http-contract)
3. [Terminal Authentication](#3-terminal-authentication)
4. [Terminal Capability Map](#4-terminal-capability-map)
5. [Exhaustive API Mapping](#5-exhaustive-api-mapping)
6. [Request Schemas](#6-request-schemas)
7. [Response Schemas](#7-response-schemas)
8. [NFC Card Authentication](#8-nfc-card-authentication)
9. [Enums](#9-enums)
10. [Permissions](#10-permissions)
11. [Operational Rules](#11-operational-rules)
12. [Evidence Index](#12-evidence-index)

---

## 1. Scope

This document describes only the APIs a Terminal (Android POS device) frontend can interact with:

- `POST /api/v1/terminal/auth/*`
- `POST /api/v1/terminal/nfc/challenge`
- `POST /api/v1/terminal/transactions/payment`

Backoffice, agent, customer self-service, merchant portal, service-provider callback, and server-to-server APIs are outside this Terminal scope.

Total Terminal endpoints in scope: **5**.

The Terminal app handles a single financial operation: **customer NFC card → merchant payment**, accepted on the device by an authenticated cashier (merchant operator). There is no cash-in, no cash-out, no card issuance, no history, and no profile endpoint in the Terminal scope.

---

## 2. HTTP Contract

### 2.1 Base Rules

| Rule | Value |
|---|---|
| Body format | JSON |
| Currency | KMF |
| Date-time format | ISO-8601 string |
| Auth header | `Authorization: Bearer <accessToken>` for every protected endpoint |
| Optional tracing header | `X-Correlation-Id: <uuid>`; the payment endpoint generates one if absent or rejects a malformed one |
| Required idempotency header | `Idempotency-Key: <opaque-key>` on `POST /api/v1/terminal/transactions/payment` |
| Optional device header | `X-Terminal-Serial`, `X-Client-Version` — accepted by CORS config; not required by any endpoint in scope |
| Rate limit | `POST /api/v1/terminal/nfc/challenge` is rate-limited per IP, per-second AND per-hour (defaults: 10/s, 1000/h) |

### 2.2 Success Envelopes

All Terminal success responses use the standard `ApiResponse` envelope:

```json
{
  "data": {},
  "timestamp": "2026-05-14T12:00:00Z"
}
```

No Terminal endpoint returns a paginated list.

### 2.3 Error Envelopes

Controller and validation errors return the wrapped envelope:

```json
{
  "error": {
    "code": "VALIDATION_FIELD_REQUIRED",
    "message": "Request validation failed",
    "details": ["field: message"],
    "correlationId": "uuid",
    "timestamp": "2026-05-14T12:00:00Z"
  }
}
```

Security filter exceptions are special:

- `401` and `403` emitted by Spring Security return raw `ApiError`, without the outer `{ "error": ... }` wrapper.
- `401` emitted by `JwtAuthenticationFilter` for invalid, expired, or revoked bearer tokens also returns raw `ApiError`.
- `429` from `RateLimitingFilter` returns the wrapped error envelope with code `TERMINAL_RATE_LIMIT`.

### 2.4 HTTP Status Semantics For Payment

`POST /api/v1/terminal/transactions/payment` returns two distinct success codes:

- `200 OK` — the payment **executed**; wallet and ledger were mutated. `outcome=EXECUTED`.
- `202 Accepted` — a control fired (PIN or confirmation). **No transaction was created, no wallet movement happened.** `outcome=PENDING_PIN` or `PENDING_CONFIRMATION`. The terminal must collect the missing step and resubmit.

---

## 3. Terminal Authentication

Terminal authentication is **two-layered**. A terminal session alone cannot take a payment — an operator (cashier) PIN session must be established on top of it.

### 3.1 Terminal Login (device)

`POST /api/v1/terminal/auth/login`

Public endpoint. The Android device authenticates with its provisioned `serialNumber` + `apiKey`.

Request:

```json
{
  "serialNumber": "POS-AB12-0007",
  "apiKey": "raw-api-key-provisioned-by-backoffice"
}
```

Response `200 ApiResponse<TerminalTokenResponse>`:

```json
{
  "data": {
    "tokenType": "Bearer",
    "accessToken": "jwt",
    "accessTokenExpiresAt": "2026-06-13T12:00:00Z",
    "terminalId": "uuid",
    "merchantId": "uuid"
  },
  "timestamp": "2026-05-14T12:00:00Z"
}
```

Terminal login rules:

- The raw API key is provisioned out-of-band by Backoffice and stored only as a BCrypt hash on the terminal.
- The returned JWT carries `actorType=MERCHANT` plus `merchantId` and `terminalId` claims. This token **cannot initiate payments** — it is only good for `operator-login`.
- Token lifetime is `komopay.jwt.expiration.terminal` (long-lived, ~30 days).
- **There is no refresh token.** When the access token expires, the device re-runs `/auth/login` with its API key.
- **5 consecutive failed API-key attempts auto-suspend the terminal.** Once suspended, login returns `TERMINAL_SUSPENDED` until Backoffice reactivates it.
- The terminal must be `ACTIVE` (not `REGISTERED`, `SUSPENDED`, or `REVOKED`) and have a non-expired API key.
- The owning merchant must not be `SUSPENDED` or `CLOSED`.

### 3.2 Operator Login (cashier PIN session)

`POST /api/v1/terminal/auth/operator-login`

Requires a valid terminal JWT (or an existing operator JWT) in the `Authorization` header. The cashier submits phone + PIN to open a shift session.

Request:

```json
{
  "phoneCountryCode": "269",
  "phoneNumber": "3212345",
  "pin": "1234"
}
```

Response `200 ApiResponse<TerminalTokenResponse>`:

```json
{
  "data": {
    "tokenType": "Bearer",
    "accessToken": "jwt",
    "accessTokenExpiresAt": "2026-05-14T20:00:00Z",
    "terminalId": "uuid",
    "merchantId": "uuid"
  },
  "timestamp": "2026-05-14T12:00:00Z"
}
```

Operator login rules:

- `terminalId` is taken from the caller's JWT — never from the request body.
- The operator must belong to the same merchant as the terminal, otherwise `OPERATOR_NOT_AUTHORIZED_ON_TERMINAL`.
- The operator must be `ACTIVE`. A `REVOKED` operator returns `ACTOR_CLOSED`; any other non-active status returns `OPERATOR_SUSPENDED`.
- The PIN is verified against the operator's BCrypt hash. A wrong PIN returns `INVALID_CREDENTIALS`.
- The returned JWT carries `actorType=MERCHANT_OPERATOR`, `actorId=operatorId`, plus `merchantId` and `terminalId`. **This is the only token accepted by the payment and NFC-challenge endpoints.**
- Operator session lifetime is **8 hours** (a typical cashier shift). There is no refresh — when it expires, the cashier logs in again.
- Operator PIN attempts are **not** counted toward a lockout in V1 (deferred).

### 3.3 Logout

`POST /api/v1/terminal/auth/logout`

Headers: `Authorization: Bearer <accessToken>` (terminal or operator JWT).

Response: `204 No Content`.

Logout revokes the current access token's `jti` until its natural expiry horizon. It does **not** revoke other sessions. Clear the local token after a successful call.

### 3.4 JWT Claims Used By Terminal

| Claim | Terminal session | Operator session |
|---|---|---|
| `sub` / `actorId` | merchant UUID | operator UUID |
| `act` | `MERCHANT` | `MERCHANT_OPERATOR` |
| `mid` | merchant UUID | merchant UUID |
| `tid` | terminal UUID | terminal UUID |
| `jti` | access token UUID | access token UUID |
| `iat`, `exp` | issued-at / expiry | issued-at / expiry |

Terminal JWTs do not carry `perms` or `brole` claims.

---

## 4. Terminal Capability Map

| Area | Terminal can do |
|---|---|
| Device session | Authenticate the device with serial + API key; re-authenticate on access-token expiry |
| Operator session | Open an 8-hour cashier PIN session bound to the device; log out |
| NFC challenge | Request a card-authentication challenge for a tapped card UID |
| Payment | Submit a customer NFC card → merchant payment, optionally carrying card auth and cleared PIN/confirmation controls |

The Terminal frontend cannot: view profile, balance, history, statements; do cash-in/cash-out; issue, sell, replace, or report cards; manage operators; or inspect approvals.

---

## 5. Exhaustive API Mapping

### 5.1 Auth

| Method | Path | Auth | Headers | Request | Response | Primary errors | Frontend notes |
|---|---|---|---|---|---|---|---|
| POST | `/api/v1/terminal/auth/login` | Public | none | `TerminalLoginRequest` | `200 ApiResponse<TerminalTokenResponse>` | `400 VALIDATION_FIELD_REQUIRED`, `401 TERMINAL_AUTH_FAILED`, `401 TERMINAL_API_KEY_EXPIRED`, `422 TERMINAL_NOT_REGISTERED`, `422 TERMINAL_SUSPENDED`, `422 TERMINAL_REVOKED`, `404 MERCHANT_NOT_FOUND`, `422 ACTOR_SUSPENDED`, `422 ACTOR_CLOSED` | Store `accessToken`. No refresh token — re-login on expiry. After 5 failures the terminal is auto-suspended. |
| POST | `/api/v1/terminal/auth/operator-login` | Terminal or Operator JWT | `Authorization` | `OperatorLoginRequest` | `200 ApiResponse<TerminalTokenResponse>` | `400 VALIDATION_FIELD_REQUIRED`, `401 UNAUTHORIZED`, `401 INVALID_CREDENTIALS`, `401 TERMINAL_API_KEY_EXPIRED`, `404 TERMINAL_NOT_FOUND`, `422 TERMINAL_SUSPENDED`, `422 TERMINAL_REVOKED`, `422 TERMINAL_NOT_REGISTERED`, `422 OPERATOR_NOT_AUTHORIZED_ON_TERMINAL`, `422 OPERATOR_SUSPENDED`, `422 ACTOR_CLOSED` | Returns the operator session JWT required by all transaction endpoints. |
| POST | `/api/v1/terminal/auth/logout` | Terminal or Operator JWT | `Authorization` | none | `204 No Content` | `401 UNAUTHORIZED`, `401 TOKEN_EXPIRED`, `401 TOKEN_REVOKED`, `403 FORBIDDEN` | Revokes only the current `jti`. Clear the local token afterwards. |

### 5.2 NFC Challenge

| Method | Path | Auth | Headers | Request | Response | Primary errors | Frontend notes |
|---|---|---|---|---|---|---|---|
| POST | `/api/v1/terminal/nfc/challenge` | Operator JWT | `Authorization` | `NfcChallengeRequest` | `200 ApiResponse<NfcChallengeResponse>` | `400 VALIDATION_FIELD_REQUIRED`, `400 VALIDATION_ERROR`, `401 UNAUTHORIZED`, `403 FORBIDDEN`, `429 TERMINAL_RATE_LIMIT` | Required only when doing challenge-response card auth. Returns `challengeId` + `challengeHex` to forward to the card. Rate-limited per IP (per-second and per-hour). |

### 5.3 Payment

| Method | Path | Auth | Headers | Request | Response | Primary errors | Frontend notes |
|---|---|---|---|---|---|---|---|
| POST | `/api/v1/terminal/transactions/payment` | Operator JWT | `Authorization`, `Idempotency-Key`, optional `X-Correlation-Id` | `TerminalPaymentRequest` | `200 ApiResponse<TerminalPaymentResponse>` (executed) or `202 ApiResponse<TerminalPaymentResponse>` (control fired) | `400 VALIDATION_FIELD_REQUIRED`, `400 VALIDATION_ERROR`, `400 MISSING_IDEMPOTENCY_KEY`, `401 UNAUTHORIZED`, `403 FORBIDDEN`, `404 CARD_NOT_FOUND`, `404 CUSTOMER_NOT_FOUND`, `404 TERMINAL_NOT_FOUND`, `404 MERCHANT_NOT_FOUND`, `404 WALLET_NOT_FOUND`, `404 OPERATOR_NOT_FOUND`, `409 DUPLICATE_IDEMPOTENCY_KEY`, `422 TERMINAL_SUSPENDED`, `422 TERMINAL_REVOKED`, `422 TERMINAL_NOT_REGISTERED`, `422 TERMINAL_API_KEY_EXPIRED`, `422 CARD_BLOCKED`, `422 CARD_LOST`, `422 CARD_STOLEN`, `422 CARD_EXPIRED`, `422 CARD_NOT_ACTIVE`, `422 CARD_AUTH_FAILED`, `422 PIN_LOCKED`, `422 OPERATOR_SUSPENDED`, `422 OPERATOR_NOT_AUTHORIZED_ON_TERMINAL`, `422 ACTOR_SUSPENDED`, `422 WALLET_FROZEN`, `422 WALLET_SUSPENDED`, `422 WALLET_CLOSED`, `422 INSUFFICIENT_BALANCE`, `422 LIMIT_EXCEEDED`, `422 CONFIG_LIMIT_PROFILE_NOT_FOUND`, `422 CONFIG_RULE_INACTIVE` | `terminalId` and `operatorId` come from the JWT, never the body. `200` = executed, `202` = a PIN/confirmation control fired and must be cleared then resubmitted. |

---

## 6. Request Schemas

Types:

- `uuid` = UUID string
- `instant` = ISO-8601 date-time string
- `long` = integer amount in KMF minor unit representation used by backend
- `hex` = hex-encoded string

### 6.1 Auth

```ts
TerminalLoginRequest = {
  serialNumber: string;      // not blank, max 100
  apiKey: string;            // not blank, max 255
}

OperatorLoginRequest = {
  phoneCountryCode: string;  // not blank, max 10
  phoneNumber: string;       // digits only, max 20
  pin: string;               // not blank, 4..12 chars
}
```

`POST /api/v1/terminal/auth/logout` takes no body.

### 6.2 NFC Challenge

```ts
NfcChallengeRequest = {
  cardUid: string;           // exactly 14 hex chars (7-byte DESFire UID)
  method?: CardAuthMethod;   // optional; omit to use the server default method
}
```

### 6.3 Payment

```ts
TerminalPaymentRequest = {
  cardUid: string;                 // exactly 14 hex chars (7-byte DESFire UID)
  amount: long;                    // strictly positive
  challengeId?: string;            // from POST /nfc/challenge — must be paired with cardAuthResponse
  cardAuthResponse?: hex;          // hex-encoded raw card response — must be paired with challengeId
  pinValidated?: boolean;          // default false; set true to clear a PENDING_PIN control
  confirmationAcknowledged?: boolean; // default false; set true to clear a PENDING_CONFIRMATION control
}
```

Pairing rule: `challengeId` and `cardAuthResponse` must be **both present or both absent**. When both are absent, the payment proceeds in `UID_ONLY` mode (Phase 1 behaviour).

---

## 7. Response Schemas

### 7.1 Auth

```ts
TerminalTokenResponse = {
  tokenType: "Bearer";
  accessToken: string;
  accessTokenExpiresAt: instant;
  terminalId: uuid;
  merchantId: uuid;
}
```

Used by both `/auth/login` and `/auth/operator-login`. There is no `refreshToken` field — terminal sessions never refresh.

### 7.2 NFC Challenge

```ts
NfcChallengeResponse = {
  challengeId: string;       // pass back in the payment request
  challengeHex: hex;         // forward these bytes to the card
  method: CardAuthMethod;    // the method the server selected
  expiresAt: instant;        // challenge TTL — single-use
}
```

### 7.3 Payment

```ts
TerminalPaymentResponse = {
  outcome: TransactionControlOutcome; // EXECUTED | PENDING_PIN | PENDING_CONFIRMATION
  matchedThresholdAmount: long | null; // the threshold that triggered the control; null when EXECUTED
  transactionId: uuid | null;          // populated only when EXECUTED
  status: TransactionStatus | null;    // populated only when EXECUTED
  requestedAmount: long;               // always populated
  feeAmount: long | null;              // populated only when EXECUTED
  netAmountToMerchant: long | null;    // populated only when EXECUTED
  currency: "KMF";
  completedAt: instant | null;         // populated only when EXECUTED
  replayed: boolean | null;            // true when returned from an idempotent replay
}
```

Two shapes:

- **Executed** (`HTTP 200`, `outcome=EXECUTED`): `transactionId`, `status`, `feeAmount`, `netAmountToMerchant`, `completedAt`, `replayed` are populated; `matchedThresholdAmount` is `null`.
- **Control fired** (`HTTP 202`, `outcome=PENDING_PIN` or `PENDING_CONFIRMATION`): only `outcome`, `matchedThresholdAmount`, `requestedAmount`, `currency` are populated. No transaction exists.

`PENDING_APPROVAL` is not returned to terminals — no PAYMENT approval workflow exists; a threshold misconfigured for approval surfaces as a server-side error, not a `202`.

---

## 8. NFC Card Authentication

The Terminal app supports two card-authentication modes for a payment.

### 8.1 UID_ONLY (Phase 1, default fallback)

- The terminal taps the card, reads the 14-hex-char DESFire UID.
- It submits the payment with `cardUid` only, omitting `challengeId` and `cardAuthResponse`.
- Security rests on the operator JWT + the locked DESFire UID, not on a card-side cryptogram.

### 8.2 Challenge-Response (`NFC_SIMULATED`, `NFC_CHALLENGE_RESPONSE`)

1. Tap the card, read the UID.
2. `POST /api/v1/terminal/nfc/challenge` with `cardUid` (and optionally a `method`). The server returns `challengeId`, `challengeHex`, `method`, `expiresAt`.
3. Forward `challengeHex` bytes to the card; the card computes a response.
4. `POST /api/v1/terminal/transactions/payment` with `challengeId` + the hex-encoded `cardAuthResponse`.
5. The server atomically consumes the challenge (single-use anti-replay), checks expiry, cross-checks the UID, and verifies the response. A failure returns `422 CARD_AUTH_FAILED`.

Challenge rules:

- The challenge is **single-use** — consumed on first verification attempt, success or failure.
- The challenge has a TTL (`expiresAt`); a stale challenge fails verification.
- The challenge is bound to the exact `cardUid` it was issued for — a UID mismatch fails verification.
- `NFC_SIMULATED` is DEV/TEST only and must never be relied on in production.

---

## 9. Enums

| Enum | Values |
|---|---|
| `ActorType` | `CUSTOMER`, `MERCHANT`, `AGENT`, `MERCHANT_OPERATOR`, `BACKOFFICE_USER`, `SYSTEM` |
| `TerminalStatus` | `REGISTERED`, `ACTIVE`, `SUSPENDED`, `REVOKED` |
| `OperatorStatus` | `ACTIVE`, `SUSPENDED`, `REVOKED` |
| `MerchantStatus` | `PENDING_KYC`, `ACTIVE`, `SUSPENDED`, `CLOSED` |
| `CardStatus` | `ISSUED`, `ACTIVE`, `BLOCKED`, `LOST`, `STOLEN`, `EXPIRED`, `CLOSED` |
| `CardAuthMethod` | `UID_ONLY`, `NFC_SIMULATED`, `NFC_CHALLENGE_RESPONSE`, `CHALLENGE_RESPONSE`, `PIN` |
| `TransactionType` | `CASH_IN`, `PAYMENT`, `CASH_OUT`, `CARD_SALE`, `AGENT_FUND_IN`, `AGENT_FUND_OUT`, `FEE_COLLECTION`, `COMMISSION_PAYOUT`, `REVERSAL`, `P2P_TRANSFER`, `MERCHANT_TO_MERCHANT`, `SERVICE_PAYMENT`, `CARD_REPLACEMENT`, `BILL_PROVIDER_SETTLEMENT`, `PLATFORM_REVENUE_WITHDRAWAL`, `PLATFORM_LIQUIDITY_TOP_UP` |
| `TransactionStatus` | `PENDING`, `AUTHORIZED`, `COMPLETED`, `DECLINED`, `EXPIRED`, `REVERSED` |
| `TransactionControlOutcome` | `EXECUTED`, `PENDING_PIN`, `PENDING_CONFIRMATION`, `PENDING_APPROVAL` |
| `ChannelType` | `TERMINAL_NFC`, `TERMINAL_MANUAL`, `MOBILE_APP`, `AGENT_CHANNEL`, `WEB_APP`, `BACKOFFICE_UI`, `BACKOFFICE_JOB` |
| `WalletStatus` | `ACTIVE`, `FROZEN`, `SUSPENDED`, `CLOSED` |

For the payment endpoint, a terminal will only ever observe `EXECUTED`, `PENDING_PIN`, or `PENDING_CONFIRMATION` in `TransactionControlOutcome`. Card auth on a terminal-driven PAYMENT is stamped as `UID_ONLY` (no challenge) or the resolved challenge method.

---

## 10. Permissions

### 10.1 Endpoint Authorization

Terminal APIs do not use the backoffice `Permission` enum. Authorization uses Spring Security role authority derived from the JWT actor type:

| Endpoint | Required authority |
|---|---|
| `POST /api/v1/terminal/auth/login` | none (public) |
| `POST /api/v1/terminal/auth/operator-login` | `ROLE_MERCHANT` or `ROLE_MERCHANT_OPERATOR` |
| `POST /api/v1/terminal/auth/logout` | `ROLE_MERCHANT` or `ROLE_MERCHANT_OPERATOR` |
| `POST /api/v1/terminal/nfc/challenge` | `ROLE_MERCHANT_OPERATOR` (and the JWT must carry `terminalId`) |
| `POST /api/v1/terminal/transactions/payment` | `ROLE_MERCHANT_OPERATOR` (and the JWT must carry `terminalId`) |

### 10.2 Session Layering Rule

- A bare **terminal** JWT (`actorType=MERCHANT`) can call `operator-login` and `logout` only.
- An **operator** JWT (`actorType=MERCHANT_OPERATOR` with `terminalId`) is required for `nfc/challenge` and `transactions/payment`. A `MERCHANT_OPERATOR` token without a `terminalId` claim is rejected with `403 FORBIDDEN`.

Server-side role, actor-type, terminal-status, operator-status, card-status, wallet-status, balance, and limit checks remain authoritative.

---

## 11. Operational Rules

### 11.1 Terminal Frontend Flows

Device startup flow:

- Call `POST /api/v1/terminal/auth/login` with `serialNumber` + `apiKey`.
- Store `accessToken` and `accessTokenExpiresAt`.
- On any `401` from a downstream call, re-run `/auth/login` (no refresh token exists).
- If login returns `TERMINAL_SUSPENDED` / `TERMINAL_REVOKED` / `TERMINAL_NOT_REGISTERED`, the device must surface a "contact Backoffice" state — it cannot self-recover.

Cashier shift flow:

- With a valid terminal JWT, call `POST /api/v1/terminal/auth/operator-login` with the cashier phone + PIN.
- Store the operator `accessToken` (8-hour TTL) and use it for all transaction endpoints.
- On operator-session expiry, prompt the cashier to log in again.
- Call `POST /api/v1/terminal/auth/logout` at end of shift.

Payment flow (UID_ONLY):

- Tap the card, read the 14-hex-char UID.
- Call `POST /api/v1/terminal/transactions/payment` with `cardUid`, `amount`, and an `Idempotency-Key`.
- `200 outcome=EXECUTED` → show the transaction result.
- `202 outcome=PENDING_PIN` → collect the customer PIN through the existing PIN mechanism, then resubmit **with the same `Idempotency-Key`** and `pinValidated=true`.
- `202 outcome=PENDING_CONFIRMATION` → show the confirmation prompt with `matchedThresholdAmount`, get explicit acknowledgement, then resubmit **with the same `Idempotency-Key`** and `confirmationAcknowledged=true`.

Payment flow (challenge-response):

- Tap the card, read the UID.
- Call `POST /api/v1/terminal/nfc/challenge` with the UID; receive `challengeId` + `challengeHex`.
- Forward `challengeHex` to the card, collect the card response.
- Call `POST /api/v1/terminal/transactions/payment` with `cardUid`, `amount`, `challengeId`, `cardAuthResponse`, and an `Idempotency-Key`.
- Handle `200` / `202` exactly as in the UID_ONLY flow. If a control fires and you resubmit, you must obtain a **fresh** challenge — the previous one was consumed.

### 11.2 Idempotency

`POST /api/v1/terminal/transactions/payment` requires an `Idempotency-Key` header. A missing or blank key returns `400 MISSING_IDEMPOTENCY_KEY`.

Idempotency behaviour:

- A reused key whose original submission **executed** returns the previous transaction response with `replayed=true`; no wallet or ledger movement happens a second time.
- A concurrent duplicate key can return `409 DUPLICATE_IDEMPOTENCY_KEY`; retrying with the same key then hits the replay path.
- When a control fires (`202`), **no transaction was created**, so the key is not yet bound. Resubmit with the **same** key plus the cleared control flag — this is the intended idempotent continuation, not a new payment.
- Each distinct card tap / amount must use a **new** key.

No other Terminal endpoint requires `Idempotency-Key`.

### 11.3 Frontend Gating

The Terminal app has no profile or capability endpoint. Gating is purely reactive:

- Treat any `401` as "session expired / invalid" — re-authenticate the appropriate layer.
- Treat `403 FORBIDDEN` as "wrong session layer" — an operator session is required for transactions.
- Treat terminal-status `422` codes (`TERMINAL_SUSPENDED`, `TERMINAL_REVOKED`, `TERMINAL_NOT_REGISTERED`) as non-recoverable on-device — require Backoffice action.
- Treat card-status `422` codes (`CARD_BLOCKED`, `CARD_LOST`, `CARD_STOLEN`, `CARD_EXPIRED`, `CARD_NOT_ACTIVE`, `PIN_LOCKED`) as "decline this card" — show a clear message and let the cashier retry with another card or method.
- Treat `INSUFFICIENT_BALANCE` and `LIMIT_EXCEEDED` as customer-side declines.

The backend is authoritative for every check; the frontend must never assume success before the `200` response.

### 11.4 Empty Bodies

`POST /api/v1/terminal/auth/logout` takes no JSON body. All other Terminal endpoints require a JSON body as specified in [Section 6](#6-request-schemas).

---

## 12. Evidence Index

| Area | Source class |
|---|---|
| Terminal auth endpoints | `terminal.api.TerminalAuthController` |
| Terminal / operator session logic | `terminal.application.TerminalAuthenticationService`, `TerminalAuthenticationService.TerminalToken` |
| Terminal auth requests | `terminal.api.TerminalLoginRequest`, `terminal.api.OperatorLoginRequest` |
| Terminal token response | `terminal.api.TerminalTokenResponse` |
| Terminal aggregate & lifecycle | `terminal.domain.Terminal`, `terminal.domain.TerminalStatus` |
| Payment endpoint | `terminal.api.TerminalPaymentController` |
| Payment request / response | `terminal.api.TerminalPaymentRequest`, `terminal.api.TerminalPaymentResponse` |
| Payment submission & control gate | `transaction.application.PaymentSubmissionUseCase`, `PaymentSubmissionResult`, `TransactionControlOutcome` |
| Payment execution | `transaction.application.PaymentUseCase`, `PaymentCommand`, `PaymentResult` |
| NFC challenge endpoint | `card.api.NfcChallengeController`, `card.api.dto.NfcChallengeRequest`, `card.api.dto.NfcChallengeResponse` |
| Card authentication service | `card.application.CardAuthService`, `card.domain.CardAuthResult`, `shared.domain.CardAuthMethod` |
| HTTP envelopes | `shared.infrastructure.web.ApiResponse`, `ApiError`, `shared.infrastructure.exception.GlobalExceptionHandler` |
| Security & rate limit | `shared.infrastructure.config.SecurityConfig`, `shared.infrastructure.web.RateLimitingFilter`, `RateLimitProperties`, `KomoPayHeaders` |
| JWT claims & authorities | `security.infrastructure.JwtService`, `security.domain.JwtPrincipal` |
| Error codes | `shared.infrastructure.exception.ErrorCode` |
| Enums | `terminal.domain.TerminalStatus`, `identity.domain.OperatorStatus`, `identity.domain.MerchantStatus`, `card.domain.CardStatus`, `shared.domain.CardAuthMethod`, `shared.domain.TransactionType`, `transaction.domain.TransactionStatus`, `transaction.application.TransactionControlOutcome`, `shared.domain.ChannelType`, `wallet.domain.WalletStatus` |

---

End of document. All content above is derived from the current KomoPay backend codebase only.
