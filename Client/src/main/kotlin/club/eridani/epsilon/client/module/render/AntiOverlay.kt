package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.decentralized.decentralizedListener
import club.eridani.epsilon.client.event.decentralized.events.client.ClientTickDecentralizedEvent
import club.eridani.epsilon.client.event.decentralized.events.network.PacketDecentralizedEvent
import club.eridani.epsilon.client.event.decentralized.events.render.RenderOverlayDecentralizedEvent
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.Utils
import net.minecraft.init.MobEffects
import net.minecraft.network.play.server.SPacketExplosion
import net.minecraft.network.play.server.SPacketSpawnExperienceOrb
import net.minecraft.network.play.server.SPacketSpawnMob
import net.minecraft.network.play.server.SPacketSpawnPainting

object AntiOverlay : Module(
    name = "AntiOverlay",
    alias = arrayOf("NoRender", "Overlay"),
    category = Category.Render,
    description = "Clear overlay"
) {

    private val fire by setting("Fire", true)
    private val blocks by setting("Blocks", true)
    private val water by setting("Water", true)
    private val blindness by setting("Blindness", true)
    private val nausea by setting("Nausea", false)
    private val explosion by setting("Explosion", true)
    private val exp by setting("Exp", false)
    private val paint by setting("Paint", false)
    private val mob by setting("Mob", false)
    val armor by setting("Armor", false)
    val hurtCam by setting("HurtCam", false)


    init {
        decentralizedListener(PacketDecentralizedEvent.Receive) {
            val packet = it.packet
            if (packet is SPacketSpawnExperienceOrb && exp
                || packet is SPacketExplosion && explosion
                || packet is SPacketSpawnPainting && paint
                || packet is SPacketSpawnMob && mob
            ) it.cancel()
        }
        decentralizedListener(ClientTickDecentralizedEvent) {
            if (Utils.nullCheck()) return@decentralizedListener
            if (blindness) {
                mc.player.removeActivePotionEffect(MobEffects.BLINDNESS)
            }

            if (nausea) {
                mc.player.removeActivePotionEffect(MobEffects.NAUSEA)
            }
        }

        decentralizedListener(RenderOverlayDecentralizedEvent) {
            if (fire && it.type == RenderOverlayDecentralizedEvent.OverlayType.FIRE) {
                it.cancel()
            } else if (blocks && it.type == RenderOverlayDecentralizedEvent.OverlayType.BLOCK) {
                it.cancel()
            } else if (water && it.type == RenderOverlayDecentralizedEvent.OverlayType.WATER) {
                it.cancel()
            }
        }
    }

}