package club.eridani.epsilon.client.config

import club.eridani.epsilon.client.Epsilon
import club.eridani.epsilon.client.common.extensions.isNotExist
import club.eridani.epsilon.client.management.FriendManager

class FriendConfig : Config("Friend.json") {
    override val dirPath = Epsilon.DEFAULT_CONFIG_PATH + "config/"

    override fun saveConfig() {
        if (configFile.isNotExist()) {
            configFile.parentFile.mkdirs()
            configFile.createNewFile()
        } else {
            val friendList = FriendManager.friendList
            var text = ""
            for (friend in friendList) {
                text += friend + "\n"
            }
            configFile.writeText(text)
        }
    }

    override fun loadConfig() {
        if (configFile.exists()) {
            FriendManager.friendList.addAll(configFile.readLines())
        }
    }
}