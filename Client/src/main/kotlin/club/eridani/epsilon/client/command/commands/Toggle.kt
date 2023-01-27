package club.eridani.epsilon.client.command.commands

import club.eridani.epsilon.client.command.Command
import club.eridani.epsilon.client.command.execute
import club.eridani.epsilon.client.management.ModuleManager
import club.eridani.epsilon.client.util.text.ChatUtil

object Toggle : Command(
    name = "Toggle",
    prefix = "toggle",
    description = "Toggle module",
    syntax = "toggle <module>",
    block = {
        execute { name ->
            ModuleManager.modules.forEach {
                if (it.name.equals(name, ignoreCase = true)) {
                    it.toggle()
                    ChatUtil.printChatMessage("Toggled ${name}!")
                }
            }
        }
    }
)