package club.eridani.epsilon.client.gui

import kotlin.math.max
import kotlin.math.min

interface IComponent {

    var x: Int
    var y: Int
    var width: Int
    var height: Int

    fun onMouseReleased(x: Int, y: Int, state: Int) {
    }

    fun keyTyped(char: Char, key: Int): Boolean {
        return false
    }

    fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float) {
    }

    fun onMouseClicked(x: Int, y: Int, button: Int): Boolean {
        return false
    }

    fun isHoovered(mouseX: Int, mouseY: Int, predicate: Boolean = true): Boolean {
        return mouseX >= min(x, x + width) && mouseX <= max(x, x + width)
                && mouseY >= min(y, y + height) && mouseY <= max(y, y + height)
                && predicate
    }

    fun isVisible(): Boolean {
        return true
    }

}