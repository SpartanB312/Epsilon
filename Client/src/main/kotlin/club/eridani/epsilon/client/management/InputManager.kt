package club.eridani.epsilon.client.management

import club.eridani.epsilon.client.common.key.KeyBind
import club.eridani.epsilon.client.event.events.InputEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.util.concurrent.CopyOnWriteArrayList

object InputManager {

    private val listeners = CopyOnWriteArrayList<KeyBind>()

    fun KeyBind.register() {
        listeners.add(this)
    }

    fun KeyBind.unregister() {
        listeners.remove(this)
    }

    @JvmStatic
    fun onKey(key: Int) {
        listeners.forEach {
            if (it.key.size > 1) {
                if (it.key.any { it2 ->
                        it2 == key
                    } && it.key.all { it3 ->
                        Keyboard.isKeyDown(it3)
                    }) it.action.invoke()
            } else if (it.key[0] == key) {
                it.action.invoke()
            }
        }
    }

    @JvmStatic
    fun onKeyInput() {
        val key = Keyboard.getEventKey()
        val state = Keyboard.getEventKeyState()
        InputEvent.Keyboard(key, state).post()
    }

    @JvmStatic
    fun onMouseInput() {
        InputEvent.Mouse(Mouse.getEventButton(), Mouse.getEventButtonState()).post()
    }

}