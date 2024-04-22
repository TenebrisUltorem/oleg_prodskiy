package ru.vtb.szkf.oleg.prodsky.handler

import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import ru.vtb.szkf.oleg.prodsky.extensions.logger
import ru.vtb.szkf.oleg.prodsky.service.AttendantJobService
import ru.vtb.szkf.oleg.prodsky.service.AttendantService

object AttendantJobCommandHandler {

    private val log = logger()

    val handleStartCommand: suspend CommandHandlerEnvironment.() -> Unit = {
        val chatId = ChatId.fromId(message.chat.id)
        try {
            if (AttendantJobService.launchNewAttendantJobForChat(message.chat.id)) {
                log.info("Запущена джоба по ежеCRONной смене дежурного для чатика {}", chatId)
                bot.sendMessage(chatId, text = "Итак, время дежурить по проду")
                bot.sendMessage(chatId, text = AttendantService.getTodayAttendant(message.chat.id))
            }
            else {
                bot.sendMessage(chatId, text = "Джоба по смене дежурного для чатика '$chatId' уже существует")
                log.warn("Джоба по смене дежурного для чатика '{}' уже существует", chatId)
            }
        }
        catch (e: Throwable) {
            log.error("Во время запуска джобы по ежеCRONной смене дежурного для чатика '{}' произошла ошибка",
                chatId, e)
            bot.sendMessage(chatId, text = "Ошибочка вышла. Ну, если что-то поймете, то вот: $e")
        }
        finally {
            update.consume()
        }
    }

}
