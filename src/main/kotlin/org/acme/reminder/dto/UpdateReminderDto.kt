package org.acme.reminder.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class UpdateReminderDto {
    @NotNull
    var id: Long? = null

    @NotBlank
    var note: String? = null

    @NotBlank
    var notificationChannel: String? = null

    @NotBlank
    var receiver: String? = null

    var reminderSent: Boolean = false
}