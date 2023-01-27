package club.eridani.epsilon.client.module.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.curBlockDamageMP
import club.eridani.epsilon.client.event.decentralized.decentralizedListener
import club.eridani.epsilon.client.event.decentralized.events.client.ClientTickDecentralizedEvent
import club.eridani.epsilon.client.event.decentralized.events.client.Render2DDecentralizedEvent
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.Utils
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.ItemBlock
import net.minecraft.util.EnumHand
import kotlin.random.Random

object AutoClicker : Module(
    name = "AutoClicker",
    category = Category.Combat,
    description = "Auto clicking module"
) {

    private var leftLastSwing = 0L
    private var rightLastSwing = 0L
    private var leftDelay: Long
    private var rightDelay: Long

    private val minCPSValue by setting("MinCPS", 8, 1..40, 1)
    private val maxCPSValue by setting("MaxCPS", 12, 1..40, 1)
    private val rightValue by setting("RightClick", true)
    private val leftValue by setting("LeftClick", true)
    private val blockClick by setting("BlockClick", true) { leftValue }
    private val blockOnly by setting("BlockOnly", true) { rightValue }
    private val jitterValue by setting("JitterClick", false)

    init {
        leftDelay = randomClickDelay(minCPSValue, maxCPSValue)
        rightDelay = randomClickDelay(minCPSValue, maxCPSValue)

        decentralizedListener(Render2DDecentralizedEvent) {
            if (jitterValue && ((leftValue && mc.gameSettings.keyBindAttack.isKeyDown
                        && mc.playerController.curBlockDamageMP == 0f) ||
                        (rightValue && mc.gameSettings.keyBindUseItem.isKeyDown && !mc.player.isHandActive
                                && (!blockOnly || mc.player.heldItemMainhand.item is ItemBlock)))
            ) {
                if (Random.nextBoolean()) mc.player.rotationYaw += (if (Random.nextBoolean()) -Random.nextDouble(
                    0.0,
                    1.0
                ) else Random.nextDouble(
                    0.0,
                    1.0
                )).toFloat()
                if (Random.nextBoolean()) {
                    mc.player.rotationPitch += (if (Random.nextBoolean()) -Random.nextDouble(
                        0.0,
                        1.0
                    ) else Random.nextDouble(
                        0.0,
                        1.0
                    )).toFloat()
                    // Make sure pitch is not going into unlegit values
                    if (mc.player.rotationPitch > 90) mc.player.rotationPitch =
                        90f else if (mc.player.rotationPitch < -90) mc.player.rotationPitch = -90f
                }
            }
        }

        decentralizedListener(ClientTickDecentralizedEvent) {
            if (Utils.nullCheck()) return@decentralizedListener
            repeat(2) {
                if (mc.gameSettings.keyBindAttack.isKeyDown && leftValue && System.currentTimeMillis() - leftLastSwing >= leftDelay) {
                    if (blockClick) {
                        mc.player.swingArm(EnumHand.MAIN_HAND)
                        if (mc.objectMouseOver.entityHit != null) {
                            mc.playerController.attackEntity(mc.player, mc.objectMouseOver.entityHit)
                        }
                    } else {
                        KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode)
                    }
//                CPS.INSTANCE.addClick()
                    leftLastSwing = System.currentTimeMillis()
                    leftDelay = randomClickDelay(minCPSValue, maxCPSValue)
                }

                if (blockOnly && mc.player.heldItemMainhand.item is ItemBlock && mc.gameSettings.keyBindUseItem.isKeyDown) {
                    return@decentralizedListener
                }
                // Right click
                if (mc.gameSettings.keyBindUseItem.isKeyDown
                    && !mc.player.isHandActive
                    && rightValue
                    && System.currentTimeMillis() - rightLastSwing >= rightDelay
                ) {
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode)
                    rightLastSwing = System.currentTimeMillis()
                    rightDelay = randomClickDelay(minCPSValue, maxCPSValue)
                }
            }
        }
    }


    private fun randomClickDelay(minCPS: Int, maxCPS: Int): Long {
        return (Math.random() * (1000 / minCPS - 1000 / maxCPS + 1) + 1000 / maxCPS).toLong()
    }
}