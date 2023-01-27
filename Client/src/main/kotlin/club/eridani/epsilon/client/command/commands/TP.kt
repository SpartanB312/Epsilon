package club.eridani.epsilon.client.command.commands

import club.eridani.epsilon.client.command.Command
import club.eridani.epsilon.client.command.execute
import club.eridani.epsilon.client.util.Wrapper
import club.eridani.epsilon.client.util.text.ChatUtil

object TP : Command(
    name = "TP",
    prefix = "tp",
    description = "Teleport you to the coords",
    syntax = "tp <x> <y> <z>",
    block = {
        try {
            execute { x ->
                execute { y ->
                    execute { z ->
                        Wrapper.mc.player?.setPosition(x.toDouble(), y.toDouble(), z.toDouble())
                        ChatUtil.printChatMessage("Teleported you to $x $y $z")
                    }
                }
            }
        } catch (ignore: Exception) {
            ChatUtil.printChatMessage("Usage : ${TP.syntax}")
        }
    }
)