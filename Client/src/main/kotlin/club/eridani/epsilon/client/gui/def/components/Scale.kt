package club.eridani.epsilon.client.gui.def.components

import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer

object Scale {
    val panelHeight
        get() = (MainFontRenderer.getHeight() + 9).toInt()

    val moduleButtonHeight
        get() = (MainFontRenderer.getHeight() + 8).toInt()

    val settingHeight
        get() = (MainFontRenderer.getHeight() + 5).toInt()
}