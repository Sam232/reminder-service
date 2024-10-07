package org.acme.reminder.http

import org.acme.commons.dtos.APIPaginationResponseDto
import org.acme.commons.dtos.APIResponseDto
import org.acme.reminder.dto.AddReminderDto
import org.acme.reminder.dto.GetReminderDto
import org.acme.reminder.dto.UpdateReminderDto

interface ReminderResource {
    fun create(addReminderDto: AddReminderDto): APIResponseDto<GetReminderDto>

    fun filterByRemindDate(
            reminderSent: String,
            startAt: String,
            endAt: String,
            pageNumber: String,
            pageSize: String
    ): APIPaginationResponseDto<List<GetReminderDto>>

    fun filterByDateCreated(
            startAt: String,
            endAt: String,
            pageNumber: String,
            pageSize: String
    ): APIPaginationResponseDto<List<GetReminderDto>>

    fun update(updateReminderDto: UpdateReminderDto): APIResponseDto<GetReminderDto>

    fun delete(id: String): APIResponseDto<GetReminderDto>

    fun send(): APIResponseDto<String>
}