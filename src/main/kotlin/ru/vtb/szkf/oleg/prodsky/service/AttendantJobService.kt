package ru.vtb.szkf.oleg.prodsky.service

import com.github.kotlintelegrambot.entities.ChatId
import dev.inmo.krontab.buildSchedule
import dev.inmo.krontab.doInfinity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.transaction
import ru.vtb.szkf.oleg.prodsky.BOT
import ru.vtb.szkf.oleg.prodsky.configuration.Configuration
import ru.vtb.szkf.oleg.prodsky.domain.AttendantJob
import ru.vtb.szkf.oleg.prodsky.extensions.domain.existsByChatId
import ru.vtb.szkf.oleg.prodsky.extensions.logger
import java.util.concurrent.ConcurrentHashMap

object AttendantJobService {
    private val kronScheduler = buildSchedule(Configuration.switchAttendantCron)
    private val jobs = ConcurrentHashMap<Long, Job>()

    @JvmStatic
    private val log = logger()

    init {
        transaction {
            AttendantJob.all().forEach {
                log.info("Запуск ранее созданной джобы для чатика '{}'", it.chatId)
                jobs[it.chatId] = launchJob(it.chatId)
            }
        }
    }

    /**
     * Запускает новую джобу по ежеCRONному назначению нового дежурного
     *
     * @return true - джоба успешно запущена, false - нет
     */
    fun launchNewAttendantJobForChat(chatId: Long): Boolean = transaction {
        if (AttendantJob.existsByChatId(chatId)) {
            log.warn("Джоба по смене дежурного в чатике '{}' уже запущена", chatId)
            return@transaction false
        }

        AttendantJob.new { this.chatId = chatId }
        jobs[chatId] = launchJob(chatId)

        return@transaction true
    }

    fun stopAttendantJobForChat(chatId: Long): Boolean = transaction {
        if (!AttendantJob.existsByChatId(chatId)) {
            log.warn("Отсутствует джоба по смене дежурного в чатике '{}'", chatId)
            return@transaction false
        }

        AttendantJob.findById(chatId)?.delete()
        jobs.remove(chatId)

        return@transaction true
    }

    private fun launchJob(chatId: Long) = CoroutineScope(Dispatchers.Default).launch {
        kronScheduler.doInfinity {
            try {
                val response = AttendantService.switchAttendant(chatId)
                if (response != null) BOT.sendMessage(ChatId.fromId(chatId), response)
            }
            catch (e: Throwable) {
                log.error("Во время переключения дежурного произошла ошибка", e)
                BOT.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = "Ошибочка вышла при переключении дежурного. Вот, наслаждайтесь: $e:${e.stackTraceToString()}"
                )
            }
        }
    }

}
