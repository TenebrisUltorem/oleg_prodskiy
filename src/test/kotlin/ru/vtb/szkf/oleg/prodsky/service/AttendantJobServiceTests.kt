package ru.vtb.szkf.oleg.prodsky.service

import org.jetbrains.exposed.sql.transactions.transaction
import ru.vtb.szkf.oleg.prodsky.AbstractTests
import ru.vtb.szkf.oleg.prodsky.domain.AttendantJob
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

object AttendantJobServiceTests: AbstractTests() {

    @Test
    fun shouldSuccessfullyLaunchJob() = transaction {
        val isJobLaunched = AttendantJobService.launchNewAttendantJobForChat(testChatId)

        assertTrue(isJobLaunched)
        assertEquals(testChatId, AttendantJob.findById(testChatId)?.chatId)
    }

    @Test
    fun shouldNotLaunchJobIfJobForChatExists() = transaction {
        AttendantJobService.launchNewAttendantJobForChat(testChatId)
        val isJobLaunched = AttendantJobService.launchNewAttendantJobForChat(testChatId)

        assertFalse(isJobLaunched)
    }

    @Test
    fun shouldSuccessfullyStopJob() = transaction {
        AttendantJobService.launchNewAttendantJobForChat(testChatId)

        val isJobStopped = AttendantJobService.stopAttendantJobForChat(testChatId)

        assertTrue(isJobStopped)
        assertNull(AttendantJob.findById(testChatId))
    }

}
