package club.eridani.epsilon.client.gui

import club.eridani.epsilon.client.util.Timer

interface IFatherExtendable : IFatherComponent {
    val timer: Timer
    var isPaused: Boolean
    var target: Int
    var current: Int
    var visibleChildren: List<IChildComponent>

    fun updateChildren() {
        visibleChildren = children.filter { it.isVisible() }
    }
}
