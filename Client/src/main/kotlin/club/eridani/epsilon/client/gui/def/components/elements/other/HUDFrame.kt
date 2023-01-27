package club.eridani.epsilon.client.gui.def.components.elements.other

import club.eridani.epsilon.client.gui.Dragging
import club.eridani.epsilon.client.gui.IComponent
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.graphics.RenderUtils2D

class HUDFrame(
    val hudModule: HUDModule,
    override var x: Int,
    override var y: Int,
    override var width: Int,
    override var height: Int
) : IComponent {

    private val dragging = Dragging(this)

    private val backgroundColor = ColorRGB(1, 1, 1, 128)

    override fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float) {
        dragging.updatePos(mouseX, mouseY)
        RenderUtils2D.drawRectFilled(x, y, x + width, y + height, backgroundColor)
        hudModule.onRender()
    }

    override fun onMouseClicked(x: Int, y: Int, button: Int): Boolean {
        if (isHoovered(x, y)) {
            dragging.onClick(x, y, button)
            return true
        }
        return false
    }

    override fun onMouseReleased(x: Int, y: Int, state: Int) {
        dragging.release(state)
    }

}