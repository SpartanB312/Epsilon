package club.eridani.epsilon.client.util

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.network.NetHandlerPlayClient

@Suppress("NOTHING_TO_INLINE")
object Wrapper {
    @JvmStatic
    inline val mc: Minecraft
        get() = Minecraft.getMinecraft()

    @JvmStatic
    inline val world: WorldClient?
        get() = mc.world

    @JvmStatic
    inline val player: EntityPlayerSP?
        get() = mc.player

    @JvmStatic
    inline val playerController: PlayerControllerMP?
        get() = mc.playerController

    @JvmStatic
    inline val connection: NetHandlerPlayClient?
        get() = mc.connection
}