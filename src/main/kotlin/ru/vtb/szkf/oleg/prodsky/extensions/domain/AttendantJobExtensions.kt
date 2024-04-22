package ru.vtb.szkf.oleg.prodsky.extensions.domain

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import ru.vtb.szkf.oleg.prodsky.domain.AttendantJob
import ru.vtb.szkf.oleg.prodsky.domain.AttendantJobTable

fun AttendantJob.Companion.existsByChatId(chatId: Long) = AttendantJob
    .count(AttendantJobTable.chatId eq chatId) != 0L
