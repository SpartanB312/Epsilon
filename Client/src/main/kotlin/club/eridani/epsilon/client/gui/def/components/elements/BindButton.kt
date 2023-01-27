package club.eridani.epsilon.client.gui.def.components.elements

import club.eridani.epsilon.client.common.key.KeyBind
import club.eridani.epsilon.client.gui.*
import club.eridani.epsilon.client.gui.def.AsyncRenderEngine
import club.eridani.epsilon.client.gui.def.components.AbstractElement
import club.eridani.epsilon.client.gui.def.components.AnimatedAlphaUnit
import club.eridani.epsilon.client.gui.def.components.Panel
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.setting.AbstractSetting
import org.lwjgl.input.Keyboard.*

class BindButton(
    override var father: IFatherComponent,
    override val setting: AbstractSetting<KeyBind>,
    override var x: Int = father.x,
    override var y: Int = father.y,
    override var height: Int = father.height,
    override var width: Int = father.width,
    override val panel: Panel
) : AbstractElement(), IChildComponent, ISettingSupplier<KeyBind>, IPanelProvider, IDescriptorContainer {

    var accepting = false

    override val animatedAlphaUnit = AnimatedAlphaUnit()

    override fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float) {
        AsyncRenderEngine.currentTheme.bindButton(this, mouseX, mouseY, partialTicks)
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

    fun getKey(): String {
        val list = mutableListOf<String>()
        setting.value.key.forEach {
            list.add(getKeyName(it))
        }
        return list.joinToString(separator = "+")
    }

    override fun keyTyped(char: Char, key: Int): Boolean {
        if (accepting) {
            if (key == KEY_BACK) {
                setting.value.key = listOf(KEY_NONE).toIntArray()
            } else {
                val binds = mutableListOf(key).also {
                    if (isKeyDown(KEY_LCONTROL)) it.add(KEY_LCONTROL)
                    if (isKeyDown(KEY_RCONTROL)) it.add(KEY_RCONTROL)
                    if (isKeyDown(KEY_LMENU)) it.add(KEY_LMENU)
                    if (isKeyDown(KEY_RMENU)) it.add(KEY_RMENU)
                }
                setting.value.key = binds.toIntArray()
            }
            accepting = false
            return true
        }
        return false
    }

    override fun onMouseClicked(x: Int, y: Int, button: Int): Boolean {
        if (!isHoovered(x, y) || !panel.hooveredInDrawnPanel(x, y)) return false
        if (button == 0) {
            accepting = !accepting
        }
        return true
    }

}