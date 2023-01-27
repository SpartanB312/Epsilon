package club.eridani.epsilon.client.gui

import club.eridani.epsilon.client.event.decentralized.IDecentralizedEvent
import club.eridani.epsilon.client.event.decentralized.Listenable
import club.eridani.epsilon.client.language.TextUnit
import club.eridani.epsilon.client.util.ColorRGB
import net.minecraft.client.gui.GuiScreen

open class SpartanScreen : GuiScreen(), Listenable {

    override val subscribedListener = ArrayList<Triple<IDecentralizedEvent<*>, (Any) -> Unit, Int>>()

    var colorPicker: ISettingSupplier<ColorRGB>? = null
    var description: TextUnit? = null

    open fun onUpdate(mouseX: Int, mouseY: Int, partialTicks: Float) {
    }

}