package org.acme.reminder.repo

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.acme.reminder.NotificationChannelsEnum
import org.hibernate.annotations.CurrentTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.io.Serializable
import java.time.LocalDateTime


@Entity
@Table(
    name = "tbl_reminders",
    indexes = [
        Index(name = "reminder_sent_remind_at_created_at_index", columnList = "reminder_sent, remind_at, created_at")
    ]
)
@NamedQueries(
    NamedQuery(name = "Reminder.filterByRemindDate", query = "from Reminder where reminderSent = ?1 and remindAt between ?2 and ?3 order by id desc"),
    NamedQuery(name = "Reminder.filterByDateCreated", query = "from Reminder where createdAt between ?1 and ?2 order by id asc"),
    NamedQuery(name = "Reminder.filterDueReminders", query = "from Reminder where reminderSent = false and remindAt = ?1")
)

open class Reminder: PanacheEntityBase, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null

    @NotBlank
    @Column(length = 255)
    open var note: String? = null

    @Column(name = "reminder_sent")
    open var reminderSent: Boolean = false

    @NotBlank
    @Column(name = "notification_channel")
    open var notificationChannel: String? = null

    @NotBlank
    @Column(name = "receiver")
    open var receiver: String? = null


    @NotNull
    @Column(name = "remind_at")
    open var remindAt: LocalDateTime? = null

    @Column(name = "created_at")
    open var createdAt: LocalDateTime? = LocalDateTime.now()

    @UpdateTimestamp
    @Column(name = "updated_at")
    open var updatedAt: LocalDateTime? = null
}