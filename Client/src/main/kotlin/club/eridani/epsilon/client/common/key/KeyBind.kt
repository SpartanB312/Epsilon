package club.eridani.epsilon.client.common.key

import org.lwjgl.input.Keyboard

class KeyBind(vararg var key: Int, val action: () -> Unit) {
    inline val rawValues: String
        get() {
            val list = mutableListOf<String>()
            key.forEach {
                list.add(Keyboard.getKeyName(it))
            }
            return list.joinToString(separator = "+")
        }

    inline val displayValue get() = rawValues.replace("CONTROL", "CTRL", true).replace("MENU", "ALT", true)
}