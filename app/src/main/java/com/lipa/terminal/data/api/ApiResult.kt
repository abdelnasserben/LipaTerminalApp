package com.lipa.terminal.data.api

import com.lipa.terminal.data.model.ApiError

sealed class ApiResult<out T> {
    data class Success<T>(val value: T, val httpStatus: Int) : ApiResult<T>()
    data class Failure(val httpStatus: Int, val error: ApiError) : ApiResult<Nothing>()
}
