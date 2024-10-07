package org.acme.reminder.service

import org.acme.commons.dtos.APIPaginationResponseDto
import org.acme.commons.dtos.APIResponseDto
import org.acme.reminder.dto.*
import java.time.LocalDateTime

interface ReminderService {
    fun create(reminder: AddReminderDto, sessionId: String): APIResponseDto<GetReminderDto>

    fun <T> filter (filter: T, sessionId: String, action: String): APIPaginationResponseDto<List<GetReminderDto>>

    fun update(updateReminder: UpdateReminderDto, sessionId: String): APIResponseDto<GetReminderDto>

    fun delete(id: Long, sessionId: String): APIResponseDto<GetReminderDto>

    fun remindUsersOnDueAction(sessionId: String)
}