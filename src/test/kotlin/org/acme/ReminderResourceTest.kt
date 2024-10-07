package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.acme.reminder.NotificationChannelsEnum
import org.acme.reminder.dto.AddReminderDto
import org.acme.reminder.dto.UpdateReminderDto
import org.acme.utils.Utils
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test;
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.co.jemos.podam.api.PodamFactoryImpl

@QuarkusTest
open class ReminderResourceTest {
    private val baseUrl = "/api/v1"
    private val podamFactoryImpl: PodamFactoryImpl = PodamFactoryImpl()
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Test
    fun testCreateReminderResource() {
        val requestBody = podamFactoryImpl.manufacturePojo(AddReminderDto::class.java)
//        val remindAtIncrement = requestBody.remindAt?.plusDays(1)
        val remindAtIncrement = requestBody.remindAt?.plusMinutes(1)
        requestBody.remindAt = remindAtIncrement
        requestBody.notificationChannel = NotificationChannelsEnum.EMAIL.name
        requestBody.receiver = "jread912@gmail.com"
        val sessionId = Utils.generateSessionId()
        val jsonRequest = Utils.convertObjectToString<AddReminderDto>(requestBody, sessionId)
        logger.info("$sessionId - Sending create reminder request with body :: $jsonRequest")

        RestAssured.given()
                .body(requestBody).contentType(ContentType.JSON)
                .`when`().post("$baseUrl/reminders")
                .then()
                .log().all()
                .time(Matchers.lessThan(5000L))
                .statusCode(200)
                .assertThat()
                .body("code", CoreMatchers.equalTo("00"))
    }

    @Test
    fun testFilterRemindersByDateCreatedResource() {
        logger.info("Sending request to get reminders by date created")

        val queryParams: HashMap<String, String> = HashMap()
        queryParams["startAt"] = "2024-04-01 00:00:00"
        queryParams["endAt"] = "2024-04-03 23:59:59"
        queryParams["pageSize"] = "5"
        queryParams["pageNumber"] = "1"

        RestAssured.given()
                .`when`().params(queryParams)
                .get("$baseUrl/reminders/filter/2")
                .then()
                .log().all()
                .time(Matchers.lessThan(5000L))
                .statusCode(200)
                .assertThat()
                .body("code", CoreMatchers.equalTo("00"))
    }

    @Test
    fun testFilterRemindersByRemindAtResource() {
        logger.info("Sending request to get reminders by remindAt")

        val queryParams: HashMap<String, String> = HashMap()
        queryParams["reminderSent"] = "true"
        queryParams["startAt"] = "2024-04-01 00:00:00"
        queryParams["endAt"] = "2024-04-03 23:59:59"
        queryParams["pageSize"] = "5"
        queryParams["pageNumber"] = "1"

        RestAssured.given()
                .`when`().params(queryParams)
                .get("$baseUrl/reminders/filter/1")
                .then()
                .log().all()
                .time(Matchers.lessThan(5000L))
                .statusCode(200)
                .assertThat()
                .body("code", CoreMatchers.equalTo("00"))
    }

    @Test
    fun testUpdateReminderResource() {
        val requestBody = podamFactoryImpl.manufacturePojo(UpdateReminderDto::class.java)
        requestBody.id = 2
        requestBody.reminderSent = true
        requestBody.notificationChannel = NotificationChannelsEnum.SMS.name
        requestBody.receiver = "233270000111"
        val sessionId = Utils.generateSessionId()
        val jsonRequest = Utils.convertObjectToString<UpdateReminderDto>(requestBody, sessionId)
        logger.info("Sending update reminder request with body :: $jsonRequest")

        RestAssured.given()
                .body(requestBody).contentType(ContentType.JSON)
                .`when`().put("$baseUrl/reminders")
                .then()
                .log().all()
                .time(Matchers.lessThan(5000L))
                .statusCode(200)
                .assertThat()
                .body("code", CoreMatchers.equalTo("00"))
    }

    @Test
    fun testDeleteReminderResource() {
        val id = 8
        logger.info("Sending request to delete reminder with id :: $id")

        RestAssured.given().contentType(ContentType.JSON)
                .`when`().delete("$baseUrl/reminders/$id")
                .then()
                .log().all()
                .time(Matchers.lessThan(5000L))
                .statusCode(200)
                .assertThat()
                .body("code", CoreMatchers.equalTo("00"))
    }

    @Test
    fun testSendDueRemindersResource() {
        logger.info("Sending request to send due reminders")

        RestAssured.given().contentType(ContentType.JSON)
                .`when`().post("$baseUrl/reminders/send")
                .then()
                .log().all()
                .time(Matchers.lessThan(5000L))
                .statusCode(200)
                .assertThat()
                .body("code", CoreMatchers.equalTo("03"))
    }

}