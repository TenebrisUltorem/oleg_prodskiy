package ru.vtb.szkf.oleg.prodsky

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import ru.vtb.szkf.oleg.prodsky.configuration.Configuration
import ru.vtb.szkf.oleg.prodsky.domain.AttendantTable
import ru.vtb.szkf.oleg.prodsky.handler.AttendantCommandHandler
import ru.vtb.szkf.oleg.prodsky.handler.HelpCommandHandler
import ru.vtb.szkf.oleg.prodsky.handler.MessageHandler
import ru.vtb.szkf.oleg.prodsky.handler.StartCommandHandler
import java.io.File
import java.net.URL
import kotlin.system.exitProcess

private object Main
private val log = LoggerFactory.getLogger(Main::class.java)

fun main() {
    ///Load default config
    val config = File("config.yml")

    if (!config.exists()) {
        config.createNewFile()
        config.writeText(getResource("defaultConfig.yml").readText())

        log.info("Отсутствует файл конфигурации config.yml. Заполните файл и перезапустите приложение")
        exitProcess(0)
    }
    // Configure database
    Database.connect(
        url = "jdbc:sqlite:./data.db",
        driver = "org.sqlite.JDBC",
        databaseConfig = DatabaseConfig.invoke {
            maxEntitiesToStoreInCachePerEntity = 0
        }
    )
    transaction { SchemaUtils.create(AttendantTable) }

    // Launch bot
    val bot = bot {
        token = Configuration.token
        dispatch {
            command("start", StartCommandHandler.handleStartCommand)
            command("help", HelpCommandHandler.handleHelpCommand)

            command("attendantList", AttendantCommandHandler.handleGetAttendantListCommand)
            command("getAttendant", AttendantCommandHandler.handleGetAttendantCommand)
            command("addAttendant", AttendantCommandHandler.handleAddAttendantCommand)
            command("deleteAttendant", AttendantCommandHandler.handleDeleteAttendantCommand)

            text(handleText = MessageHandler.handleText)
        }
    }
    bot.startPolling()

    log.info("Oleg has started!")
}

fun getResource(name: String): URL = checkNotNull(Main::class.java.classLoader.getResource(name)) {
    "No resource found by name = '$name'"
}
