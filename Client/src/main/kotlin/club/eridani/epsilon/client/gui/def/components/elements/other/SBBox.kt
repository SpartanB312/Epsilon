package club.eridani.epsilon.client.gui.def.components.elements.other

import club.eridani.epsilon.client.gui.ISettingSupplier
import club.eridani.epsilon.client.gui.def.AsyncRenderEngine
import club.eridani.epsilon.client.gui.def.components.AbstractElement
import club.eridani.epsilon.client.gui.def.components.AnimatedAlphaUnit
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.setting.AbstractSetting
import club.eridani.epsilon.client.util.ColorHSB
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.math.Vec2f
import kotlin.math.max
import kotlin.math.min

class SBBox(override val setting: AbstractSetting<ColorRGB>, override var height: Int = 74, override var width: Int = 89) :
    AbstractElement(), ISettingSupplier<ColorRGB> {

    override val animatedAlphaUnit: AnimatedAlphaUnit = AnimatedAlphaUnit()

    var animatedWidth = 0f
    var animatedHeight = 0f

    val fieldStart get() = Vec2f(x + 5f, y + 5f)
    val fieldEnd get() = Vec2f(x + 69f, y + 69f)

    val alphaStart get() = Vec2f(x + 74f, y + 5f)
    val alphaEnd get() = Vec2f(x + 84f, y + 69f)

    private var dragging = false

    override fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float) {
        AsyncRenderEngine.currentTheme.saturationBrightnessBox(this, mouseX, mouseY, partialTicks)
        if (isHoovered(mouseX, mouseY) && getDescription().isNotEmpty()) {
            GUIManager.defaultGUI.currentDescription = getDescription()

            if (dragging) {
                if (hooveredIn(fieldStart.minus(5f, 5f), fieldEnd.plus(3f, 5f), mouseX, mouseY)) {
                    setting.value = ColorHSB(setting.value.hue, ((mouseX - this.x - 5f) / 64f).coerceIn(0.0f, 1.0f), (1.0f - (mouseY - this.y - 5f) / 64f)
                        .coerceIn(0.0f, 1.0f)).toRGB().alpha(setting.value.a)
                }
                if (hooveredIn(alphaStart.minus(2f, 5f), alphaEnd.plus(5f, 5f), mouseX, mouseY)) {
                    setting.value = setting.value.alpha(( (1.0f - (mouseY - this.y - 5f) / 64f).coerceIn(0.0f, 1.0f) * 255).toInt())
                }
            }
        }
    }

    override fun getDescription(): String {
        return "R:${setting.value.r}  G:${setting.value.g}  B:${setting.value.b}  A:${setting.value.a}"
    }

    override fun onMouseClicked(x: Int, y: Int, button: Int): Boolean {
        if (!setting.isVisible || !isHoovered(x, y)) return false
        if (button == 0) {
            dragging = true
            return true
        }
        return false
    }

    override fun onMouseReleased(x: Int, y: Int, state: Int) {
        dragging = false
    }

    private fun hooveredIn(start: Vec2f, end: Vec2f, mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= min(start.x, end.x) && mouseX <= max(start.x, end.x) && mouseY >= min(start.y, end.y) && mouseY <= max(start.y, end.y)
    }

}