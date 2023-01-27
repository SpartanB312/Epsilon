package club.eridani.epsilon.client.config

import club.eridani.epsilon.client.Epsilon
import club.eridani.epsilon.client.common.extensions.isNotExist
import club.eridani.epsilon.client.gui.SpartanGUI

class GuiConfig(
    val gui: SpartanGUI
) : Config("${gui.name}.json") {
    override val dirPath = Epsilon.DEFAULT_CONFIG_PATH + "config/gui/"

    override fun saveConfig() {
        if (configFile.isNotExist()) {
            configFile.parentFile.mkdirs()
            configFile.createNewFile()
        }
    }

    override fun loadConfig() {
        if (configFile.exists()) {
        } else saveConfig()
    }

}