package club.eridani.epsilon.client.command.commands

import club.eridani.epsilon.client.Epsilon.VERSION
import club.eridani.epsilon.client.command.Command
import club.eridani.epsilon.client.command.execute
import club.eridani.epsilon.client.management.CommandManager
import club.eridani.epsilon.client.management.ModuleManager
import club.eridani.epsilon.client.module.client.RootGUI
import club.eridani.epsilon.client.util.text.ChatUtil
import club.eridani.epsilon.client.util.text.ChatUtil.BLUE
import club.eridani.epsilon.client.util.text.ChatUtil.DARK_AQUA
import club.eridani.epsilon.client.util.text.ChatUtil.GOLD
import club.eridani.epsilon.client.util.text.ChatUtil.GRAY
import club.eridani.epsilon.client.util.text.ChatUtil.YELLOW

object Help : Command(
    name = "Help",
    prefix = "help",
    description = "Help command",
    syntax = "help",
    block = {
        var returned = false

        execute { moduleName ->
            ModuleManager.modules.find { it.name.equals(moduleName, true) }?.let {
                ChatUtil.printChatMessage("[${it.name}]${it.description}")
                returned = true
            }
        }

        if (!returned) {
            ChatUtil.printChatMessage("${BLUE}Epsilon ${DARK_AQUA}V$VERSION")
            ChatUtil.printChatMessage("${BLUE}Author ${DARK_AQUA}B312 KillRED")
            ChatUtil.printChatMessage("${BLUE}ClickGUI ${DARK_AQUA}${RootGUI.keyBind.displayValue}")
            ChatUtil.printChatMessage("${BLUE}CommandPrefix ${DARK_AQUA}${CommandManager.prefix}")
            ChatUtil.printChatMessage("${BLUE}Available Commands : ")
            CommandManager.commands.forEach {
                ChatUtil.printChatMessage("${GOLD}${it.name} ${YELLOW}${it.syntax} ${GRAY}[${it.description}]")
            }
        }
    }
)