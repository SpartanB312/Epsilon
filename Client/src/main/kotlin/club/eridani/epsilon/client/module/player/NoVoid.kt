package club.eridani.epsilon.client.module.player

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.isBlockUnder
import club.eridani.epsilon.client.event.decentralized.decentralizedListener
import club.eridani.epsilon.client.event.decentralized.events.player.OnUpdateWalkingPlayerDecentralizedEvent
import club.eridani.epsilon.client.event.events.PlayerMoveEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.Timer
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.BlockPos

object NoVoid: Module(
    name = "NoVoid",
    category = Category.Player,
    description = "Anti player fall in to void"
) {

    val mode by setting("Mode", Mode.Normal)
    val posY by setting("Y Position", 100, 0 ..256, 1) { mode == Mode.Normal }
    private var latestPosition: BlockPos? = null
    private var fall = 0f
    private var shouldBack = false
    val timer = Timer()

    init {
        decentralizedListener(OnUpdateWalkingPlayerDecentralizedEvent.Pre) {
            if (mc.player.onGround) latestPosition = mc.player.position
        }

        listener<PlayerMoveEvent.Pre> {
            runSafe {
                if (mc.player == null) return@runSafe

                if (mode == Mode.Hypixel) {
                    if (!club.eridani.epsilon.client.util.EntityUtil.isOnGround(0.001)) {
                        if (mc.player.motionY < -0.08) {
                            fall = (fall.toDouble() - mc.player.motionY).toFloat()
                        }
                    } else {
                        fall = 0.0f
                    }
                    if (shouldBack && timer.passed(150) || mc.player.collidedVertically) {
                        shouldBack = false
                        timer.reset()
                    }
                    if (fall > 8.0f && !mc.player.isBlockUnder) {
                        if (!shouldBack) {
                            shouldBack = true
                            timer.reset()
                        }
                        fall = 0.0f
                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 12.0, mc.player.posZ, false))
                        mc.player.connection.sendPacket(CPacketPlayer.Position(latestPosition!!.x.toDouble(), latestPosition!!.y.toDouble() - 0.1, latestPosition!!.z.toDouble(), true))
                        if (mc.isSingleplayer) {
                            mc.player.setPosition(latestPosition!!.x.toDouble(), latestPosition!!.y.toDouble(), latestPosition!!.z.toDouble())
                        }
                    }
                } else {
                    var isVoid = true
                    for (i in mc.player.posY.toInt() downTo -1 + 1) {
                        if (mc.world.getBlockState(BlockPos(mc.player.posX, i.toDouble(), mc.player.posZ)).block !== Blocks.AIR) {
                            isVoid = false
                            break
                        }
                    }
                    if (mc.player.posY < posY && isVoid) {
                        mc.player.motionY = 0.0
                    }
                }
            }
        }
    }

    override fun getHudInfo(): String {
        return mode.name
    }

    enum class Mode {
        Hypixel,
        Normal
    }
}