package club.eridani.epsilon.client.command.commands

import club.eridani.epsilon.client.command.Command
import club.eridani.epsilon.client.command.execute
import club.eridani.epsilon.client.management.AltManager
import club.eridani.epsilon.client.management.FriendManager
import club.eridani.epsilon.client.util.text.ChatUtil
import club.eridani.epsilon.client.util.text.ChatUtil.AQUA
import club.eridani.epsilon.client.util.text.ChatUtil.RESET

object UnFriend : Command(
    name = "UnFriend",
    prefix = "unfriend",
    description = "Delete friend",
    syntax = "unfriend <name>",
    block = {
        execute { name ->
            Thread {
                val uuid = AltManager.getUUID(name)
                if (uuid.isEmpty()) {
                    ChatUtil.printChatMessage("Cannot find a player called $AQUA$name")
                } else {
                    if (FriendManager.removeFriend(uuid)) {
                        ChatUtil.printChatMessage("$AQUA$name$RESET has been unfriended.")
                    } else {
                        ChatUtil.printChatMessage("$AQUA$name$RESET is not your friend.")
                    }
                }
                Thread.currentThread().interrupt()
            }.start()
        }
    }
)
