package club.eridani.epsilon.client.hud.info

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.util.Timer
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.onPacketReceive

object LagNotification : HUDModule(
    name = "LagNotification",
    category = Category.InfoHUD,
    description = "Notify you when server is no responding"
) {

    val timeOut by setting("TimeOut Sceound", 2f, 0.1f..10f, 0.1f)

    val timer = Timer()

    init {
        onPacketReceive {
            timer.reset()
        }
    }

    override fun onRender() {
        resize {
            width = 150
            height = (MainFontRenderer.getHeight() + 1).toInt()
        }
        val seconds = (System.currentTimeMillis() - timer.time).toFloat() / 1000.0f % 60.0f
        if (seconds >= timeOut) {
            MainFontRenderer.drawString("Server no responding " + String.format("%.1f", seconds) + " seconds.", x.toFloat(), y.toFloat(), GUIManager.firstColor)
        }
    }
}