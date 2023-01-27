package club.eridani.epsilon.client.module.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.decentralized.decentralizedListener
import club.eridani.epsilon.client.event.decentralized.events.player.OnUpdateWalkingPlayerDecentralizedEvent
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.module.misc.AntiBot
import club.eridani.epsilon.client.module.movement.Scaffold
import club.eridani.epsilon.client.module.movement.Speed
import club.eridani.epsilon.client.util.RotationUtil
import club.eridani.epsilon.client.util.onPacketSend
import net.minecraft.network.login.client.CPacketLoginStart


object TargetStrafe :
    Module(name = "TargetStrafe", category = Category.Combat, description = "Strafes around your current target") {
    private val holdSpace by setting("Hold Space", true)
    val range by setting("Range", 3.0, 0.1..15.0, .1)
    var direction = -1.0f
    var forward = 0f
    var yaw = 0f

    init {
        decentralizedListener(OnUpdateWalkingPlayerDecentralizedEvent.Pre) {
            if (canStrafe()) {
                if (direction.toInt() % 1 != 0 || direction == 0f) {
                    direction = 1f
                }
                val rotation = RotationUtil.getRotationsGucel(KillAura.currentTarget)[0]

                if (mc.player.collidedHorizontally) {
                    if (!Scaffold.isEnabled) {
                        invertStrafe()
                    }
                }

                yaw = rotation

                forward = if (mc.player.getDistance(KillAura.currentTarget!!) <= range) {
                    0f
                } else {
                    1f
                }
            } else {
                forward = mc.player.movementInput.moveForward
                yaw = mc.player.rotationYaw
                direction = mc.player.movementInput.moveStrafe
            }
        }

        onPacketSend {
            it.packet is CPacketLoginStart
        }
    }


    private fun canStrafe(): Boolean {
        return KillAura.currentTarget != null && !AntiBot.isBot(KillAura.currentTarget!!) && Speed.isEnabled && KillAura.isEnabled &&
                if (holdSpace) {
                    mc.gameSettings.keyBindJump.isKeyDown
                } else {
                    true
                }

    }

    private fun invertStrafe() {
        direction = -direction
    }
}
