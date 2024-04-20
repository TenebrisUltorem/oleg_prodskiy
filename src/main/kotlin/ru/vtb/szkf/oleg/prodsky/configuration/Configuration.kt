package ru.vtb.szkf.oleg.prodsky.configuration

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import kotlinx.serialization.Serializable
import ru.vtb.szkf.oleg.prodsky.extensions.logger
import java.io.FileInputStream

data object Configuration {
    @Serializable
    private data class ConfigurationData(
        val token: String,
        val botNames: List<String>,
        val switchAttendantCron: String
    )

    private val data = Yaml.default.decodeFromStream<ConfigurationData>(FileInputStream("config.yml"))

    val token = data.token
    val botNames = data.botNames
    val switchAttendantCron = data.switchAttendantCron

    @JvmStatic
    private val log = logger()

    init {
        log.info("Loaded {}", data)
    }

}
