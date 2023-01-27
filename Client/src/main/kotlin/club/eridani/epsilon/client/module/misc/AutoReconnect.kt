package club.eridani.epsilon.client.module.misc

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.message
import club.eridani.epsilon.client.common.extensions.parentScreen
import club.eridani.epsilon.client.common.extensions.reason
import club.eridani.epsilon.client.event.events.GuiEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.module.Module
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import kotlin.math.max

internal object AutoReconnect : Module(
    name = "AutoReconnect",
    description = "Automatically reconnects after being disconnected",
    category = Category.Misc
) {
    private val delay by setting("Delay", 5.0f, 0.5f..100.0f, 0.5f)

    private var prevServerDate: ServerData? = null

    init {
        listener<GuiEvent.Closed>(true) {
            if (it.screen is GuiConnecting) prevServerDate = mc.currentServerData
        }

        listener<GuiEvent.Displayed>(true) {
            if (isDisabled || (prevServerDate == null && mc.currentServerData == null)) return@listener
            (it.screen as? GuiDisconnected)?.let { gui ->
                it.screen = TrollGuiDisconnected(gui)
            }
        }
    }

    private class TrollGuiDisconnected(disconnected: GuiDisconnected) : GuiDisconnected(disconnected.parentScreen, disconnected.reason, disconnected.message) {
        private val time = System.currentTimeMillis()

        override fun updateScreen() {
            if (System.currentTimeMillis() - time >= delay * 1000.0f) {
                mc.displayGuiScreen(GuiConnecting(parentScreen, mc, mc.currentServerData ?: prevServerDate ?: return))
            }
        }

        override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
            super.drawScreen(mouseX, mouseY, partialTicks)
            val ms = max(delay * 1000.0f - (System.currentTimeMillis() - time), 0.0f).toInt()
            val text = "Reconnecting in ${ms}ms"
            fontRenderer.drawString(text, width * 0.5f - fontRenderer.getStringWidth(text) * 0.5f, height - 32.0f, 0xffffff, true)
        }
    }
}
