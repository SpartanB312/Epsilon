package club.eridani.epsilon.client.hud.info

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.toInt
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.Timer
import club.eridani.epsilon.client.util.graphics.AnimationUtil
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.graphics.render.asyncRender
import org.lwjgl.input.Mouse

object Keystroke :
    HUDModule(name = "Keystroke", category = Category.InfoHUD, description = "Display what keys you pressed") {

    val space by setting("Space", true).listen {
        updateSize()
    }
    val mouse by setting("Mouse", false).listen {
        updateSize()
    }

    private const val default1USize = 25.2f
    private const val default1USpace = 27.8f //27.8f

    val resize by setting("Resize", 1.0f, 0.1f..5.0f, 0.1f).listen {
        updateButtons()
        updateSize()
    }

    private val buttons = mutableListOf<Button>()

    init {
        updateButtons()
        updateSize()
    }

    private val animationTimer = Timer()

    private val asSyncUpdate = asyncRender {
        if (animationTimer.passed(16)) {
            animationTimer.reset()
            buttons.forEach {
                it.calAnimation()
            }
        }
    }

    private fun updateButtons() {
        buttons.clear()
        buttons.add(Button("W", default1USize * resize, default1USize * resize))
        buttons.add(Button("S", default1USize * resize, default1USize * resize))
        buttons.add(Button("A", default1USize * resize, default1USize * resize))
        buttons.add(Button("D", default1USize * resize, default1USize * resize))
        buttons.add(Button("Space", default1USize * 3.21f * resize, default1USize * resize))
        buttons.add(Button("LMB", default1USize * 1.55f * resize, default1USize * resize))
        buttons.add(Button("RMB", default1USize * 1.55f * resize, default1USize * resize))
    }

    private fun updateSize() {
        resize {
            val count = space.toInt() + mouse.toInt()
            width = (81 * resize).toInt()
            height = ((60 + (count * 25)) * resize).toInt()
        }
    }

    override fun onRender() {
        buttons[0].draw(x + default1USpace * resize, y.toFloat())
        buttons[2].draw(x.toFloat(), y + default1USpace * resize)
        buttons[1].draw(x + default1USpace * resize, y + default1USpace * resize)
        buttons[3].draw(x + (default1USpace * 2) * resize, y + default1USpace * resize)
        if (buttons.isNotEmpty()) {
            if (mc.gameSettings.keyBindForward.isKeyDown) buttons[0].onPress()
            if (mc.gameSettings.keyBindBack.isKeyDown) buttons[1].onPress()
            if (mc.gameSettings.keyBindLeft.isKeyDown) buttons[2].onPress()
            if (mc.gameSettings.keyBindRight.isKeyDown) buttons[3].onPress()
            if (mc.gameSettings.keyBindJump.isKeyDown) buttons[4].onPress()
            if (Mouse.isButtonDown(0)) buttons[5].onPress()
            if (Mouse.isButtonDown(1)) buttons[6].onPress()
        }
        var yPos = 2
        if (space) {
            buttons[4].draw(x.toFloat(), y + default1USpace * yPos * resize)
            yPos++
        }
        if (mouse) {
            buttons[5].draw(x.toFloat(), y + default1USpace * yPos * resize)
            buttons[6].draw(x.toFloat() + default1USpace * 1.5f * resize, y + default1USpace * yPos * resize)
        }
        asSyncUpdate.render()
    }


    class Button(val button: String, val width: Float, val height: Float) {
        private var isPressed = false
        private val pressedTimer = Timer()
        private var pct = 0.0

        fun draw(posX: Float, posY: Float) {
            if (pressedTimer.passed(30)) {
                isPressed = false
                pressedTimer.reset()
            }
            val fontHeight = MainFontRenderer.getHeight(resize) / 2

            RenderUtils2D.drawRectFilled(posX, posY, posX + width, posY + height, ColorRGB(16 + pct.toInt(), 16 + pct.toInt(), 16 + pct.toInt(), 117))
            MainFontRenderer.drawCenteredString(button, posX + width / 2f, posY + height / 2f - fontHeight, GUIManager.white, resize, false)
        }

        fun calAnimation() {
            pct = AnimationUtil.animate(if (isPressed) 239.0 else 0.0, pct, 0.19)
        }

        fun onPress() {
            pressedTimer.reset()
            isPressed = true
        }
    }
}