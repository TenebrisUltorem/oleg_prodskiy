package ru.vtb.szkf.oleg.prodsky.integration

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import ru.vtb.szkf.oleg.prodsky.extensions.logger
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicReference

object ProductionCalendarWebClient {
    private val client = HttpClient()
    private const val URL = "https://isdayoff.ru/api/getdata"

    private const val WORKING_DAY_CODE = "0"

    private val log = logger()

    //First - expiration date, second - is today a vacation
    private val isTodayAVacationCache = AtomicReference<Pair<LocalDate, Boolean>>()

    fun isTodayAVacation(): Boolean = runBlocking {
        val today = LocalDate.now()

        if (isTodayAVacationCache.get()?.first?.isAfter(today) == true) {
            return@runBlocking isTodayAVacationCache.get().second
        }

        val response = client.get(URL) {
            url {
                parameters.append("year", today.year.toString())
                parameters.append("month", today.month.value.toString())
                parameters.append("day", today.dayOfMonth.toString())
            }
        }

        check(response.status == HttpStatusCode.OK) {
            "От сервиса производственного календаря получен неуспешный ответ " +
                "'${response.bodyAsText()}' со статусом '${response.status}'"
        }

        log.info("От сервиса производственного календаря получен ответ {}:{}", response.status, response.bodyAsText())

        val result = response.bodyAsText() != WORKING_DAY_CODE

        isTodayAVacationCache.set(today.plusDays(1) to result)
        return@runBlocking result
    }

}
