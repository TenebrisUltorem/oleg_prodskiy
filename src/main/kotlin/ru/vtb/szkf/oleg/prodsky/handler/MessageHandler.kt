package ru.vtb.szkf.oleg.prodsky.handler

import com.github.kotlintelegrambot.dispatcher.handlers.TextHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import ru.vtb.szkf.oleg.prodsky.configuration.Configuration
import ru.vtb.szkf.oleg.prodsky.extensions.logger

object MessageHandler {

    @JvmStatic
    private val log = logger()

    val handleText: suspend TextHandlerEnvironment.() -> Unit = {
        log.info("Got text '{}'", text)

        if (Configuration.botNames.any { text.contains(it) }) {
            bot.sendMessage(
                chatId = ChatId.fromId(message.chat.id),
                text = "Коллеги, я пока что на бета версии. Список доступных команд можно получить по команде /help",
                disableNotification = false,
            )
            update.consume()
        }

    }

}
