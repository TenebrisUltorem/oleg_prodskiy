package ru.vtb.szkf.oleg.prodsky.scheduling

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import dev.inmo.krontab.buildSchedule
import dev.inmo.krontab.doInfinity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.vtb.szkf.oleg.prodsky.configuration.Configuration
import ru.vtb.szkf.oleg.prodsky.extensions.logger
import ru.vtb.szkf.oleg.prodsky.service.AttendantService
import java.util.concurrent.ConcurrentHashMap


object Scheduler {
    private val kronScheduler = buildSchedule(Configuration.switchAttendantCron)
    private val cronJobIds = ConcurrentHashMap.newKeySet<Long>()

    @JvmStatic
    private val log = logger()

    fun launchAttendantJobForChat(chatId: Long, bot: Bot): Job? {
        if (cronJobIds.contains(chatId)) {
            log.warn("Джоба по смене дежурного в чатике '{}' уже запущена", chatId)
            return null
        }

        cronJobIds.add(chatId)
        return CoroutineScope(Dispatchers.Default).launch {
            kronScheduler.doInfinity {
                val response = AttendantService.switchAttendant(chatId)
                if (response != null) bot.sendMessage(ChatId.fromId(chatId), response)
            }
        }
    }
}
