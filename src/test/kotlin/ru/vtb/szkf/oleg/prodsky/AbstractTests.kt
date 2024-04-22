package ru.vtb.szkf.oleg.prodsky

import io.mockk.mockkObject
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import ru.vtb.szkf.oleg.prodsky.domain.AttendantJobTable
import ru.vtb.szkf.oleg.prodsky.domain.AttendantTable
import ru.vtb.szkf.oleg.prodsky.integration.ProductionCalendarWebClient
import java.io.File
import kotlin.random.Random
import kotlin.test.BeforeTest

abstract class AbstractTests {

    protected val testChatId = Random.nextLong()

    init {
        Database.connect("jdbc:sqlite:./test_data.db", "org.sqlite.JDBC")
        transaction { SchemaUtils.create(AttendantTable, AttendantJobTable) }

        mockkObject(ProductionCalendarWebClient)
    }

    @BeforeTest
    fun beforeTest() {
        transaction {
            AttendantTable.deleteAll()
            AttendantJobTable.deleteAll()
        }
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun shutdown() {
            File("./test_data.db").delete()
        }
    }

}
