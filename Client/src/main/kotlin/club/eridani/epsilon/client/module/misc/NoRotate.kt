package club.eridani.epsilon.client.module.misc

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.rotationPitch
import club.eridani.epsilon.client.common.extensions.rotationYaw
import club.eridani.epsilon.client.common.extensions.runSafeTask
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.onPacketReceive
import net.minecraft.network.play.server.SPacketPlayerPosLook

object NoRotate : Module(
    name = "NoRotate",
    category = Category.Misc,
    description = "Prevents you from processing server rotations"
) {

    init {
        onPacketReceive {
            runSafeTask {
                if (it.packet is SPacketPlayerPosLook) {
                    it.packet.rotationYaw = mc.player.rotationYaw
                    it.packet.rotationPitch = mc.player.rotationPitch
                }
            }
        }
    }

}