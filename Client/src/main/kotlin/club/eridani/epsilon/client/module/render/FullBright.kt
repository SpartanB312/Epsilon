package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.decentralized.decentralizedListener
import club.eridani.epsilon.client.event.decentralized.events.client.ClientTickDecentralizedEvent
import club.eridani.epsilon.client.event.decentralized.events.network.PacketDecentralizedEvent
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.Utils
import net.minecraft.init.MobEffects
import net.minecraft.network.play.server.SPacketEntityEffect
import net.minecraft.potion.PotionEffect


object FullBright :
    Module(
        name = "FullBright",
        alias = arrayOf("Brightness,Bright"),
        category = Category.Render,
        description = "Brightens up your world"
    ) {

    private val mode by setting("Mode", Mode.Gamma)
    private val effects by setting("Effects", false)
    private var prevGamma = -1f

    init {
        decentralizedListener(ClientTickDecentralizedEvent) {
            if (Utils.nullCheck()) return@decentralizedListener
            if (mode == Mode.Gamma) {
                if (mc.gameSettings.gammaSetting <= 100f) mc.gameSettings.gammaSetting++
            } else if (mode == Mode.Potion) {
                mc.player.addPotionEffect(PotionEffect(MobEffects.NIGHT_VISION, 5210))
            }
        }
        decentralizedListener(PacketDecentralizedEvent.Receive) { event ->
            val packet = event.packet
            if (packet is SPacketEntityEffect && effects) {
                if (mc.player != null && packet.entityId == mc.player.entityId && (packet.effectId.toInt() == 9 || packet.effectId.toInt() == 15)) {
                    event.cancel()
                }
            }
        }
    }

    override fun onEnable() {
        prevGamma = mc.gameSettings.gammaSetting
    }

    override fun onDisable() {
        if (mc.player != null) mc.player.removePotionEffect(MobEffects.NIGHT_VISION)
        if (prevGamma == -1f)
            return
        mc.gameSettings.gammaSetting = prevGamma
        prevGamma = -1f
    }

    enum class Mode(val standardName: String) {
        Gamma("Gamma"),
        Potion("Potion")
    }
}