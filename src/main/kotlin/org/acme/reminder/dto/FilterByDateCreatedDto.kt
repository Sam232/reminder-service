package org.acme.reminder.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

class FilterByDateCreatedDto {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var startAt: LocalDateTime? = null

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var endAt: LocalDateTime? = null

    var pageNumber: String = "1"

    var pageSize: String = "1"
}