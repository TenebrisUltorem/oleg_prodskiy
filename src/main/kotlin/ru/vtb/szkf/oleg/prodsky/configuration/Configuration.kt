package ru.vtb.szkf.oleg.prodsky.configuration

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.parseToYamlNode
import com.charleskorn.kaml.yamlMap
import com.charleskorn.kaml.yamlScalar
import ru.vtb.szkf.oleg.prodsky.extensions.logger
import ru.vtb.szkf.oleg.prodsky.getResource
import java.io.File
import java.io.FileInputStream
import kotlin.system.exitProcess

data object Configuration {

    val token: String
    val botNames: List<String>
    val switchAttendantCron: String

    @JvmStatic
    private val log = logger()

    init {
        val config = File("config.yml")

        if (!config.exists()) {
            config.createNewFile()
            config.writeText(getResource("defaultConfig.yml").readText())

            log.info("Отсутствует файл конфигурации config.yml. Заполните файл и перезапустите приложение")
            exitProcess(0)
        }

        val data = Yaml.default.parseToYamlNode(FileInputStream("config.yml")).yamlMap

        token = requireNotNull(data.getScalar("token")?.content) {
            "В конфигурационном файле не заполнен параметр 'token'"
        }

        require(token.matches(Regex("\\d{8,10}:[a-zA-Z0-9_-]{35}"))) {
            "Некорректный формат токена"
        }

        botNames = requireNotNull(data.get<YamlList>("botNames")?.items?.map { it.yamlScalar.content }) {
            "В конфигурационном файле не заполнен параметр 'botNames'"
        }
        switchAttendantCron = requireNotNull(data.getScalar("switchAttendantCron")?.content) {
            "В конфигурационном файле не заполнен параметр 'switchAttendantCron'"
        }

        log.info("Loaded configuration: {}", mapOf(
            "token" to token,
            "botNames" to botNames,
            "switchAttendantCron" to switchAttendantCron
        ))
    }

}
