package org.acme.reminder.http

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import org.acme.commons.PROCESSING_CODE
import org.acme.commons.dtos.APIPaginationResponseDto
import org.acme.commons.dtos.APIResponseDto
import org.acme.reminder.FILTER_BY_DATE_CREATED
import org.acme.reminder.FILTER_BY_REMIND_AT
import org.acme.reminder.REQUEST_IN_PROGRESS
import org.acme.reminder.dto.*
import org.acme.reminder.service.ReminderService
import org.acme.utils.Utils
import org.slf4j.LoggerFactory
import java.time.LocalTime

@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ReminderResourceImpl @Inject constructor(val reminderService: ReminderService) : ReminderResource {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @POST
    @Path("reminders")
    @Transactional
    override fun create(addReminderDto: AddReminderDto): APIResponseDto<GetReminderDto> {
        val sessionId = Utils.generateSessionId()

        val requestStr = Utils.convertObjectToString(addReminderDto, sessionId)

        logger.info("$sessionId - Request received to create new reminder : $requestStr")

        val response = reminderService.create(addReminderDto, sessionId)
        val responseStr = Utils.convertObjectToString(response, sessionId)

        logger.info("$sessionId - Sending response : $responseStr")

        return response
    }

    @GET
    @Path("reminders/filter/1")
    override fun filterByRemindDate(
        @DefaultValue("false") @QueryParam("reminderSent") reminderSent: String,
        @DefaultValue("") @QueryParam("startAt") startAt: String,
        @DefaultValue("") @QueryParam("endAt") endAt: String,
        @DefaultValue("1") @QueryParam("pageNumber") pageNumber: String,
        @DefaultValue("10") @QueryParam("pageSize") pageSize: String
    ): APIPaginationResponseDto<List<GetReminderDto>> {
        val sessionId = Utils.generateSessionId()
        logger.info("$sessionId - Request received to filter reminders by reminder date. Filters include: " +
                "reminderSent: $reminderSent, startAt: $startAt, endAt: $endAt, " +
                "pageNumber: $pageNumber, pageSize: $pageSize")

        val currentDateTime = Utils.getCurrentDateTime()
        val formattedStartAt = Utils.parseDateTime(currentDateTime, startAt, LocalTime.MIN, sessionId)
        val formattedEndAt = Utils.parseDateTime(currentDateTime, endAt, LocalTime.MAX, sessionId)

        val reminderSentBool: Boolean

        try {
            reminderSentBool = reminderSent.toBoolean()
        } catch (ex: TypeCastException) {
            throw WebApplicationException("$sessionId - Invalid 'reminderSent' value found in request")
        }

        val filterDto = FilterByRemindDateDto()
        filterDto.reminderSent = reminderSentBool
        filterDto.startAt = formattedStartAt
        filterDto.endAt = formattedEndAt
        filterDto.pageNumber = pageNumber
        filterDto.pageSize = pageSize

        val response = reminderService.filter(filterDto, sessionId, FILTER_BY_REMIND_AT)
        val responseStr = Utils.convertObjectToString(response, sessionId)

        logger.info("$sessionId - Sending response : $responseStr")

        return response
    }

    @GET
    @Path("reminders/filter/2")
    override fun filterByDateCreated(
        @DefaultValue("") @QueryParam("startAt") startAt: String,
        @DefaultValue("") @QueryParam("endAt") endAt: String,
        @DefaultValue("1") @QueryParam("pageNumber") pageNumber: String,
        @DefaultValue("10") @QueryParam("pageSize") pageSize: String
    ): APIPaginationResponseDto<List<GetReminderDto>> {
        val sessionId = Utils.generateSessionId()
        logger.info("$sessionId - Request received to filter reminders by date created. Filters include: " +
                "startAt: $startAt, endAt: $endAt, pageNumber: $pageNumber, pageSize: $pageSize")

        val currentDateTime = Utils.getCurrentDateTime()
        val formattedStartAt = Utils.parseDateTime(currentDateTime, startAt, LocalTime.MIN, sessionId)
        val formattedEndAt = Utils.parseDateTime(currentDateTime, endAt, LocalTime.MAX, sessionId)

        val filterDto = FilterByDateCreatedDto()
        filterDto.startAt = formattedStartAt
        filterDto.endAt = formattedEndAt
        filterDto.pageNumber = pageNumber
        filterDto.pageSize = pageSize

        val response = reminderService.filter(filterDto, sessionId, FILTER_BY_DATE_CREATED)
        val responseStr = Utils.convertObjectToString(response, sessionId)

        logger.info("$sessionId - Sending response : $responseStr")

        return response
    }

    @PUT
    @Path("reminders")
    override fun update(updateReminderDto: UpdateReminderDto): APIResponseDto<GetReminderDto> {
        val sessionId = Utils.generateSessionId()

        val requestStr = Utils.convertObjectToString(updateReminderDto, sessionId)

        logger.info("$sessionId - Request received to update reminder : $requestStr")

        val response = reminderService.update(updateReminderDto, sessionId)

        val responseStr = Utils.convertObjectToString(response, sessionId)

        logger.info("$sessionId - Sending response : $responseStr")

        return response
    }

    @DELETE
    @Path("reminders/{id}")
    override fun delete(@PathParam("id") id: String): APIResponseDto<GetReminderDto> {
        val sessionId = Utils.generateSessionId()

        logger.info("$sessionId - Request received to delete reminder with id : $id")

        val idLong: Long

        try {
            idLong = id.toLong()
        } catch (nex: NumberFormatException) {
            throw WebApplicationException("$sessionId - Invalid reminder id [$id] received")
        }

        val response = reminderService.delete(idLong, sessionId)
        val responseStr = Utils.convertObjectToString(response, sessionId)

        logger.info("$sessionId - Sending response : $responseStr")

        return response
    }

    @POST
    @Path("reminders/send")
    override fun send(): APIResponseDto<String> {
        val sessionId = Utils.generateSessionId()

        logger.info("$sessionId - Request received to send due reminders")

//        Uni.createFrom().item { reminderService.remindUsersOnDueAction(sessionId) }
        val re = reminderService.remindUsersOnDueAction(sessionId)

        return APIResponseDto(code = PROCESSING_CODE, msg = REQUEST_IN_PROGRESS, pid = sessionId, data = null)
    }

}