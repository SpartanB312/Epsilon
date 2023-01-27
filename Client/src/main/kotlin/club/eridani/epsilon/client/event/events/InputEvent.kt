package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Event

sealed class InputEvent(val state: Boolean) : Event() {
    class Keyboard(val key: Int, state: Boolean) : InputEvent(state)

    class Mouse(val button: Int, state: Boolean) : InputEvent(state)
}
