package club.eridani.epsilon.client.menu.main.elements

import club.eridani.epsilon.client.gui.IComponent

class MainSelectButton(
    val name: String,
    val action: () -> Unit = {},
    override var x: Int = 0,
    override var y: Int = 0,
    override var width: Int = 0,
    override var height: Int = 0
) : IComponent {
    var size = 1.0f
    var alpha = 0
}