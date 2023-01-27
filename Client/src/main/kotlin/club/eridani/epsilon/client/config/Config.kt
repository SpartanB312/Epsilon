package club.eridani.epsilon.client.config

import club.eridani.epsilon.client.Epsilon
import club.eridani.epsilon.client.setting.AbstractSetting
import java.io.File

@Suppress("NOTHING_TO_INLINE")
abstract class Config(
    val configName: String,
    val configs: MutableList<AbstractSetting<*>> = mutableListOf()
) {
    open val dirPath = Epsilon.DEFAULT_CONFIG_PATH + "config/"
    private inline val savePath get() = "$dirPath/$configName"

    private var file: File? = null

    abstract fun saveConfig()
    abstract fun loadConfig()

    protected val configFile
        get() = file ?: File(savePath).also {
            file = it
        }
}