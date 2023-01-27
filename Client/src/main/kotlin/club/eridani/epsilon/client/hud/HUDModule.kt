package club.eridani.epsilon.client.hud

import club.eridani.epsilon.client.common.AbstractModule
import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.gui.def.components.elements.other.HUDFrame
import club.eridani.epsilon.client.management.SpartanCore.addAsyncUpdateListener
import club.eridani.epsilon.client.module.client.HUDEditor
import org.lwjgl.input.Keyboard

@Suppress("LeakingThis")
abstract class HUDModule(
    name: String,
    alias: Array<String> = emptyArray(),
    category: Category,
    description: String,
    x: Int = 0,
    y: Int = 0,
    height: Int = 0,
    width: Int = 0,
    priority: Int = 1000,
    visibleOnArray: Boolean = false,
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

    val hudFrame: HUDFrame = HUDFrame(this, x, y, width, height)

    abstract fun onRender()

    final override fun onAsyncUpdate(block: () -> Unit) {
        addAsyncUpdateListener {
            if (isEnabled || (isDisabled && HUDEditor.isHUDEditor())) block.invoke()
        }
    }

    inline val x get() = hudFrame.x
    inline val y get() = hudFrame.y
    inline val width get() = hudFrame.width
    inline val height get() = hudFrame.height

    fun resize(block: HUDFrame.() -> Unit) {
        block.invoke(this.hudFrame)
    }

}