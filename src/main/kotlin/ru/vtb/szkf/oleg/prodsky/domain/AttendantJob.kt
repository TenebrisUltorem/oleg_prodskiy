package ru.vtb.szkf.oleg.prodsky.domain

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object AttendantJobTable: IdTable<Long>("attendant_jobs") {
    val chatId = long("chat_id").uniqueIndex()

    override val id: Column<EntityID<Long>> = chatId.entityId()
}

class AttendantJob(id: EntityID<Long>): Entity<Long>(id) {
    companion object: EntityClass<Long, AttendantJob>(AttendantJobTable)

    var chatId by AttendantJobTable.chatId
}
