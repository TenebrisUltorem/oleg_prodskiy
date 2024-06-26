package ru.vtb.szkf.oleg.prodsky

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import ru.vtb.szkf.oleg.prodsky.configuration.Configuration
import ru.vtb.szkf.oleg.prodsky.domain.AttendantJobTable
import ru.vtb.szkf.oleg.prodsky.domain.AttendantTable
import ru.vtb.szkf.oleg.prodsky.handler.AttendantCommandHandler
import ru.vtb.szkf.oleg.prodsky.handler.HelpCommandHandler
import ru.vtb.szkf.oleg.prodsky.handler.MessageHandler
import ru.vtb.szkf.oleg.prodsky.handler.AttendantJobCommandHandler
import ru.vtb.szkf.oleg.prodsky.service.AttendantJobService
import java.net.URL

private object Main
private val log = LoggerFactory.getLogger(Main::class.java)

val BOT = bot {
    token = Configuration.token
    dispatch {
        command("start", AttendantJobCommandHandler.handleStartCommand)
        command("stop", AttendantJobCommandHandler.handleStopCommand)
        command("help", HelpCommandHandler.handleHelpCommand)

        command("attendantList", AttendantCommandHandler.handleGetAttendantListCommand)
        command("getAttendant", AttendantCommandHandler.handleGetAttendantCommand)
        command("addAttendant", AttendantCommandHandler.handleAddAttendantCommand)
        command("deleteAttendant", AttendantCommandHandler.handleDeleteAttendantCommand)

        text(handleText = MessageHandler.handleText)
    }
}

fun main() {
    // Configure database
    Database.connect(
        url = "jdbc:sqlite:./data.db",
        driver = "org.sqlite.JDBC",
    )
    transaction { SchemaUtils.create(AttendantTable, AttendantJobTable) }

    //Initialize jobs service
    AttendantJobService

    // Launch bot
    BOT.startPolling()

    log.info("Oleg has started!")
}

fun getResource(name: String): URL = checkNotNull(Main::class.java.classLoader.getResource(name)) {
    "No resource found by name = '$name'"
}
