package ru.vtb.szkf.oleg.prodsky.extensions.domain

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import ru.vtb.szkf.oleg.prodsky.domain.Attendant
import ru.vtb.szkf.oleg.prodsky.domain.AttendantTable

fun Attendant.Companion.findAllByChatId(chatId: Long) = Attendant
    .find { AttendantTable.chatId eq chatId }.toList()

fun Attendant.Companion.findCurrentAttendantByChatId(chatId: Long) = Attendant
    .find { (AttendantTable.isAttendant eq true) and (AttendantTable.chatId eq chatId) }
    .firstOrNull()

fun Attendant.Companion.findByUsernameAndChatId(username: String, chatId: Long) = Attendant
    .find { (AttendantTable.username eq username) and (AttendantTable.chatId eq chatId) }
    .firstOrNull()

fun Attendant.Companion.existsByUsernameAndChatId(username: String, chatId: Long) = Attendant
    .count((AttendantTable.username eq username) and (AttendantTable.chatId eq chatId)) != 0L
