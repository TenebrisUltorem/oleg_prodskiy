package ru.vtb.szkf.oleg.prodsky.handler

import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import ru.vtb.szkf.oleg.prodsky.extensions.logger

object HelpCommandHandler {

    @JvmStatic
    private val log = logger()

    private val HELP_MESSAGE = """
        Роскошного времени суток. Меня зовут Олег (не Тиньков) 
        и я помогаю б̶е̶с̶т̶о̶л̶к̶о̶в̶ы̶м̶ разработчикам не путать свои смены дежурства по проду.
        
        Вот команды для коммуникации со мной:
            /addAttendant <@ИмяСчастливчика> - Добавить нового дежурного
            /getAttendant - Узнать имя сегодняшнего дежурящего
            /attendantList - Получить список всех дежурных
            /deleteAttendant <@ИмяНеудачника> - Удалить дежурного из списка (скажем, на время отпуска)
            /start Запуск джобы для уведолмения о сегодняшнем дежурстве
    """.trimIndent()

    val handleHelpCommand: suspend CommandHandlerEnvironment.() -> Unit = {
        log.debug("Получена команда на получение мануала")
        val chatId = ChatId.fromId(message.chat.id)
        try {
            bot.sendMessage(chatId, text = HELP_MESSAGE)
        }
        finally {
            update.consume()
        }
    }
}
