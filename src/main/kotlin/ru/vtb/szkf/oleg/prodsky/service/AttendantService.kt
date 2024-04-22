package ru.vtb.szkf.oleg.prodsky.service

import org.jetbrains.exposed.sql.transactions.transaction
import ru.vtb.szkf.oleg.prodsky.configuration.Configuration
import ru.vtb.szkf.oleg.prodsky.domain.Attendant
import ru.vtb.szkf.oleg.prodsky.domain.AttendantTable
import ru.vtb.szkf.oleg.prodsky.extensions.domain.existsByUsernameAndChatId
import ru.vtb.szkf.oleg.prodsky.extensions.domain.findAllByChatId
import ru.vtb.szkf.oleg.prodsky.extensions.domain.findByUsernameAndChatId
import ru.vtb.szkf.oleg.prodsky.extensions.domain.findCurrentAttendantByChatId
import ru.vtb.szkf.oleg.prodsky.extensions.logger
import ru.vtb.szkf.oleg.prodsky.integration.ProductionCalendarWebClient

object AttendantService {

    private const val NO_ATTENDANTS_FOUND = "А дежурить то и некому..."

    @JvmStatic
    private val log = logger()

    /**
     * Возвращает список дежурных
     *
     * @return Сообщение для отправки в чат
     */
    fun getAttendantList(chatId: Long): String  = transaction {
        log.debug("Получен запрос на получение списка дежурных по проду")

        val attendants = Attendant.findAllByChatId(chatId)

        log.info("Успешно получен список дежурных по проду '{}'", attendants)

        return@transaction if (attendants.isEmpty()) NO_ATTENDANTS_FOUND
                           else "По проду у нас дежурят ${attendants.joinToString(", ") { it.username }}"
    }

    /**
     * Возвращает текущего дежурного
     *
     * @return Сообщение для отправки в чат
     */
    fun getTodayAttendant(chatId: Long): String = transaction {
        log.debug("Получен запрос на получение текущего дежурного")

        if (ProductionCalendarWebClient.isTodayAVacation()) {
            log.info("Сегодня выходной, дежурный не требуется")
            return@transaction "А сегодня выходной, отыхаем"
        }

        val attendant = Attendant.findCurrentAttendantByChatId(chatId) ?: run {
            return@transaction NO_ATTENDANTS_FOUND
        }

        log.info("Успешно получен сегодняшний дежурный '{}'", attendant.username)

        return@transaction "Сегодня дежурит по прому ${attendant.username}"
    }

    /**
     * Добавляет нового дежурного по проду
     * @param attendantUsername Имя пользователя нового дежурного
     * @return Сообщение для отправки в чат
     */
    fun addAttendant(attendantUsername: String?, chatId: Long): String = transaction {
        log.debug("Получен запрос на добавление нового дежурного по проду '{}'", attendantUsername)

        require(!Configuration.botNames.contains(attendantUsername)) {
            "Мне делать что-ли нечего? Ваш прод, вы и дежурьте"
        }
        requireNotNull(attendantUsername) { "Кого добавлять то?" }
        require(attendantUsername.startsWith("@")) { "$attendantUsername - это кто вообще?" }


        require(!Attendant.existsByUsernameAndChatId(attendantUsername, chatId)) {
            "$attendantUsername и так уже в списке. Хотите лишнюю смену докинуть?"
        }

        val attendantCount = Attendant.count()

        Attendant.new {
            this.username = attendantUsername
            this.isAttendant = attendantCount == 0L
            this.chatId = chatId
        }

        log.info("Успешно добавлен новый дежурный '{}'", attendantUsername)

        return@transaction if (attendantCount == 0L)
            "$attendantUsername, поздравляем, ты теперь дежурный! Можешь приступать - сегодня твоя смена."
        else "$attendantUsername, добро пожаловать в клуб!"
    }

    /**
     * Удаляет дежурного и назначает на сегодняшнее дежурство следующего по списку
     *
     * @param attendantUsername Имя пользователя удаляемого дежурного
     * @return Сообщение для отправки в чат
     */
    fun deleteAttendant(attendantUsername: String?, chatId: Long): String {
        log.debug("Получен запрос на удаление дежурного по проду '{}'", attendantUsername)

        requireNotNull(attendantUsername) { "Кого удалять то?" }
        require(attendantUsername.startsWith("@")) { "$attendantUsername - это кто вообще?" }

        return transaction {
            var result = String()
            val attendantToRemove = Attendant.findByUsernameAndChatId(attendantUsername, chatId)

            requireNotNull(attendantToRemove) { "$attendantUsername и так не дежурит" }

            result += "${attendantToRemove.username} больше не дежурит"

            if (attendantToRemove.isAttendant) {
                val nextAttendant = findNextAttendant(attendantToRemove)

                result += if (nextAttendant.username == attendantToRemove.username)
                    "\nТеперь дежурить некому. Замечательно."
                else {
                    nextAttendant.isAttendant = true
                    "\n${nextAttendant.username}, принимай смену дежурства"
                }
            }

            attendantToRemove.delete()
            result
        }
    }

    /**
     * Переключает дежурного по проду
     *
     * @return Сообщение для отправки в чат
     */
    fun switchAttendant(chatId: Long): String? = transaction {
        log.debug("Запуск переключения дежурного по проду")

        if (ProductionCalendarWebClient.isTodayAVacation()) {
            log.info("Сегодня выходной, переключение не требуется")
            return@transaction null
        }

        if (Attendant.find { AttendantTable.chatId eq chatId }.empty()) return@transaction "А дежурить то и некому..."

        val prevAttendant = Attendant.findCurrentAttendantByChatId(chatId) ?: run {
            return@transaction NO_ATTENDANTS_FOUND
        }

        val nextAttendant = findNextAttendant(prevAttendant)

        return@transaction if (nextAttendant.username == prevAttendant.username)
            "${nextAttendant.username}, ты один, так что дежурь дальше"
        else {
            nextAttendant.isAttendant = true
            prevAttendant.isAttendant = false

            "${nextAttendant.username}, принимай смену дежурства"
        }.also { log.debug("Дежурным по проду назначен '{}'", nextAttendant.username) }
    }


    private fun findNextAttendant(currAttendant: Attendant): Attendant = transaction {
        val attendants = Attendant.all().toList()

        var newAttendant = currAttendant
        for (i: Int in attendants.indices) {
            if (attendants[i].username == currAttendant.username) {
                newAttendant = if (i == attendants.lastIndex) attendants[0] else attendants[i + 1]
                break
            }
        }

        newAttendant
    }

}
