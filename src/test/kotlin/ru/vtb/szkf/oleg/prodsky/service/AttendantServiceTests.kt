package ru.vtb.szkf.oleg.prodsky.service

import io.mockk.every
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.assertThrows
import ru.vtb.szkf.oleg.prodsky.AbstractTests
import ru.vtb.szkf.oleg.prodsky.configuration.Configuration
import ru.vtb.szkf.oleg.prodsky.domain.Attendant
import ru.vtb.szkf.oleg.prodsky.domain.AttendantTable
import ru.vtb.szkf.oleg.prodsky.integration.ProductionCalendarWebClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object AttendantServiceTests: AbstractTests() {

    private const val VALID_ATTENDANT_USERNAME = "@testUser"

    private const val ADD_FIRST_ATTENDANT_SUCCESS_MESSAGE = "$VALID_ATTENDANT_USERNAME, поздравляем, " +
        "ты теперь дежурный! Можешь приступать - сегодня твоя смена."
    private const val ADD_NEXT_ATTENDANT_SUCCESS_MESSAGE =  "$VALID_ATTENDANT_USERNAME, добро пожаловать в клуб!"
    private const val ATTENDANT_DUPLICATE_MESSAGE = "$VALID_ATTENDANT_USERNAME и так уже в списке. " +
        "Хотите лишнюю смену докинуть?"
    private const val I_WILL_NEVER_BE_ATTENDANT = "Мне делать что-ли нечего? Ваш прод, вы и дежурьте"
    private const val NO_USERNAME_FOR_ADDING = "Кого добавлять то?"
    private const val NO_USERNAME_FOR_DELETING = "Кого удалять то?"
    private const val NO_ATTENDANTS_FOUND = "А дежурить то и некому..."
    private const val VACATION = "А сегодня выходной, отыхаем"

    @Test
    fun shouldSuccessfullyAddFirstAttendant() = transaction {
        val resultMessage = AttendantService.addAttendant(VALID_ATTENDANT_USERNAME, testChatId)

        assertEquals(ADD_FIRST_ATTENDANT_SUCCESS_MESSAGE, resultMessage)
        val attendant = Attendant.find { AttendantTable.username eq VALID_ATTENDANT_USERNAME }.firstOrNull()
        assertNotNull(attendant)
        assertTrue(attendant!!.isAttendant)
    }

    @Test
    fun shouldSuccessfullyAddNextAttendant() = transaction {
        Attendant.new { username = "@Vitaliy"; isAttendant = true; chatId = testChatId }

        val resultMessage = AttendantService.addAttendant(VALID_ATTENDANT_USERNAME, testChatId)

        assertEquals(ADD_NEXT_ATTENDANT_SUCCESS_MESSAGE, resultMessage)
        val attendant = Attendant.find { AttendantTable.username eq VALID_ATTENDANT_USERNAME }.firstOrNull()
        assertNotNull(attendant)
        assertFalse(attendant!!.isAttendant)
    }

    @Test
    fun shouldFailAddingNewAttendantCauseOfWrongUsername() {
        val err = assertThrows<IllegalArgumentException> {
            AttendantService.addAttendant("Кокисов Иван Пафнутьевич", testChatId)
        }

        assertEquals("Кокисов Иван Пафнутьевич - это кто вообще?", err.message)
    }

    @Test
    fun shouldFailAddingNewAttendantIfUsernameIsNull() = transaction {
        val err = assertThrows<IllegalArgumentException> { AttendantService.addAttendant(null, testChatId) }
        assertEquals(NO_USERNAME_FOR_ADDING, err.message)
    }

    @Test
    fun shouldFailAddingNewAttendantIfHeWasAlreadyAdded() = transaction {
        Attendant.new { username = VALID_ATTENDANT_USERNAME; isAttendant = true; chatId = testChatId }

        val err = assertThrows<IllegalArgumentException> {
            AttendantService.addAttendant(VALID_ATTENDANT_USERNAME, testChatId)
        }
        assertEquals(ATTENDANT_DUPLICATE_MESSAGE, err.message)
    }

    @Test
    fun shouldFailAddingNewAttendantIfUserTriesToMakeBotAnAttendant() = transaction {
        val err = assertThrows<IllegalArgumentException> {
            AttendantService.addAttendant(Configuration.botNames.first(), testChatId)
        }
        assertEquals(I_WILL_NEVER_BE_ATTENDANT, err.message)
    }

    @Test
    fun shouldSuccessfullyGetAttendantList() = transaction {
        Attendant.new { username = "@Vitaliy"; isAttendant = true; chatId = testChatId }
        Attendant.new { username = VALID_ATTENDANT_USERNAME; isAttendant = false; chatId = testChatId }

        val resultMessage = AttendantService.getAttendantList(testChatId)

        assertEquals("По проду у нас дежурят @Vitaliy, @testUser", resultMessage)
    }

    @Test
    fun shouldSuccessfullyGetTodayAttendant() = transaction {
        every { ProductionCalendarWebClient.isTodayAVacation() } returns false
        Attendant.new { username = "@Vitaliy"; isAttendant = true; chatId = testChatId }
        Attendant.new { username = VALID_ATTENDANT_USERNAME; isAttendant = false; chatId = testChatId }

        val resultMessage = AttendantService.getTodayAttendant(testChatId)
        assertEquals("Сегодня дежурит по прому @Vitaliy", resultMessage)
    }

    @Test
    fun shouldNotGetTodayAttendantAtVacation() = transaction {
        every { ProductionCalendarWebClient.isTodayAVacation() } returns true
        Attendant.new { username = "@Vitaliy"; isAttendant = true; chatId = testChatId }

        val resultMessage = AttendantService.getTodayAttendant(testChatId)
        assertEquals(VACATION, resultMessage)
    }

    @Test
    fun shouldSuccessfullyDeleteAttendant() = transaction {
        Attendant.new { username = "@Vitaliy"; isAttendant = true; chatId = testChatId }
        Attendant.new { username = VALID_ATTENDANT_USERNAME; isAttendant = false; chatId = testChatId }

        val resultMessage = AttendantService.deleteAttendant("@Vitaliy", testChatId)
        assertEquals("@Vitaliy больше не дежурит\n@testUser, принимай смену дежурства", resultMessage)

        assertTrue(Attendant.find { AttendantTable.username eq VALID_ATTENDANT_USERNAME }.firstOrNull()!!.isAttendant)
    }

    @Test
    fun shouldSuccessfullyDeleteTheOnlyAttendant() = transaction {
        Attendant.new { username = "@Vitaliy"; isAttendant = true; chatId = testChatId }

        val resultMessage = AttendantService.deleteAttendant("@Vitaliy", testChatId)
        assertEquals("@Vitaliy больше не дежурит\nТеперь дежурить некому. Замечательно.", resultMessage)

        assertTrue(Attendant.all().empty())
    }

    @Test
    fun shouldFailDeletingAttendantCauseOfWrongUsername() {
        val err = assertThrows<IllegalArgumentException> {
            AttendantService.deleteAttendant("Кокисов Иван Пафнутьевич", testChatId)
        }

        assertEquals("Кокисов Иван Пафнутьевич - это кто вообще?", err.message)
    }

    @Test
    fun shouldFailDeletingAttendantIfUsernameIsNull() = transaction {
        val err = assertThrows<IllegalArgumentException> { AttendantService.deleteAttendant(null, testChatId) }
        assertEquals(NO_USERNAME_FOR_DELETING, err.message)
    }

    @Test
    fun shouldFailDeletingAttendantIfUserDoesNotExist() = transaction {
        val err = assertThrows<IllegalArgumentException> {
            AttendantService.deleteAttendant(VALID_ATTENDANT_USERNAME, testChatId)
        }
        assertEquals("@testUser и так не дежурит", err.message)
    }

    @Test
    fun shouldSuccessfullySwitchAttendant() = transaction {
        every { ProductionCalendarWebClient.isTodayAVacation() } returns false
        Attendant.new { username = "@Vitaliy"; isAttendant = true; chatId = testChatId }
        Attendant.new { username = VALID_ATTENDANT_USERNAME; isAttendant = false; chatId = testChatId }

        val responseMessage = AttendantService.switchAttendant(testChatId)

        val testAttendant = Attendant
            .find { (AttendantTable.username eq VALID_ATTENDANT_USERNAME) and (AttendantTable.chatId eq testChatId) }
            .first()

        val prevAttendant = Attendant
            .find { (AttendantTable.username eq "@Vitaliy") and (AttendantTable.chatId eq testChatId) }
            .first()

        assertEquals("@testUser, принимай смену дежурства", responseMessage)
        assertTrue(testAttendant.isAttendant)
        assertFalse(prevAttendant.isAttendant)
    }

    @Test
    fun shouldFailSwitchingAttendantCauseOfNoAttendantFound() = transaction {
        every { ProductionCalendarWebClient.isTodayAVacation() } returns false
        val responseMessage = AttendantService.switchAttendant(testChatId)

        assertEquals(NO_ATTENDANTS_FOUND, responseMessage)
    }

}
