package ru.vtb.szkf.oleg.prodsky.handler

import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import ru.vtb.szkf.oleg.prodsky.extensions.logger
import ru.vtb.szkf.oleg.prodsky.service.AttendantService

object AttendantCommandHandler {

    @JvmStatic
    private val log = logger()

    val handleGetAttendantCommand: suspend CommandHandlerEnvironment.() -> Unit = {
        log.debug("Получена команда на получение сегодняшнего дежурного")

        val chatId = ChatId.fromId(message.chat.id)
        try {
            bot.sendMessage(chatId, text = AttendantService.getTodayAttendant(message.chat.id))
        }
        catch (e: Throwable) {
            log.error("Во время получения сегодняшнего дежурного произошла ошибка", e)
            bot.sendMessage(chatId, text = "Ошибочка вышла. Ну, если что-то поймете, то вот: $e")
        }
        finally {
            update.consume()
        }
    }

    val handleGetAttendantListCommand: suspend CommandHandlerEnvironment.() -> Unit = {
        val chatId = ChatId.fromId(message.chat.id)

        log.debug("Получена команда на получение списка дежурных чатика '{}'", chatId)

        try {
            bot.sendMessage(chatId, text = AttendantService.getAttendantList(message.chat.id))
        }
        catch (e: Throwable) {
            log.error("Во время получения списка дежурных произошла ошибка", e)
            bot.sendMessage(chatId, text = "Ошибочка вышла. Ну, если что-то поймете, то вот: $e")
        }
        finally {
            update.consume()
        }
    }

    val handleAddAttendantCommand: suspend CommandHandlerEnvironment.() -> Unit = {
        val chatId = ChatId.fromId(message.chat.id)
        log.debug("Получена команда на добавление нового дежурного с аргументами '{}' для чатика '{}'",
            args, chatId
        )

        try {
            bot.sendMessage(chatId, text = AttendantService.addAttendant(args.firstOrNull(), message.chat.id))
        }
        catch (e: IllegalArgumentException) {
            log.error("Некорректный ввод аргументов для операции добавления нового дежурного")
            bot.sendMessage(
                chatId,
                text = e.message ?: "@${message.from?.username}, научись уже нормально вводить аргументы")
        }
        catch (e: Throwable) {
            log.error("Во время добавления нового дежурного произошла ошибка", e)
            bot.sendMessage(chatId, text = "Ошибочка вышла. Ну, если что-то поймете, то вот: $e")
        }
        finally {
            update.consume()
        }

    }

    val handleDeleteAttendantCommand: suspend CommandHandlerEnvironment.() -> Unit = {
        val chatId = ChatId.fromId(message.chat.id)

        log.debug("Получена команда на удаление дежурного с аргументами '{}' для чатика '{}'",
            args, chatId)

        try {
            bot.sendMessage(chatId, text = AttendantService.deleteAttendant(args.firstOrNull(), message.chat.id))
        }
        catch (e: IllegalArgumentException) {
            log.error("Некорректный ввод аргументов для операции удаления дежурного")
            bot.sendMessage(
                chatId,
                text = e.message ?: "@${message.from?.username}, научись уже нормально вводить аргументы"
            )
        }
        catch (e: Throwable) {
            log.error("Во время удаления дежурного произошла ошибка", e)
            bot.sendMessage(chatId, text = "Ошибочка вышла. Ну, если что-то поймете, то вот: $e")
        }
        finally {
            update.consume()
        }
    }

}
