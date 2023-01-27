package club.eridani.epsilon.client.module

import club.eridani.epsilon.client.common.AbstractModule
import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.management.SpartanCore.addAsyncUpdateListener
import org.lwjgl.input.Keyboard

open class Module(
    name: String,
    alias: Array<String> = emptyArray(),
    category: Category,
    description: String,
    priority: Int = 1000,
    visibleOnArray: Boolean = true,
    keyBind: Int = Keyboard.KEY_NONE
) : AbstractModule(
    name,
    alias,
    category,
    description,
    priority,
    keyBind,
    visibleOnArray
) {
    final override fun onAsyncUpdate(block: () -> Unit) {
        addAsyncUpdateListener {
            if (isEnabled) block.invoke()
        }
    }
}