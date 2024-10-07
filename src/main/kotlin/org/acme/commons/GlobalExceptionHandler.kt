package org.acme.commons

import jakarta.persistence.PersistenceException
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.acme.commons.dtos.APIResponseDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.DateTimeException

@Provider
class GlobalExceptionHandler: ExceptionMapper<Exception> {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun toResponse(p0: Exception): Response {
        return mapExceptionToResponse(p0)
    }

    private fun mapExceptionToResponse(exception: Exception): Response {
        var status = 400
        var message = "Request failed"
        val sessionId: String?

        when (exception) {
            is WebApplicationException -> {
                val messageArr = exception.message?.split(" - ")
                sessionId = messageArr?.get(0)
                message = messageArr?.get(1) ?: message
                logger.error("Web application exception: {}", "", exception)
            }
            is PersistenceException -> {
                status = 500
                val messageArr = exception.message?.split(" - ")
                sessionId = messageArr?.get(0)
                logger.error("Database persistence exception: {}", "", exception);
            }
            is DateTimeException -> {
                val messageArr = exception.message?.split(" - ")
                sessionId = messageArr?.get(0)
                message = messageArr?.get(1) ?: message
                logger.error("Date time exception: {}", "", exception);
            }
            else -> {
                status = 500
                val messageArr = exception.message?.split(" - ")
                sessionId = messageArr?.get(0)
                logger.error("Unexpected server error: {}", "", exception);
            }
        }

        val apiResponse = APIResponseDto(msg = message, code = FAILURE_CODE, data = null, pid = sessionId)
        val response = Response.status(status).entity(apiResponse).build();

        return response
    }
}