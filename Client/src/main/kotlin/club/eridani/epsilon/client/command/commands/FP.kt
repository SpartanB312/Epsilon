package club.eridani.epsilon.client.command.commands

import club.eridani.epsilon.client.command.Command
import club.eridani.epsilon.client.module.misc.FakePlayer
import club.eridani.epsilon.client.util.text.ChatUtil

object FP : Command(
    name = "FakePlayer",
    prefix = "fp",
    description = "Toggle fake player",
    syntax = "fp",
    block = {
        FakePlayer.toggle()
        ChatUtil.printChatMessage("Toggled fake player!")
    }
)