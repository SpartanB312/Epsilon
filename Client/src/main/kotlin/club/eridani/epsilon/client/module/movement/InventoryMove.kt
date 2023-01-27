package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.decentralized.decentralizedListener
import club.eridani.epsilon.client.event.decentralized.events.player.OnUpdateWalkingPlayerDecentralizedEvent
import club.eridani.epsilon.client.event.events.PlayerInputEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.module.Module
import net.minecraft.client.gui.GuiChat
import org.lwjgl.input.Keyboard

object InventoryMove :
    Module(name = "InventoryMove", category = Category.Movement, description = "Moving when you opening gui") {

    val sneak by setting("Sneak", false)
    private val chat by setting("Chat", false)
    private val pitchSpeed by setting("PitchSpeed", 10, 0..20, 1)
    private val yawSpeed by setting("YawSpeed", 10, 0..20, 1)

    init {
        decentralizedListener(OnUpdateWalkingPlayerDecentralizedEvent.Pre) {
            if (mc.currentScreen != null && mc.currentScreen !is GuiChat || mc.currentScreen is GuiChat && chat) {
                if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                    for (i in 0 until pitchSpeed) {
                        mc.player.rotationPitch--
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                    for (i in 0 until pitchSpeed) {
                        mc.player.rotationPitch++
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                    for (i in 0 until yawSpeed) {
                        mc.player.rotationYaw++
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                    for (i in 0 until yawSpeed) {
                        mc.player.rotationYaw--
                    }
                }
                if (Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.keyCode)) {
                    mc.player.isSprinting = true
                }
                if (mc.player.rotationPitch > 90) mc.player.rotationPitch = 90f
                if (mc.player.rotationPitch < -90) mc.player.rotationPitch = -90f
            }
        }

        listener<PlayerInputEvent> { event ->
            if (mc.currentScreen != null && mc.currentScreen !is GuiChat || mc.currentScreen is GuiChat && chat) {
                if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.keyCode)) {
                    event.movementInput.moveForward = getSpeed()
                }
                if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.keyCode)) {
                    event.movementInput.moveForward = -getSpeed()
                }
                if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.keyCode)) {
                    event.movementInput.moveStrafe = getSpeed()
                }
                if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.keyCode)) {
                    event.movementInput.moveStrafe = -getSpeed()
                }
                if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.keyCode)) {
                    event.movementInput.jump = true
                }
                if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.keyCode) && sneak) {
                    event.movementInput.sneak = true
                }
            }
        }
    }


    private fun getSpeed(): Float {
        var x = 1f
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.keyCode) && sneak) {
            x /= 0.3f
        }
        return x
    }
}