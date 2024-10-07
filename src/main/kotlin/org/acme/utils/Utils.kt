package org.acme.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.ws.rs.WebApplicationException
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID

object Utils {
    val DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val DATE_TIME_FORMAT_2: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun generateSessionId(): String {
        return UUID.randomUUID().toString()
    }

    fun <T> convertObjectToString(requestBody: T, sessionId: String): String {
        val stringRequest: String

        try {
            val objectMap = ObjectMapper().registerModule(JavaTimeModule())
            stringRequest = objectMap.writeValueAsString(requestBody)
        } catch (ex: Exception) {
            throw WebApplicationException("$sessionId - Failed to parse request to string")
        }

        logger.info("$sessionId - Request parsed to string")

        return stringRequest
    }

    fun getCurrentDateTime(): LocalDateTime {
        return LocalDateTime.now()
    }

    fun parseDateTime(
            currDateTime: LocalDateTime,
            dateTimeStr: String,
            localTime: LocalTime,
            sessionId: String
    ): LocalDateTime {
        val formattedDateTime: LocalDateTime = if (dateTimeStr.isBlank()) {
            try {
                val dateTime = currDateTime.with(localTime).format(DATE_TIME_FORMAT).toString()
                logger.info("$sessionId - No datetime was provided. Default datetime set to $dateTime")
                LocalDateTime.parse(dateTime, DATE_TIME_FORMAT)
            } catch (ex: Exception) {
                throw Exception("$sessionId - Failed to parse default dateTime '$dateTimeStr' to LocalDateTime object. Reason : ${ex.message}")
            }
        } else {
            try {
                LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMAT)
            } catch (dex: DateTimeException) {
                throw DateTimeException("$sessionId - Invalid date time received to be parsed : ${dex.message} ")
            }
        }

        return formattedDateTime
    }


}