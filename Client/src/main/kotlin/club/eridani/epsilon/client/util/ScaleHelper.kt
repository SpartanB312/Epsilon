package club.eridani.epsilon.client.util

import club.eridani.epsilon.client.gui.SpartanScreen
import net.minecraft.client.gui.ScaledResolution

object ScaleHelper {

    init {
        onSpartanTick {
            if (Wrapper.player == null && Wrapper.mc.currentScreen is SpartanScreen) {
                scaledResolution = ScaledResolution(Wrapper.mc)
            }
        }
    }

    var lastScale = getScale()

    private fun getScale(): Int {
        val scaledWidth = Wrapper.mc.displayWidth
        val scaledHeight = Wrapper.mc.displayHeight
        var scaleFactor = 1
        val flag = Wrapper.mc.isUnicode
        var i = Wrapper.mc.gameSettings.guiScale

        if (i == 0) {
            i = 1000
        }
        while (scaleFactor < i && scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor
        }
        if (flag && scaleFactor % 2 != 0 && scaleFactor != 1) {
            --scaleFactor
        }
        return scaleFactor
    }

    var scaledResolution = ScaledResolution(Wrapper.mc)
        set(value) {
            field = value
            lastScale = getScale()
        }

    val width get() = scaledResolution.scaledWidth
    val height get() = scaledResolution.scaledHeight

}