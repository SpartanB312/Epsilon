package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.Utils
import club.eridani.epsilon.client.util.onTick
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.network.play.client.CPacketPlayer

object Step : Module(
    name = "Step",
    category = Category.Movement,
    description = "Allows you step blocks like ladder"
) {
    private val oneBlockPositions = doubleArrayOf(0.42, 0.753)
    private val oneAndHalfBlockPositions = doubleArrayOf(0.41, 0.75, 1.00, 1.16, 1.24, 1.17)
    private val twoBlocksPositions = doubleArrayOf(0.4, 0.75, 0.5, 0.41, 0.83, 1.16, 1.41, 1.57, 1.58, 1.42)

    private val mode by setting("Mode", Mode.NCP)
    private val height by setting("Height", 2f, 0f..2f, 0.1f)
    private val toggle by setting("Toggle", false)

    init {
        onTick {
            runSafe {
                when (mode) {
                    Mode.NCP -> {
                        var stepHeight = -1.0
                        val box = player.entityBoundingBox.offset(0.0, 0.05, 0.0).grow(0.05)
                        if (mc.world.getCollisionBoxes(player, box.offset(0.0, 2.0, 0.0)).isNotEmpty()) {
                            return@runSafe
                        }

                        for (bb in mc.world.getCollisionBoxes(player, box)) {
                            if (bb.maxY > stepHeight) {
                                stepHeight = bb.maxY
                            }
                        }

                        stepHeight -= player.posY

                        if (stepHeight < 0.0 || stepHeight > height) {
                           return@runSafe
                        }

                        player.stepHeight = 0.5f

                        if (!player.onGround || player.isOnLadder || player.isInWater || player.isInLava) {
                            return@runSafe
                        }
                        if (player.movementInput.moveForward == 0.0f && player.movementInput.moveStrafe == 0.0f) {
                            return@runSafe
                        }
                        if (player.movementInput.jump || !player.collidedHorizontally) {
                            return@runSafe
                        }

                        if (stepHeight == 2.0) {
                            for (position in twoBlocksPositions) {
                                mc.player.connection.sendPacket(
                                    CPacketPlayer.Position(
                                        mc.player.posX,
                                        mc.player.posY + position,
                                        mc.player.posZ,
                                        player.onGround
                                    )
                                )
                            }
                            player.setPosition(player.posX, player.posY + 2, player.posZ)
                            if (toggle) {
                                toggle()
                            }
                        } else if (stepHeight == 1.5) {
                            for (position in oneAndHalfBlockPositions) {
                                mc.player.connection.sendPacket(
                                    CPacketPlayer.Position(
                                        mc.player.posX,
                                        mc.player.posY + position,
                                        mc.player.posZ,
                                        player.onGround
                                    )
                                )
                            }
                            player.setPosition(player.posX, player.posY + 1.5, player.posZ)
                            if (toggle) {
                                toggle()
                            }
                        } else if (stepHeight == 1.0) {
                            for (position in oneBlockPositions) {
                                mc.player.connection.sendPacket(
                                    CPacketPlayer.Position(
                                        mc.player.posX,
                                        mc.player.posY + position,
                                        mc.player.posZ,
                                        player.onGround
                                    )
                                )
                            }
                            player.setPosition(player.posX, player.posY + 1, player.posZ)
                            if (toggle) {
                                toggle()
                            }
                        }
                    }
                    Mode.AAC -> {
                        if (mc.player.collidedHorizontally) {
                            if (mc.player.onGround) {
                                mc.player.motionY = 0.4322
                            } else {
                                mc.player.motionY += 0.0122
                            }
                            if (toggle) {
                                toggle()
                            }
                        }
                    }
                    Mode.Vanilla -> {
                        player.stepHeight = height
                    }
                }
            }
        }
    }

    override fun onDisable() {
        if (Utils.nullCheck()) return
        mc.player.stepHeight = 0.5f
    }

    override fun onEnable() {
        if (Utils.nullCheck()) return
        mc.player.stepHeight = 0.5f
    }


    override fun getHudInfo(): String {
        return mode.name
    }

    enum class Mode {
        NCP, AAC, Vanilla
    }
}