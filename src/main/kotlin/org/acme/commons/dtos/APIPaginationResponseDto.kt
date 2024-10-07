package org.acme.commons.dtos

data class APIPaginationResponseDto <T> (
    var msg: String? = null,
    var code: String? = null,
    var totalPages: Int? = null,
    var pageSize: Int? = null,
    var data: T? = null,
    var pid: String? = null
)