package club.eridani.epsilon.client.command.commands

import club.eridani.epsilon.client.command.Command
import club.eridani.epsilon.client.command.execute
import club.eridani.epsilon.client.config.ConfigManager
import club.eridani.epsilon.client.util.text.ChatUtil

object Config : Command(
    name = "Config",
    prefix = "config",
    description = "Used for save or load config",
    syntax = "config <save/load>",
    block = {
        execute {
            if (it.equals("save", true)) {
                ConfigManager.saveAll()
                ChatUtil.printChatMessage("Saved all Spartan configs")
            } else if (it.equals("load", true)) {
                ConfigManager.loadAll()
                ChatUtil.printChatMessage("Loaded all Spartan configs")
            }
        }
    }
)

