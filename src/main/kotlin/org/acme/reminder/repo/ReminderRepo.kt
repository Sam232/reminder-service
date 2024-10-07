package org.acme.reminder.repo

import io.quarkus.hibernate.orm.panache.kotlin.PanacheQuery
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import io.smallrye.common.annotation.Blocking
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime

@ApplicationScoped
class ReminderRepo: PanacheRepositoryBase<Reminder, Long> {
    fun filterByRemindDate(
        reminderSent: Boolean,
        startAt: LocalDateTime,
        endAt: LocalDateTime
    ): PanacheQuery<Reminder> {
        return find("#Reminder.filterByRemindDate", reminderSent, startAt, endAt)
    }

    fun filterByDateCreated(startAt: LocalDateTime, endAt: LocalDateTime): PanacheQuery<Reminder> {
        return find("#Reminder.filterByDateCreated", startAt, endAt)
    }

    fun filterDueReminders(dueDateTime: LocalDateTime): PanacheQuery<Reminder> {
        return find("#Reminder.filterDueReminders", dueDateTime)
    }
}