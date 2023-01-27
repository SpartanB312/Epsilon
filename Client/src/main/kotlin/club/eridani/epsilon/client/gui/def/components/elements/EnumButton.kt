package club.eridani.epsilon.client.gui.def.components.elements

import club.eridani.epsilon.client.gui.*
import club.eridani.epsilon.client.gui.def.AsyncRenderEngine
import club.eridani.epsilon.client.gui.def.components.AbstractElement
import club.eridani.epsilon.client.gui.def.components.AnimatedAlphaUnit
import club.eridani.epsilon.client.gui.def.components.Panel
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.setting.impl.primitive.EnumSetting
import kotlin.math.max
import kotlin.math.min

class EnumButton<T : Enum<T>>(
    override var father: IFatherComponent,
    override val setting: EnumSetting<T>,
    override var x: Int = father.x,
    override var y: Int = father.y,
    override var height: Int = father.height,
    override var width: Int = father.width,
    override val panel: Panel
) : AbstractElement(), IChildComponent, ISettingSupplier<T>, IPanelProvider, IDescriptorContainer {

    override val animatedAlphaUnit = AnimatedAlphaUnit()

    override fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float) {
        AsyncRenderEngine.currentTheme.enumButton(this, mouseX, mouseY, partialTicks)
    }

    override fun drawDescription(mouseX: Int, mouseY: Int) {
        if (isHoovered(mouseX, mouseY) && getDescription() != "") {
            GUIManager.defaultGUI.currentDescription = getDescription()
        }
    }

    override fun getDescription(): String {
        return setting.description.currentText
    }

    override fun isVisible(): Boolean {
        return setting.isVisible
    }

    override fun onMouseClicked(x: Int, y: Int, button: Int): Boolean {
        if (!setting.isVisible || !isHoovered(x, y) || !panel.hooveredInDrawnPanel(x, y)) return false
        if (button == 0) {
            if (onRight(x, y)) setting.nextValue()
            else if (onLeft(x, y)) setting.lastValue()
            return true
        }
        return false
    }

    private fun onRight(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= min(x.toFloat(), x + width / 2f) && mouseX <= max(x.toFloat(), x + width / 2f)
                && mouseY >= min(y, y + height) && mouseY <= max(y, y + height)
    }

    private fun onLeft(mouseX: Int, mouseY: Int): Boolean = !onRight(mouseX, mouseY)

}