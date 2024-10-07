package org.acme.reminder.service

import io.quarkus.hibernate.orm.panache.kotlin.PanacheQuery
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Default
import jakarta.inject.Inject
import jakarta.persistence.PersistenceException
import jakarta.transaction.Transactional
import jakarta.ws.rs.WebApplicationException
import org.acme.commons.FAILURE_CODE
import org.acme.commons.SUCCESS_CODE
import org.acme.commons.dtos.APIPaginationResponseDto
import org.acme.commons.dtos.APIResponseDto
import org.acme.reminder.*
import org.acme.reminder.dto.*
import org.acme.reminder.repo.Reminder
import org.acme.reminder.repo.ReminderRepo
import org.acme.utils.Utils
import org.hibernate.QueryException
import org.hibernate.QueryTimeoutException
import org.modelmapper.ModelMapper
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.sql.SQLException
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.stream.Collectors

@ApplicationScoped
class ReminderServiceImpl : ReminderService {
    @field:Default
    @field:Inject
    private lateinit var reminderRepo: ReminderRepo

    private val modelMapper = ModelMapper()

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun create(reminder: AddReminderDto, sessionId: String): APIResponseDto<GetReminderDto> {
        logger.info("$sessionId - Mapping reminderDto to reminder entity")

        val newReminder = modelMapper.map(reminder, Reminder::class.java) ?:
            throw Exception("$sessionId - Failed to map reminder dto to reminder entity")

        if (newReminder.remindAt!! <= newReminder.createdAt) {
            throw WebApplicationException("$sessionId - remindAt cannot be less than current datetime")
        }

        try {
            logger.info("$sessionId - Creating new reminder")
            reminderRepo.persist(newReminder)
        } catch (pex: PersistenceException) {
            throw PersistenceException("$sessionId - Failed to create new reminder")
        }

        logger.info("$sessionId - New reminder created")
        val savedReminder = modelMapper.map(newReminder, GetReminderDto::class.java) ?:
            throw Exception("$sessionId - Failed to map new reminder object to get reminder dto")

        return APIResponseDto(code = SUCCESS_CODE, msg = CREATED_REMINDER_SUCCESSFUL, data = savedReminder, pid = sessionId)
    }

    override fun <T> filter(filter: T, sessionId: String, action: String): APIPaginationResponseDto<List<GetReminderDto>> {
        logger.info("$sessionId - About to fetch list of reminders")

        val result: APIPaginationResponseDto<List<GetReminderDto>>

        try {
            result = when (action) {
                FILTER_BY_REMIND_AT -> {
                    filter as FilterByRemindDateDto
                    logger.info("$sessionId - Fetching list of reminders")
                    val filterResult = reminderRepo.filterByRemindDate(filter.reminderSent, filter.startAt!!, filter.endAt!!)
                    analyzeExpectedPaginationResult(filterResult, filter.pageNumber, filter.pageSize, sessionId)
                }
                FILTER_BY_DATE_CREATED -> {
                    filter as FilterByDateCreatedDto
                    logger.info("$sessionId - Fetching list of reminders by date")
                    val filterResult = reminderRepo.filterByDateCreated(filter.startAt!!, filter.endAt!!)
                    analyzeExpectedPaginationResult(filterResult, filter.pageNumber, filter.pageSize, sessionId)
                }
                else -> {
                    throw Exception("$sessionId - Unknown filter action '$action'")
                }
            }
        } catch (qex: QueryException) {
            throw QueryException("$sessionId - Query reminder failed. Reason : ${qex.message}")
        } catch (qex: QueryTimeoutException) {
            throw QueryTimeoutException("$sessionId - Query too long. Reason : ${qex.message}", SQLException(), "")
        } catch (ex: Exception) {
            throw Exception("$sessionId - Failed to perform filter. Reason : ${ex.message}")
        }

        logger.info("$sessionId - Reminders fetched")

        return result
    }

    private fun getById(id: Long, sessionId: String): Reminder? {
        val reminder: Reminder?

        try {
            reminder = reminderRepo.findById(id)
        } catch (qex: QueryException) {
            throw QueryException("$sessionId - Query reminder failed. Reason : ${qex.message}")
        } catch (qex: QueryTimeoutException) {
            throw QueryTimeoutException("$sessionId - Query too long. Reason : ${qex.message}", SQLException(), "")
        } catch (ex: Exception) {
            throw Exception("$sessionId - Failed to perform filter. Reason : ${ex.message}")
        }

        reminder == null && throw WebApplicationException("$sessionId - Provided id '$id' doesn't match any reminder id")

        return reminder
    }

    @Transactional
    override fun update(updateReminder: UpdateReminderDto, sessionId: String): APIResponseDto<GetReminderDto> {
        val reminderToUpdate = getById(updateReminder.id!!, sessionId)

        reminderToUpdate!!.note = updateReminder.note
        reminderToUpdate.notificationChannel = updateReminder.notificationChannel
        reminderToUpdate.receiver = updateReminder.receiver
        reminderToUpdate.reminderSent = updateReminder.reminderSent

        try {
            reminderRepo.persistAndFlush(reminderToUpdate)
        } catch (pex: PersistenceException) {
            throw PersistenceException("$sessionId - Failed to update reminder. Reason : ${pex.message}")
        }

        val updatedReminder = modelMapper.map(reminderToUpdate, GetReminderDto::class.java) ?:
            throw Exception("$sessionId - Failed to map updated entity to get reminder dto")

        return APIResponseDto(code = SUCCESS_CODE, msg = REMINDER_UPDATED, data = updatedReminder, pid = sessionId)
    }

    @Transactional
    override fun delete(id: Long, sessionId: String): APIResponseDto<GetReminderDto> {
        val reminderToDelete = getById(id, sessionId)

        try {
            reminderRepo.deleteById(id)
        } catch (ex: PersistenceException) {
            throw PersistenceException("$sessionId - Failed to delete reminder. Reason : ${ex.message}")
        }

        val deletedReminder = modelMapper.map(reminderToDelete, GetReminderDto::class.java) ?:
            throw Exception("$sessionId - Failed to map object of deleted entity to get reminder dto")

        return APIResponseDto(code = SUCCESS_CODE, msg = REMINDER_DELETED, data = deletedReminder, pid = sessionId)
    }

    @Transactional
    override fun remindUsersOnDueAction(sessionId: String) {
        val remindersToPushToQueue: List<GetReminderDto> = mutableListOf()
        val dueReminders: PanacheQuery<Reminder>

        val formattedDatetime: LocalDateTime
        var currentDateTime: String

        try {
            currentDateTime = Utils.getCurrentDateTime().format(Utils.DATE_TIME_FORMAT_2)
            currentDateTime = "2024-04-03 02:17"
            formattedDatetime = LocalDateTime.parse(currentDateTime, Utils.DATE_TIME_FORMAT_2)
        } catch (dex: DateTimeException) {
            throw Exception("$sessionId - Failed to get current datetime to use for due reminder query. Reason : ${dex.message}")
        }

        try {
            logger.info("$sessionId - Fetching reminders due on $currentDateTime")
            dueReminders = reminderRepo.filterDueReminders(formattedDatetime)
        } catch (ex: Exception) {
            throw Exception("$sessionId - Failed to fetch reminders. Reason : ${ex.message}")
        }

        if (dueReminders.count() > 0) {
            logger.info("$sessionId - Reminders found")
            val dueRemindersPage = dueReminders.page(0, 2)
            logger.info("Number of pages count is ${dueReminders.pageCount()}")
            val page = dueRemindersPage.firstPage()
            page.stream().forEach(ReminderAutomationServiceImpl(sessionId, page))
            while (page.hasNextPage()) {
                page.nextPage().stream().forEach(ReminderAutomationServiceImpl(sessionId, page))
            }
        } else {
            logger.info("$sessionId - No reminders due on $currentDateTime")
        }
    }

    private fun analyzeExpectedPaginationResult(
            result: PanacheQuery<Reminder>,
            pageNumber: String,
            pageSize: String,
            sessionID: String
    ): APIPaginationResponseDto<List<GetReminderDto>> {
        val reminders: List<Reminder> = result.page(pageNumber.toInt() - 1, pageSize.toInt())
                .stream().collect(Collectors.toList())
        val totalPages = result.pageCount()

        val listOfReminder: MutableList<GetReminderDto> = mutableListOf()

        if (reminders.isNotEmpty()) {
            reminders.stream().forEach { car ->
                val reminder: GetReminderDto = modelMapper.map(car, GetReminderDto::class.java)
                        ?: throw Exception("$sessionID - $FAILED_TO_MAP_FETCHED_REMINDERS_TO_REMINDER_DTO")
                listOfReminder.add(reminder)
            }
        }

        val response: APIPaginationResponseDto<List<GetReminderDto>> = if (listOfReminder.isNotEmpty()) {
            logger.info("$sessionID - Reminders found")
            APIPaginationResponseDto(
                    msg = REMINDERS_FETCHED, code = SUCCESS_CODE, totalPages = totalPages,
                    pageSize = pageSize.toInt(), data = listOfReminder, pid = sessionID
            )
        } else {
            logger.info("$sessionID - No reminders found")
            APIPaginationResponseDto(msg = NO_REMINDERS_FOUND, code = FAILURE_CODE, pid = sessionID)
        }
        return response
    }
}