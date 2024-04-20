package ru.vtb.szkf.oleg.prodsky.handler

import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import ru.vtb.szkf.oleg.prodsky.extensions.logger
import ru.vtb.szkf.oleg.prodsky.scheduling.Scheduler
import ru.vtb.szkf.oleg.prodsky.service.AttendantService

object StartCommandHandler {

    private val log = logger()

    val handleStartCommand: suspend CommandHandlerEnvironment.() -> Unit = {
        val chatId = ChatId.fromId(message.chat.id)
        try {
            val job = Scheduler.launchAttendantJobForChat(message.chat.id, bot)

            if (job == null) {
                bot.sendMessage(chatId, text = "Джоба по смене дежурного для чатика '$chatId' уже существует")
                log.warn("Джоба по смене дежурного для чатика '{}' уже существует", chatId)
            }
            else {
                log.info("Запущена джоба по ежеCRONной смене дежурного для чатика {}", chatId)
                bot.sendMessage(chatId, text = "Итак, время дежурить по проду")
                bot.sendMessage(chatId, text = AttendantService.getTodayAttendant(message.chat.id))
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
