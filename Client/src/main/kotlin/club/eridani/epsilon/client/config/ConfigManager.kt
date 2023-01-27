package club.eridani.epsilon.client.config

import club.eridani.epsilon.client.common.extensions.runSafeTask
import club.eridani.epsilon.client.management.AltManager
import club.eridani.epsilon.client.management.SpartanCore
import club.eridani.epsilon.client.util.Logger
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Suppress("NOTHING_TO_INLINE")
object ConfigManager {

    val gsonPretty: Gson = GsonBuilder().setPrettyPrinting().create()
    val jsonParser = JsonParser()

    val moduleConfigs = mutableListOf<ModuleConfig>()
    val guiConfigs = mutableListOf<GuiConfig>()
    val otherConfigs = mutableListOf<Config>()
    val friendConfig = FriendConfig()

    inline fun register(config: Config) {
        when (config) {
            is ModuleConfig -> moduleConfigs.add(config)
            is GuiConfig -> guiConfigs.add(config)
            else -> otherConfigs.add(config)
        }
    }

    inline fun loadAll(parallel: Boolean = false) {
        Logger.info("Loading all Spartan configs")
        loadModule(parallel = parallel)
        loadGUI(parallel = parallel)
        loadConfigs(parallel = parallel)
        friendConfig.loadConfig()
        AltManager.loadAccount()
        //Update settings later
        SpartanCore.updateSettings()
    }

    inline fun saveAll(parallel: Boolean = false) {
        Logger.info("Saving all Spartan configs")
        friendConfig.saveConfig()
        saveModule(parallel = parallel)
        saveGUI(parallel = parallel)
        saveConfigs(parallel = parallel)
        AltManager.saveAccount()
    }


    inline fun loadModule(parallel: Boolean) {
        loadConfigs(moduleConfigs, parallel)
    }

    inline fun saveModule(parallel: Boolean) {
        saveConfigs(moduleConfigs, parallel)
    }

    inline fun loadGUI(parallel: Boolean) {
        loadConfigs(guiConfigs, parallel)
    }

    inline fun saveGUI(parallel: Boolean) {
        saveConfigs(guiConfigs, parallel)
    }

    inline fun loadConfigs(configs: List<Config> = otherConfigs, parallel: Boolean = false) {
        runBlocking {
            configs.forEach {
                if (parallel) launch(Dispatchers.IO) {
                    if (!runSafeTask(false) { it.loadConfig() }) {
                        Logger.error("Can't load config : ${it.configName}")
                    }
                } else if (!runSafeTask(false) { it.loadConfig() }) {
                    Logger.error("Can't load config : ${it.configName}")
                }
            }
        }
    }

    inline fun saveConfigs(configs: List<Config> = otherConfigs, parallel: Boolean = false) {
        runBlocking {
            configs.forEach {
                if (parallel) launch(Dispatchers.IO) {
                    if (!runSafeTask(false) { it.saveConfig() }) {
                        Logger.error("Can't save config : ${it.configName}")
                    }
                } else if (!runSafeTask(false) { it.saveConfig() }) {
                    Logger.error("Can't save config : ${it.configName}")
                }
            }
        }
    }
}