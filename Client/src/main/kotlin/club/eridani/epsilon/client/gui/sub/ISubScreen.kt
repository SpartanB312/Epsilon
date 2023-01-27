package club.eridani.epsilon.client.gui.sub

interface ISubScreen {

    fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float) {
    }

    fun onMouseClicked(mouseX: Int, mouseY: Int, button: Int) {
    }

    fun onMouseReleased(mouseX: Int, mouseY: Int, partialTicks: Float) {
    }

    fun onKeyTyped(keyCode: Int)
}