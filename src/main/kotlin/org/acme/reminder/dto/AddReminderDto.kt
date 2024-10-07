package org.acme.reminder.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

class AddReminderDto {
    @NotBlank
    var note: String? = null

    @NotBlank
    var notificationChannel: String? = null

    @NotBlank
    var receiver: String? = null

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    var remindAt: LocalDateTime? = null
}