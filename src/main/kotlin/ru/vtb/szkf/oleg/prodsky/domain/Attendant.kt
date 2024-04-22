package ru.vtb.szkf.oleg.prodsky.domain

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

private const val USERNAME_LENGTH = 32

object AttendantTable: IntIdTable("attendants") {
    val username = varchar("username", USERNAME_LENGTH)
    val isAttendant = bool("is_attendant")
    val chatId = long("chat_id")

    init {
        uniqueIndex(username, chatId)
    }
}

class Attendant(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Attendant>(AttendantTable)

    var username by AttendantTable.username
    var isAttendant by AttendantTable.isAttendant
    var chatId by AttendantTable.chatId
}
