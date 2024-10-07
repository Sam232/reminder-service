package org.acme.reminder.service

import io.quarkus.hibernate.orm.panache.kotlin.PanacheQuery
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Default
import jakarta.inject.Inject
import org.acme.reminder.repo.Reminder
import org.acme.reminder.repo.ReminderRepo
import org.acme.utils.Utils
import org.slf4j.LoggerFactory
import java.util.function.Consumer

//@ApplicationScoped
class ReminderAutomationServiceImpl(
        private val sessionId: String,
        private val page: PanacheQuery<Reminder>
): Consumer<Reminder> {
    @field:Default
    @field:Inject
//    private lateinit var reminderRepo: ReminderRepo
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    override fun accept(reminder: Reminder) {
        val requestStr = Utils.convertObjectToString(reminder, sessionId)
        logger.info("$sessionId - Page ${page.page().index + 1}, will produce reminder to queue :: $requestStr")
    }
}