package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Event
import net.minecraft.client.gui.GuiScreen

sealed class GuiEvent : Event() {
    abstract val screen: GuiScreen?

    class Closed(override val screen: GuiScreen) : GuiEvent()

    class Displayed(override var screen: GuiScreen?) : GuiEvent()
}