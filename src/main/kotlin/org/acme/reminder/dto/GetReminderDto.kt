package org.acme.reminder.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

class GetReminderDto {
    @NotNull
    var id: Long? = null

    @NotBlank
    var note: String? = null

    @NotBlank
    var notificationChannel: String? = null

    @NotBlank
    var receiver: String? = null

    var reminderSent: Boolean = false

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    var remindAt: LocalDateTime? = null

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    var createdAt: LocalDateTime? = null

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    var updatedAt: LocalDateTime? = null
}