package org.acme.commons.dtos

data class APIResponseDto<T> (
    val code: String,
    val msg: String,
    val pid: String? = null,
    val data: T?
)