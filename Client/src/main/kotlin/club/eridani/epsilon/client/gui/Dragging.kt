package club.eridani.epsilon.client.gui

class Dragging(private val component: IComponent) {

    var isDragging = false
    private var x2: Int = 0
    private var y2: Int = 0

    fun updatePos(mouseX: Int, mouseY: Int) {
        if (isDragging) {
            component.x = x2 + mouseX
            component.y = y2 + mouseY
        }
    }

    fun onClick(x: Int, y: Int, button: Int) {
        if (button == 0) {
            x2 = component.x - x
            y2 = component.y - y
            isDragging = true
        }
    }

    fun release(state: Int) {
        if (state == 0) isDragging = false
    }

}