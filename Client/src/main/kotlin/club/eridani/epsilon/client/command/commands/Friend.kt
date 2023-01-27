package club.eridani.epsilon.client.command.commands

import club.eridani.epsilon.client.command.Command
import club.eridani.epsilon.client.command.execute
import club.eridani.epsilon.client.management.AltManager
import club.eridani.epsilon.client.management.FriendManager
import club.eridani.epsilon.client.util.text.ChatUtil
import club.eridani.epsilon.client.util.text.ChatUtil.AQUA
import club.eridani.epsilon.client.util.text.ChatUtil.RESET

object Friend : Command(
    name = "Friend",
    prefix = "friend",
    description = "Add friend",
    syntax = "friend <name>",
    block = {
        execute { name ->
            Thread {
                val uuid = AltManager.getUUID(name)
                if (uuid.isEmpty()) {
                    ChatUtil.printChatMessage("Cannot find a player called $AQUA$name")
                } else {
                    if (FriendManager.addFriend(uuid)) {
                        ChatUtil.printChatMessage("$AQUA$name$RESET has been friended.")
                    } else {
                        ChatUtil.printChatMessage("$AQUA$name$RESET already is your friend.")
                    }
                }
                Thread.currentThread().interrupt()
            }.start()
        }
    }
)
