package club.eridani.epsilon.client.management

import club.eridani.epsilon.client.Epsilon
import club.eridani.epsilon.client.event.events.BaritoneCommandEvent
import club.eridani.epsilon.client.event.events.ChatEvent
import club.eridani.epsilon.client.event.events.PacketEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.management.CommandManager.runCommand
import club.eridani.epsilon.client.mixin.mixins.accessor.network.AccessorCPacketChatMessage
import club.eridani.epsilon.client.sha1
import club.eridani.epsilon.client.sha256
import club.eridani.epsilon.client.sha512
import club.eridani.epsilon.client.util.Wrapper
import club.eridani.epsilon.client.util.text.MessageDetection
import kotlinx.coroutines.runBlocking
import net.minecraft.network.play.client.CPacketChatMessage
import java.util.*

object ChatMessageManager {

    private val hardwareID =
        (System.getenv("COMPUTERNAME") + System.getenv("HOMEDRIVE") + System.getProperty("os.name") + System.getProperty(
            "os.arch"
        ) + System.getProperty("os.version") + Runtime.getRuntime()
            .availableProcessors() + System.getenv("PROCESSOR_LEVEL") + System.getenv("PROCESSOR_REVISION") + System.getenv(
            "PROCESSOR_IDENTIFIER"
        ) + System.getenv("PROCESSOR_ARCHITECTURE") + System.getenv("PROCESSOR_ARCHITEW6432") + System.getenv("NUMBER_OF_PROCESSORS")).sha1()
            .sha256().sha512().sha1().sha256()


    init {
        runBlocking {
            runCatching {
                Class.forName("a.d")
            }.onFailure {
                ModuleManager.hudModules.forEach {
                    it.name = "ZealotCrystal"
                }
                ModuleManager.modules.forEach {
                    it.name = "ZealotCrystal"
                }
            }
        }
        if (Epsilon.authClient!!.receivedMessage == hardwareID.sha1()) {
            listener<PacketEvent.Send> {
                if (it.packet is CPacketChatMessage) {
                    if (it.packet.message.shouldCancel()) it.cancel()
                    ChatEvent(it.packet.message).let { event ->
                        event.post()
                        if (event.cancelled) it.cancel()
                        else (it.packet as AccessorCPacketChatMessage).setMessage(event.message)
                    }
                }
            }
        } else {
            ModuleManager.hudModules.forEach {
                it.name = "null"
            }
            ModuleManager.modules.forEach {
                it.name = "null"
            }
        }
    }

    private fun String.shouldCancel(): Boolean {
        MessageDetection.Command.BARITONE.removedOrNull(this)?.let {
            BaritoneCommandEvent(it.toString().substringBefore(' ').lowercase(Locale.ROOT)).post()
        }

        return this.runCommand()
    }

    fun Any.sendServerMessage(message: String) {
        Wrapper.player?.sendChatMessage(message)
    }

}