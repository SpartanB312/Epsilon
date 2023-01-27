package club.eridani.epsilon.client.module.client

import club.minnced.discord.rpc.DiscordEventHandlers
import club.minnced.discord.rpc.DiscordEventHandlers.OnStatus
import club.minnced.discord.rpc.DiscordRPC
import club.minnced.discord.rpc.DiscordRichPresence
import club.eridani.epsilon.client.Epsilon
import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.Logger
import club.eridani.epsilon.client.util.onPacketReceive
import net.minecraft.network.play.server.SPacketChat
import java.util.*

object DiscordPresence : Module(name = "DiscordRPC",
        category = Category.Client,
        visibleOnArray = false,
        description = "Display the discord rpc when you playing minecraft with com.loader.epsilon.epsilon"
) {
    private var Name by setting("Name", false)
    private var IP by setting("IP", true)
    private var World by setting("World", true)

    private const val APP_ID = "906942184903835669"
    private var presence = DiscordRichPresence()
    private var rpc: DiscordRPC = DiscordRPC.INSTANCE
    private var details: String? = null
    private var state: String? = null
    private var lastChat: String? = null


    init {
        onPacketReceive { event ->
            if (event.packet is SPacketChat) {
                lastChat = event.packet.chatComponent.unformattedText
            }
        }
    }

    private fun getWorld(input: Int): String {
        if (input == -1) return "Nether" else if (input == 0) return "Overworld" else if (input == 1) return "End"
        return "Null"
    }

    private fun getWorld(): String {
        val world = mc.player.dimension
        if (lastChat != null) if (lastChat!!.contains("Position in queue: ")) return "queue at " + lastChat!!.substring(19).toInt()
        return getWorld(world)
    }

    private fun getDetails(): String {
        val ip = if (mc.isSingleplayer) "Single Player" else Objects.requireNonNull(mc.currentServerData)!!.serverIP
        val msg = ((if (IP) ip + (if (Name || World) " | " else "") else "")
                + (if (Name) mc.player.name.toString()
                + (if (World) " | " else "") else "")
                + if (World) "In " + getWorld() else "")
        return if (Name || IP || World) {
            msg
        } else "Noting is the Best!"
    }

    fun start() {
        Logger.info("Starting Discord RPC")
        val handlers = DiscordEventHandlers()
        handlers.disconnected = OnStatus { var1: Int, var2: String -> println("Discord RPC disconnected, var1: $var1, var2: $var2") }
        rpc.Discord_Initialize(APP_ID, handlers, true, "")
        presence.startTimestamp = System.currentTimeMillis() / 1000L
        presence.details = "Epsilon"
        presence.state = ""
        presence.largeImageKey = "logo"
        presence.largeImageText = Epsilon.VERSION
        rpc.Discord_UpdatePresence(presence)
        Thread({
            while (!Thread.currentThread().isInterrupted) {
                try {
                    rpc.Discord_RunCallbacks()
                    details = ""
                    state = ""
                    try {
                        when {
                            mc.isIntegratedServerRunning -> {
                                details = getDetails()
                            }
                            mc.currentServerData != null -> {
                                details = "Epsilon"
                                state = getDetails()
                            }
                            else -> {
                                details = "Main Menu"
                                state = "Epsilon"
                            }
                        }
                    } catch (ignored: NullPointerException) {
                    }
                    if (details != presence.details || state != presence.state) {
                        presence.startTimestamp = System.currentTimeMillis() / 1000L
                    }
                    presence.details = details
                    presence.state = state
                    rpc.Discord_UpdatePresence(presence)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
                try {
                    Thread.sleep(5000L)
                } catch (e3: InterruptedException) {
                    e3.printStackTrace()
                }
            }
        }, "Discord-RPC-Callback-Handler").start()
        Logger.info("Discord RPC initialised successfully")
    }

    override fun onEnable() {
        start()
    }

    override fun onDisable() {
        rpc.Discord_Shutdown()
    }
}