package com.lipa.terminal.data.api

import com.lipa.terminal.data.model.ApiError
import com.lipa.terminal.data.model.ApiResponse
import com.lipa.terminal.data.model.NfcChallengeRequest
import com.lipa.terminal.data.model.NfcChallengeResponse
import com.lipa.terminal.data.model.OperatorLoginRequest
import com.lipa.terminal.data.model.TerminalLoginRequest
import com.lipa.terminal.data.model.TerminalPaymentRequest
import com.lipa.terminal.data.model.TerminalPaymentResponse
import com.lipa.terminal.data.model.TerminalTokenResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val API_PREFIX = "/api/v1/terminal"
private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

class HttpTerminalApi(
    private val baseUrl: String,
    private val client: OkHttpClient = defaultClient(),
    private val json: Json = defaultJson,
) : TerminalApi {

    override suspend fun login(req: TerminalLoginRequest): ApiResult<TerminalTokenResponse> =
        post("$API_PREFIX/auth/login", body = req, auth = null)

    override suspend fun operatorLogin(
        terminalToken: String,
        req: OperatorLoginRequest,
    ): ApiResult<TerminalTokenResponse> =
        post("$API_PREFIX/auth/operator-login", body = req, auth = terminalToken)

    override suspend fun logout(accessToken: String): ApiResult<Unit> =
        postNoContent("$API_PREFIX/auth/logout", auth = accessToken)

    override suspend fun nfcChallenge(
        operatorToken: String,
        req: NfcChallengeRequest,
    ): ApiResult<NfcChallengeResponse> =
        post("$API_PREFIX/nfc/challenge", body = req, auth = operatorToken)

    override suspend fun submitPayment(
        operatorToken: String,
        idempotencyKey: String,
        correlationId: String?,
        req: TerminalPaymentRequest,
    ): ApiResult<TerminalPaymentResponse> {
        val extra = buildMap {
            put("Idempotency-Key", idempotencyKey)
            if (!correlationId.isNullOrBlank()) put("X-Correlation-Id", correlationId)
        }
        return post("$API_PREFIX/transactions/payment", body = req, auth = operatorToken, extraHeaders = extra)
    }

    private suspend inline fun <reified Req : Any, reified Res : Any> post(
        path: String,
        body: Req,
        auth: String?,
        extraHeaders: Map<String, String> = emptyMap(),
    ): ApiResult<Res> {
        val payload = json.encodeToString(serializer<Req>(), body)
        val request = buildRequest(path, payload, auth, extraHeaders)
        return execute(request) { raw -> json.decodeFromString(envelopeSerializer<Res>(), raw).data }
    }

    private suspend fun postNoContent(path: String, auth: String?): ApiResult<Unit> {
        val request = buildRequest(path, payload = "", auth = auth, extraHeaders = emptyMap())
        return execute(request) { Unit }
    }

    private fun buildRequest(
        path: String,
        payload: String,
        auth: String?,
        extraHeaders: Map<String, String>,
    ): Request {
        val headers = buildMap {
            put("Accept", "application/json")
            if (!auth.isNullOrBlank()) put("Authorization", "Bearer $auth")
            putAll(extraHeaders)
        }.toHeaders()
        val builder = Request.Builder()
            .url(baseUrl.trimEnd('/') + path)
            .headers(headers)
        builder.post(payload.toRequestBody(JSON_MEDIA))
        return builder.build()
    }

    private suspend fun <T> execute(
        request: Request,
        parseSuccess: (String) -> T,
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).await()
            val status = response.code
            val raw = response.body?.string().orEmpty()
            response.close()
            if (status in 200..299) {
                if (status == 204 || raw.isBlank()) {
                    @Suppress("UNCHECKED_CAST")
                    ApiResult.Success(Unit as T, status)
                } else {
                    ApiResult.Success(parseSuccess(raw), status)
                }
            } else {
                ApiResult.Failure(status, parseError(raw, status))
            }
        } catch (e: IOException) {
            ApiResult.Failure(0, networkError(e))
        } catch (e: Exception) {
            ApiResult.Failure(0, networkError(e))
        }
    }

    private fun parseError(raw: String, status: Int): ApiError {
        if (raw.isBlank()) return fallbackError(status, "Empty error body")
        return runCatching {
            val tree = json.parseToJsonElement(raw).jsonObject
            val node = tree["error"]?.takeIf { it is JsonObject } ?: tree
            json.decodeFromJsonElement(ApiError.serializer(), node)
        }.getOrElse { fallbackError(status, raw.take(200)) }
    }

    private fun fallbackError(status: Int, message: String): ApiError = ApiError(
        code = when (status) {
            401 -> "UNAUTHORIZED"
            403 -> "FORBIDDEN"
            404 -> "NOT_FOUND"
            in 500..599 -> "SERVER_ERROR"
            else -> "HTTP_$status"
        },
        message = message,
        details = emptyList(),
        correlationId = UUID.randomUUID().toString(),
        timestamp = Instant.now().toString(),
    )

    private fun networkError(e: Throwable): ApiError = ApiError(
        code = "NETWORK_ERROR",
        message = e.message ?: "Network error",
        details = emptyList(),
        correlationId = UUID.randomUUID().toString(),
        timestamp = Instant.now().toString(),
    )

    companion object {
        val defaultJson: Json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        private inline fun <reified T : Any> envelopeSerializer() =
            ApiResponse.serializer(serializer<T>())

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}

private suspend fun Call.await(): Response = suspendCoroutine { cont ->
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            cont.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            cont.resume(response)
        }
    })
}
