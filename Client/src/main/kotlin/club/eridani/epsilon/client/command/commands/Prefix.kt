package club.eridani.epsilon.client.command.commands

import club.eridani.epsilon.client.command.Command
import club.eridani.epsilon.client.command.execute
import club.eridani.epsilon.client.management.CommandManager
import club.eridani.epsilon.client.util.Wrapper
import club.eridani.epsilon.client.util.text.ChatUtil
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.init.SoundEvents

object Prefix : Command(
    name = "Prefix",
    prefix = "prefix",
    description = "Change command prefix",
    syntax = "prefix <char>",
    block = {
        execute { newPrefix ->
            if (newPrefix.length != 1) {
                ChatUtil.sendNoSpamErrorMessage("Please specify a new prefix!")
            } else {
                ChatUtil.sendNoSpamMessage("Prefix set to " + ChatUtil.SECTION_SIGN + "b" + newPrefix + ChatUtil.SECTION_SIGN + "r" + " !")
                CommandManager.prefix = newPrefix
                Wrapper.mc.soundHandler.playSound(
                    PositionedSoundRecord.getMasterRecord(
                        SoundEvents.BLOCK_ANVIL_USE,
                        1F
                    )
                )
            }
        }
    }
)